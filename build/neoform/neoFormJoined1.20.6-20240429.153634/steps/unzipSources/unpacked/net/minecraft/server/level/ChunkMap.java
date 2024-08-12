package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class ChunkMap extends ChunkStorage implements ChunkHolder.PlayerProvider {
    private static final byte CHUNK_TYPE_REPLACEABLE = -1;
    private static final byte CHUNK_TYPE_UNKNOWN = 0;
    private static final byte CHUNK_TYPE_FULL = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHUNK_SAVED_PER_TICK = 200;
    private static final int CHUNK_SAVED_EAGERLY_PER_TICK = 20;
    private static final int EAGER_CHUNK_SAVE_COOLDOWN_IN_MILLIS = 10000;
    public static final int MIN_VIEW_DISTANCE = 2;
    public static final int MAX_VIEW_DISTANCE = 32;
    public static final int FORCED_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap<>();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap<>();
    private final LongSet entitiesInLevel = new LongOpenHashSet();
    final ServerLevel level;
    private final ThreadedLevelLightEngine lightEngine;
    private final BlockableEventLoop<Runnable> mainThreadExecutor;
    private ChunkGenerator generator;
    private final RandomState randomState;
    private final ChunkGeneratorStructureState chunkGeneratorState;
    private final Supplier<DimensionDataStorage> overworldDataStorage;
    private final PoiManager poiManager;
    final LongSet toDrop = new LongOpenHashSet();
    private boolean modified;
    private final ChunkTaskPriorityQueueSorter queueSorter;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    private final ChunkProgressListener progressListener;
    private final ChunkStatusUpdateListener chunkStatusListener;
    private final ChunkMap.DistanceManager distanceManager;
    private final AtomicInteger tickingGenerated = new AtomicInteger();
    private final String storageName;
    private final PlayerMap playerMap = new PlayerMap();
    private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
    private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
    private final Long2LongMap chunkSaveCooldowns = new Long2LongOpenHashMap();
    private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
    private int serverViewDistance;
    private WorldGenContext worldGenContext;

    public ChunkMap(
        ServerLevel p_214836_,
        LevelStorageSource.LevelStorageAccess p_214837_,
        DataFixer p_214838_,
        StructureTemplateManager p_214839_,
        Executor p_214840_,
        BlockableEventLoop<Runnable> p_214841_,
        LightChunkGetter p_214842_,
        ChunkGenerator p_214843_,
        ChunkProgressListener p_214844_,
        ChunkStatusUpdateListener p_214845_,
        Supplier<DimensionDataStorage> p_214846_,
        int p_214847_,
        boolean p_214848_
    ) {
        super(
            new RegionStorageInfo(p_214837_.getLevelId(), p_214836_.dimension(), "chunk"),
            p_214837_.getDimensionPath(p_214836_.dimension()).resolve("region"),
            p_214838_,
            p_214848_
        );
        Path path = p_214837_.getDimensionPath(p_214836_.dimension());
        this.storageName = path.getFileName().toString();
        this.level = p_214836_;
        this.generator = p_214843_;
        RegistryAccess registryaccess = p_214836_.registryAccess();
        long i = p_214836_.getSeed();
        if (p_214843_ instanceof NoiseBasedChunkGenerator noisebasedchunkgenerator) {
            this.randomState = RandomState.create(noisebasedchunkgenerator.generatorSettings().value(), registryaccess.lookupOrThrow(Registries.NOISE), i);
        } else {
            this.randomState = RandomState.create(NoiseGeneratorSettings.dummy(), registryaccess.lookupOrThrow(Registries.NOISE), i);
        }

        this.chunkGeneratorState = p_214843_.createState(registryaccess.lookupOrThrow(Registries.STRUCTURE_SET), this.randomState, i);
        this.mainThreadExecutor = p_214841_;
        ProcessorMailbox<Runnable> processormailbox1 = ProcessorMailbox.create(p_214840_, "worldgen");
        ProcessorHandle<Runnable> processorhandle = ProcessorHandle.of("main", p_214841_::tell);
        this.progressListener = p_214844_;
        this.chunkStatusListener = p_214845_;
        ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(p_214840_, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter(
            ImmutableList.of(processormailbox1, processorhandle, processormailbox), p_214840_, Integer.MAX_VALUE
        );
        this.worldgenMailbox = this.queueSorter.getProcessor(processormailbox1, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(processorhandle, false);
        this.lightEngine = new ThreadedLevelLightEngine(
            p_214842_, this, this.level.dimensionType().hasSkyLight(), processormailbox, this.queueSorter.getProcessor(processormailbox, false)
        );
        this.distanceManager = new ChunkMap.DistanceManager(p_214840_, p_214841_);
        this.overworldDataStorage = p_214846_;
        this.poiManager = new PoiManager(
            new RegionStorageInfo(p_214837_.getLevelId(), p_214836_.dimension(), "poi"), path.resolve("poi"), p_214838_, p_214848_, registryaccess, p_214836_
        );
        this.setServerViewDistance(p_214847_);
        this.worldGenContext = new WorldGenContext(p_214836_, p_214843_, p_214839_, this.lightEngine);
    }

    protected ChunkGenerator generator() {
        return this.generator;
    }

    protected ChunkGeneratorStructureState generatorState() {
        return this.chunkGeneratorState;
    }

    protected RandomState randomState() {
        return this.randomState;
    }

    public void debugReloadGenerator() {
        DataResult<JsonElement> dataresult = ChunkGenerator.CODEC.encodeStart(JsonOps.INSTANCE, this.generator);
        DataResult<ChunkGenerator> dataresult1 = dataresult.flatMap(p_183804_ -> ChunkGenerator.CODEC.parse(JsonOps.INSTANCE, p_183804_));
        dataresult1.ifSuccess(
            p_329941_ -> {
                this.generator = p_329941_;
                this.worldGenContext = new WorldGenContext(
                    this.worldGenContext.level(), p_329941_, this.worldGenContext.structureManager(), this.worldGenContext.lightEngine()
                );
            }
        );
    }

    private static double euclideanDistanceSquared(ChunkPos p_140227_, Entity p_140228_) {
        double d0 = (double)SectionPos.sectionToBlockCoord(p_140227_.x, 8);
        double d1 = (double)SectionPos.sectionToBlockCoord(p_140227_.z, 8);
        double d2 = d0 - p_140228_.getX();
        double d3 = d1 - p_140228_.getZ();
        return d2 * d2 + d3 * d3;
    }

    boolean isChunkTracked(ServerPlayer p_295366_, int p_294911_, int p_296247_) {
        return p_295366_.getChunkTrackingView().contains(p_294911_, p_296247_)
            && !p_295366_.connection.chunkSender.isPending(ChunkPos.asLong(p_294911_, p_296247_));
    }

    private boolean isChunkOnTrackedBorder(ServerPlayer p_295596_, int p_294838_, int p_295212_) {
        if (!this.isChunkTracked(p_295596_, p_294838_, p_295212_)) {
            return false;
        } else {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if ((i != 0 || j != 0) && !this.isChunkTracked(p_295596_, p_294838_ + i, p_295212_ + j)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    protected ThreadedLevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    protected ChunkHolder getUpdatingChunkIfPresent(long p_140175_) {
        return this.updatingChunkMap.get(p_140175_);
    }

    @Nullable
    public ChunkHolder getVisibleChunkIfPresent(long p_140328_) {
        return this.visibleChunkMap.get(p_140328_);
    }

    protected IntSupplier getChunkQueueLevel(long p_140372_) {
        return () -> {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_140372_);
            return chunkholder == null
                ? ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1
                : Math.min(chunkholder.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
        };
    }

    public String getChunkDebugData(ChunkPos p_140205_) {
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_140205_.toLong());
        if (chunkholder == null) {
            return "null";
        } else {
            String s = chunkholder.getTicketLevel() + "\n";
            ChunkStatus chunkstatus = chunkholder.getLastAvailableStatus();
            ChunkAccess chunkaccess = chunkholder.getLastAvailable();
            if (chunkstatus != null) {
                s = s + "St: \u00a7" + chunkstatus.getIndex() + chunkstatus + "\u00a7r\n";
            }

            if (chunkaccess != null) {
                s = s + "Ch: \u00a7" + chunkaccess.getStatus().getIndex() + chunkaccess.getStatus() + "\u00a7r\n";
            }

            FullChunkStatus fullchunkstatus = chunkholder.getFullStatus();
            s = s + '\u00a7' + fullchunkstatus.ordinal() + fullchunkstatus;
            return s + "\u00a7r";
        }
    }

    private CompletableFuture<ChunkResult<List<ChunkAccess>>> getChunkRangeFuture(ChunkHolder p_281446_, int p_282030_, IntFunction<ChunkStatus> p_282923_) {
        if (p_282030_ == 0) {
            ChunkStatus chunkstatus1 = p_282923_.apply(0);
            return p_281446_.getOrScheduleFuture(chunkstatus1, this).thenApply(p_329931_ -> p_329931_.map(List::of));
        } else {
            List<CompletableFuture<ChunkResult<ChunkAccess>>> list = new ArrayList<>();
            List<ChunkHolder> list1 = new ArrayList<>();
            ChunkPos chunkpos = p_281446_.getPos();
            int i = chunkpos.x;
            int j = chunkpos.z;

            for (int k = -p_282030_; k <= p_282030_; k++) {
                for (int l = -p_282030_; l <= p_282030_; l++) {
                    int i1 = Math.max(Math.abs(l), Math.abs(k));
                    ChunkPos chunkpos1 = new ChunkPos(i + l, j + k);
                    long j1 = chunkpos1.toLong();
                    ChunkHolder chunkholder = this.getUpdatingChunkIfPresent(j1);
                    if (chunkholder == null) {
                        return CompletableFuture.completedFuture(ChunkResult.error(() -> "Unloaded " + chunkpos1));
                    }

                    ChunkStatus chunkstatus = p_282923_.apply(i1);
                    CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = chunkholder.getOrScheduleFuture(chunkstatus, this);
                    list1.add(chunkholder);
                    list.add(completablefuture);
                }
            }

            CompletableFuture<List<ChunkResult<ChunkAccess>>> completablefuture1 = Util.sequence(list);
            CompletableFuture<ChunkResult<List<ChunkAccess>>> completablefuture2 = completablefuture1.thenApply(
                p_329951_ -> {
                    List<ChunkAccess> list2 = Lists.newArrayList();
                    int k1 = 0;

                    for (ChunkResult<ChunkAccess> chunkresult : p_329951_) {
                        if (chunkresult == null) {
                            throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                        }

                        ChunkAccess chunkaccess = chunkresult.orElse(null);
                        if (chunkaccess == null) {
                            int l1 = k1;
                            return ChunkResult.error(
                                () -> "Unloaded " + new ChunkPos(i + l1 % (p_282030_ * 2 + 1), j + l1 / (p_282030_ * 2 + 1)) + " " + chunkresult.getError()
                            );
                        }

                        list2.add(chunkaccess);
                        k1++;
                    }

                    return ChunkResult.of(list2);
                }
            );

            for (ChunkHolder chunkholder1 : list1) {
                chunkholder1.addSaveDependency("getChunkRangeFuture " + chunkpos + " " + p_282030_, completablefuture2);
            }

            return completablefuture2;
        }
    }

    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException p_203752_, String p_203753_) {
        StringBuilder stringbuilder = new StringBuilder();
        Consumer<ChunkHolder> consumer = p_203756_ -> p_203756_.getAllFutures()
                .forEach(
                    p_329939_ -> {
                        ChunkStatus chunkstatus = p_329939_.getFirst();
                        CompletableFuture<ChunkResult<ChunkAccess>> completablefuture = p_329939_.getSecond();
                        if (completablefuture != null && completablefuture.isDone() && completablefuture.join() == null) {
                            stringbuilder.append(p_203756_.getPos())
                                .append(" - status: ")
                                .append(chunkstatus)
                                .append(" future: ")
                                .append(completablefuture)
                                .append(System.lineSeparator());
                        }
                    }
                );
        stringbuilder.append("Updating:").append(System.lineSeparator());
        this.updatingChunkMap.values().forEach(consumer);
        stringbuilder.append("Visible:").append(System.lineSeparator());
        this.visibleChunkMap.values().forEach(consumer);
        CrashReport crashreport = CrashReport.forThrowable(p_203752_, "Chunk loading");
        CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk loading");
        crashreportcategory.setDetail("Details", p_203753_);
        crashreportcategory.setDetail("Futures", stringbuilder);
        return new ReportedException(crashreport);
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareEntityTickingChunk(ChunkHolder p_281455_) {
        return this.getChunkRangeFuture(p_281455_, 2, p_329942_ -> ChunkStatus.FULL)
            .thenApplyAsync(p_329945_ -> p_329945_.map(p_214939_ -> (LevelChunk)p_214939_.get(p_214939_.size() / 2)), this.mainThreadExecutor);
    }

    @Nullable
    ChunkHolder updateChunkScheduling(long p_140177_, int p_140178_, @Nullable ChunkHolder p_140179_, int p_140180_) {
        if (!ChunkLevel.isLoaded(p_140180_) && !ChunkLevel.isLoaded(p_140178_)) {
            return p_140179_;
        } else {
            if (p_140179_ != null) {
                p_140179_.setTicketLevel(p_140178_);
            }

            if (p_140179_ != null) {
                if (!ChunkLevel.isLoaded(p_140178_)) {
                    this.toDrop.add(p_140177_);
                } else {
                    this.toDrop.remove(p_140177_);
                }
            }

            if (ChunkLevel.isLoaded(p_140178_) && p_140179_ == null) {
                p_140179_ = this.pendingUnloads.remove(p_140177_);
                if (p_140179_ != null) {
                    p_140179_.setTicketLevel(p_140178_);
                } else {
                    p_140179_ = new ChunkHolder(new ChunkPos(p_140177_), p_140178_, this.level, this.lightEngine, this.queueSorter, this);
                }

                this.updatingChunkMap.put(p_140177_, p_140179_);
                this.modified = true;
            }

            net.neoforged.neoforge.event.EventHooks.fireChunkTicketLevelUpdated(this.level, p_140177_, p_140180_, p_140178_, p_140179_);
            return p_140179_;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.queueSorter.close();
            this.poiManager.close();
        } finally {
            super.close();
        }
    }

    protected void saveAllChunks(boolean p_140319_) {
        if (p_140319_) {
            List<ChunkHolder> list = this.visibleChunkMap
                .values()
                .stream()
                .filter(ChunkHolder::wasAccessibleSinceLastSave)
                .peek(ChunkHolder::refreshAccessibility)
                .toList();
            MutableBoolean mutableboolean = new MutableBoolean();

            do {
                mutableboolean.setFalse();
                list.stream()
                    .map(p_203102_ -> {
                        CompletableFuture<ChunkAccess> completablefuture;
                        do {
                            completablefuture = p_203102_.getChunkToSave();
                            this.mainThreadExecutor.managedBlock(completablefuture::isDone);
                        } while (completablefuture != p_203102_.getChunkToSave());

                        return completablefuture.join();
                    })
                    .filter(p_203088_ -> p_203088_ instanceof ImposterProtoChunk || p_203088_ instanceof LevelChunk)
                    .filter(this::save)
                    .forEach(p_203051_ -> mutableboolean.setTrue());
            } while (mutableboolean.isTrue());

            this.processUnloads(() -> true);
            this.flushWorker();
        } else {
            this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
        }
    }

    protected void tick(BooleanSupplier p_140281_) {
        ProfilerFiller profilerfiller = this.level.getProfiler();
        profilerfiller.push("poi");
        this.poiManager.tick(p_140281_);
        profilerfiller.popPush("chunk_unload");
        if (!this.level.noSave()) {
            this.processUnloads(p_140281_);
        }

        profilerfiller.pop();
    }

    public boolean hasWork() {
        return this.lightEngine.hasLightWork()
            || !this.pendingUnloads.isEmpty()
            || !this.updatingChunkMap.isEmpty()
            || this.poiManager.hasWork()
            || !this.toDrop.isEmpty()
            || !this.unloadQueue.isEmpty()
            || this.queueSorter.hasWork()
            || this.distanceManager.hasTickets();
    }

    private void processUnloads(BooleanSupplier p_140354_) {
        LongIterator longiterator = this.toDrop.iterator();

        for (int i = 0; longiterator.hasNext() && (p_140354_.getAsBoolean() || i < 200 || this.toDrop.size() > 2000); longiterator.remove()) {
            long j = longiterator.nextLong();
            ChunkHolder chunkholder = this.updatingChunkMap.remove(j);
            if (chunkholder != null) {
                this.pendingUnloads.put(j, chunkholder);
                this.modified = true;
                i++;
                this.scheduleUnload(j, chunkholder);
            }
        }

        int k = Math.max(0, this.unloadQueue.size() - 2000);

        Runnable runnable;
        while ((p_140354_.getAsBoolean() || k > 0) && (runnable = this.unloadQueue.poll()) != null) {
            k--;
            runnable.run();
        }

        int l = 0;
        ObjectIterator<ChunkHolder> objectiterator = this.visibleChunkMap.values().iterator();

        while (l < 20 && p_140354_.getAsBoolean() && objectiterator.hasNext()) {
            if (this.saveChunkIfNeeded(objectiterator.next())) {
                l++;
            }
        }
    }

    private void scheduleUnload(long p_140182_, ChunkHolder p_140183_) {
        CompletableFuture<ChunkAccess> completablefuture = p_140183_.getChunkToSave();
        completablefuture.thenAcceptAsync(p_203002_ -> {
            CompletableFuture<ChunkAccess> completablefuture1 = p_140183_.getChunkToSave();
            if (completablefuture1 != completablefuture) {
                this.scheduleUnload(p_140182_, p_140183_);
            } else {
                if (this.pendingUnloads.remove(p_140182_, p_140183_) && p_203002_ != null) {
                    net.neoforged.neoforge.common.CommonHooks.onChunkUnload(this.poiManager, p_203002_); // Neo: Must be called for all chunk unloading. Not just LevelChunks.
                    this.chunkTypeCache.remove(p_203002_.getPos().toLong()); // Neo: Prevent chunk type cache from permanently retaining data for unloaded chunks
                    if (p_203002_ instanceof LevelChunk) {
                        ((LevelChunk)p_203002_).setLoaded(false);
                        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.ChunkEvent.Unload(p_203002_));
                    }

                    this.save(p_203002_);
                    if (this.entitiesInLevel.remove(p_140182_) && p_203002_ instanceof LevelChunk levelchunk) {
                        this.level.unload(levelchunk);
                    }

                    this.lightEngine.updateChunkStatus(p_203002_.getPos());
                    this.lightEngine.tryScheduleUpdate();
                    this.progressListener.onStatusChange(p_203002_.getPos(), null);
                    this.chunkSaveCooldowns.remove(p_203002_.getPos().toLong());
                }
            }
        }, this.unloadQueue::add).whenComplete((p_202996_, p_202997_) -> {
            if (p_202997_ != null) {
                LOGGER.error("Failed to save chunk {}", p_140183_.getPos(), p_202997_);
            }
        });
    }

    protected boolean promoteChunkMap() {
        if (!this.modified) {
            return false;
        } else {
            this.visibleChunkMap = this.updatingChunkMap.clone();
            this.modified = false;
            return true;
        }
    }

    public CompletableFuture<ChunkResult<ChunkAccess>> schedule(ChunkHolder p_140293_, ChunkStatus p_331633_) {
        ChunkPos chunkpos = p_140293_.getPos();
        if (p_331633_ == ChunkStatus.EMPTY) {
            return this.scheduleChunkLoad(chunkpos).thenApply(ChunkResult::of);
        } else {
            if (p_331633_ == ChunkStatus.LIGHT) {
                this.distanceManager.addTicket(TicketType.LIGHT, chunkpos, ChunkLevel.byStatus(ChunkStatus.LIGHT), chunkpos);
            }

            if (!p_331633_.hasLoadDependencies()) {
                ChunkAccess chunkaccess = p_140293_.getOrScheduleFuture(p_331633_.getParent(), this).getNow(ChunkHolder.UNLOADED_CHUNK).orElse(null);
                if (chunkaccess != null && chunkaccess.getStatus().isOrAfter(p_331633_)) {
                    CompletableFuture<ChunkAccess> completablefuture = p_331633_.load(
                        this.worldGenContext, p_329947_ -> this.protoChunkToFullChunk(p_140293_, p_329947_), chunkaccess
                    );
                    this.progressListener.onStatusChange(chunkpos, p_331633_);
                    return completablefuture.thenApply(ChunkResult::of);
                }
            }

            return this.scheduleChunkGeneration(p_140293_, p_331633_);
        }
    }

    private CompletableFuture<ChunkAccess> scheduleChunkLoad(ChunkPos p_140418_) {
        return this.readChunk(p_140418_).thenApply(p_214925_ -> p_214925_.filter(p_214928_ -> {
                boolean flag = isChunkDataValid(p_214928_);
                if (!flag) {
                    LOGGER.error("Chunk file at {} is missing level data, skipping", p_140418_);
                }

                return flag;
            })).thenApplyAsync(p_340671_ -> {
            this.level.getProfiler().incrementCounter("chunkLoad");
            if (p_340671_.isPresent()) {
                ChunkAccess chunkaccess = ChunkSerializer.read(this.level, this.poiManager, p_140418_, p_340671_.get());
                this.markPosition(p_140418_, chunkaccess.getStatus().getChunkType());
                return chunkaccess;
            } else {
                return this.createEmptyChunk(p_140418_);
            }
        }, this.mainThreadExecutor).exceptionallyAsync(p_329919_ -> this.handleChunkLoadFailure(p_329919_, p_140418_), this.mainThreadExecutor);
    }

    private static boolean isChunkDataValid(CompoundTag p_214941_) {
        return p_214941_.contains("Status", 8);
    }

    private ChunkAccess handleChunkLoadFailure(Throwable p_214902_, ChunkPos p_214903_) {
        Throwable throwable = p_214902_ instanceof CompletionException completionexception ? completionexception.getCause() : p_214902_;
        Throwable throwable1 = throwable instanceof ReportedException reportedexception ? reportedexception.getCause() : throwable;
        boolean flag1 = throwable1 instanceof Error;
        boolean flag = throwable1 instanceof IOException || throwable1 instanceof NbtException;
        if (!flag1) {
            if (!flag) {
            }

            LOGGER.error("Couldn't load chunk {}", p_214903_, throwable1);
            this.level.getServer().reportChunkLoadFailure(p_214903_);
            return this.createEmptyChunk(p_214903_);
        } else {
            CrashReport crashreport = CrashReport.forThrowable(p_214902_, "Exception loading chunk");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk being loaded");
            crashreportcategory.setDetail("pos", p_214903_);
            this.markPositionReplaceable(p_214903_);
            throw new ReportedException(crashreport);
        }
    }

    private ChunkAccess createEmptyChunk(ChunkPos p_214962_) {
        this.markPositionReplaceable(p_214962_);
        return new ProtoChunk(p_214962_, UpgradeData.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registries.BIOME), null);
    }

    private void markPositionReplaceable(ChunkPos p_140423_) {
        this.chunkTypeCache.put(p_140423_.toLong(), (byte)-1);
    }

    private byte markPosition(ChunkPos p_140230_, ChunkType p_332120_) {
        return this.chunkTypeCache.put(p_140230_.toLong(), (byte)(p_332120_ == ChunkType.PROTOCHUNK ? -1 : 1));
    }

    private CompletableFuture<ChunkResult<ChunkAccess>> scheduleChunkGeneration(ChunkHolder p_140361_, ChunkStatus p_330841_) {
        ChunkPos chunkpos = p_140361_.getPos();
        CompletableFuture<ChunkResult<List<ChunkAccess>>> completablefuture = this.getChunkRangeFuture(
            p_140361_, p_330841_.getRange(), p_329911_ -> this.getDependencyStatus(p_330841_, p_329911_)
        );
        this.level.getProfiler().incrementCounter(() -> "chunkGenerate " + p_330841_);
        Executor executor = p_214958_ -> this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_140361_, p_214958_));
        return completablefuture.thenComposeAsync(
            p_329930_ -> {
                List<ChunkAccess> list = p_329930_.orElse(null);
                if (list == null) {
                    this.releaseLightTicket(chunkpos);
                    return CompletableFuture.completedFuture(ChunkResult.error(p_329930_::getError));
                } else {
                    try {
                        ChunkAccess chunkaccess = list.get(list.size() / 2);
                        CompletableFuture<ChunkAccess> completablefuture1;
                        if (chunkaccess.getStatus().isOrAfter(p_330841_)) {
                            completablefuture1 = p_330841_.load(
                                this.worldGenContext, p_329922_ -> this.protoChunkToFullChunk(p_140361_, p_329922_), chunkaccess
                            );
                        } else {
                            completablefuture1 = p_330841_.generate(
                                this.worldGenContext, executor, p_329944_ -> this.protoChunkToFullChunk(p_140361_, p_329944_), list
                            );
                        }

                        this.progressListener.onStatusChange(chunkpos, p_330841_);
                        return completablefuture1.thenApply(ChunkResult::of);
                    } catch (Exception exception) {
                        exception.getStackTrace();
                        CrashReport crashreport = CrashReport.forThrowable(exception, "Exception generating new chunk");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk to be generated");
                        crashreportcategory.setDetail("Status being generated", () -> BuiltInRegistries.CHUNK_STATUS.getKey(p_330841_).toString());
                        crashreportcategory.setDetail("Location", String.format(Locale.ROOT, "%d,%d", chunkpos.x, chunkpos.z));
                        crashreportcategory.setDetail("Position hash", ChunkPos.asLong(chunkpos.x, chunkpos.z));
                        crashreportcategory.setDetail("Generator", this.generator);
                        this.mainThreadExecutor.execute(() -> {
                            throw new ReportedException(crashreport);
                        });
                        throw new ReportedException(crashreport);
                    }
                }
            },
            executor
        );
    }

    protected void releaseLightTicket(ChunkPos p_140376_) {
        this.mainThreadExecutor
            .tell(
                Util.name(
                    () -> this.distanceManager.removeTicket(TicketType.LIGHT, p_140376_, ChunkLevel.byStatus(ChunkStatus.LIGHT), p_140376_),
                    () -> "release light ticket " + p_140376_
                )
            );
    }

    private ChunkStatus getDependencyStatus(ChunkStatus p_331608_, int p_140264_) {
        ChunkStatus chunkstatus;
        if (p_140264_ == 0) {
            chunkstatus = p_331608_.getParent();
        } else {
            chunkstatus = ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance(p_331608_) + p_140264_);
        }

        return chunkstatus;
    }

    private static void postLoadProtoChunk(ServerLevel p_143065_, List<CompoundTag> p_143066_) {
        if (!p_143066_.isEmpty()) {
            p_143065_.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(p_143066_, p_143065_));
        }
    }

    private CompletableFuture<ChunkAccess> protoChunkToFullChunk(ChunkHolder p_140384_, ChunkAccess p_330540_) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkPos chunkpos = p_140384_.getPos();
            ProtoChunk protochunk = (ProtoChunk)p_330540_;
            LevelChunk levelchunk;
            if (protochunk instanceof ImposterProtoChunk) {
                levelchunk = ((ImposterProtoChunk)protochunk).getWrapped();
            } else {
                levelchunk = new LevelChunk(this.level, protochunk, p_214900_ -> postLoadProtoChunk(this.level, protochunk.getEntities()));
                p_140384_.replaceProtoChunk(new ImposterProtoChunk(levelchunk, false));
            }

            levelchunk.setFullStatus(() -> ChunkLevel.fullStatus(p_140384_.getTicketLevel()));
            try {
            p_140384_.currentlyLoading = levelchunk; // Neo: bypass the future chain when getChunk is called, this prevents deadlocks.
            levelchunk.runPostLoad();
            } finally {
                p_140384_.currentlyLoading = null; // Neo: Stop bypassing the future chain.
            }
            if (this.entitiesInLevel.add(chunkpos.toLong())) {
                levelchunk.setLoaded(true);
                try {
                p_140384_.currentlyLoading = levelchunk; // Neo: bypass the future chain when getChunk is called, this prevents deadlocks.
                levelchunk.registerAllBlockEntitiesAfterLevelLoad();
                levelchunk.registerTickContainerInLevel(this.level);
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.ChunkEvent.Load(levelchunk, !(protochunk instanceof ImposterProtoChunk)));
                } finally {
                    p_140384_.currentlyLoading = null; // Neo: Stop bypassing the future chain.
                }
            }

            return levelchunk;
        }, p_214951_ -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_214951_, p_140384_.getPos().toLong(), p_140384_::getTicketLevel)));
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareTickingChunk(ChunkHolder p_143054_) {
        CompletableFuture<ChunkResult<List<ChunkAccess>>> completablefuture = this.getChunkRangeFuture(p_143054_, 1, p_329920_ -> ChunkStatus.FULL);
        CompletableFuture<ChunkResult<LevelChunk>> completablefuture1 = completablefuture.<ChunkResult<LevelChunk>>thenApplyAsync(
                p_329912_ -> p_329912_.map(p_293806_ -> (LevelChunk)p_293806_.get(p_293806_.size() / 2)),
                p_214944_ -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143054_, p_214944_))
            )
            .thenApplyAsync(p_329924_ -> p_329924_.ifSuccess(p_300770_ -> {
                    p_300770_.postProcessGeneration();
                    this.level.startTickingChunk(p_300770_);
                    CompletableFuture<?> completablefuture2 = p_143054_.getChunkSendSyncFuture();
                    if (completablefuture2.isDone()) {
                        this.onChunkReadyToSend(p_300770_);
                    } else {
                        completablefuture2.thenAcceptAsync(p_300774_ -> this.onChunkReadyToSend(p_300770_), this.mainThreadExecutor);
                    }
                }), this.mainThreadExecutor);
        completablefuture1.handle((p_331041_, p_287365_) -> {
            this.tickingGenerated.getAndIncrement();
            return null;
        });
        return completablefuture1;
    }

    private void onChunkReadyToSend(LevelChunk p_296003_) {
        ChunkPos chunkpos = p_296003_.getPos();

        for (ServerPlayer serverplayer : this.playerMap.getAllPlayers()) {
            if (serverplayer.getChunkTrackingView().contains(chunkpos)) {
                markChunkPendingToSend(serverplayer, p_296003_);
            }
        }
    }

    public CompletableFuture<ChunkResult<LevelChunk>> prepareAccessibleChunk(ChunkHolder p_143110_) {
        return this.getChunkRangeFuture(p_143110_, 1, ChunkStatus::getStatusAroundFullChunk)
            .thenApplyAsync(
                p_329940_ -> p_329940_.map(p_203092_ -> (LevelChunk)p_203092_.get(p_203092_.size() / 2)),
                p_214859_ -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143110_, p_214859_))
            );
    }

    public int getTickingGenerated() {
        return this.tickingGenerated.get();
    }

    private boolean saveChunkIfNeeded(ChunkHolder p_198875_) {
        if (!p_198875_.wasAccessibleSinceLastSave()) {
            return false;
        } else {
            ChunkAccess chunkaccess = p_198875_.getChunkToSave().getNow(null);
            if (!(chunkaccess instanceof ImposterProtoChunk) && !(chunkaccess instanceof LevelChunk)) {
                return false;
            } else {
                long i = chunkaccess.getPos().toLong();
                long j = this.chunkSaveCooldowns.getOrDefault(i, -1L);
                long k = System.currentTimeMillis();
                if (k < j) {
                    return false;
                } else {
                    boolean flag = this.save(chunkaccess);
                    p_198875_.refreshAccessibility();
                    if (flag) {
                        this.chunkSaveCooldowns.put(i, k + 10000L);
                    }

                    return flag;
                }
            }
        }
    }

    private boolean save(ChunkAccess p_140259_) {
        this.poiManager.flush(p_140259_.getPos());
        if (!p_140259_.isUnsaved()) {
            return false;
        } else {
            p_140259_.setUnsaved(false);
            ChunkPos chunkpos = p_140259_.getPos();

            try {
                ChunkStatus chunkstatus = p_140259_.getStatus();
                if (chunkstatus.getChunkType() != ChunkType.LEVELCHUNK) {
                    if (this.isExistingChunkFull(chunkpos)) {
                        return false;
                    }

                    if (chunkstatus == ChunkStatus.EMPTY && p_140259_.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                        return false;
                    }
                }

                this.level.getProfiler().incrementCounter("chunkSave");
                CompoundTag compoundtag = ChunkSerializer.write(this.level, p_140259_);
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.ChunkDataEvent.Save(p_140259_, p_140259_.getLevel() != null ? p_140259_.getLevel() : this.level, compoundtag));
                this.write(chunkpos, compoundtag).exceptionallyAsync(p_329914_ -> {
                    this.level.getServer().reportChunkSaveFailure(chunkpos);
                    return null;
                }, this.mainThreadExecutor);
                this.markPosition(chunkpos, chunkstatus.getChunkType());
                return true;
            } catch (Exception exception) {
                LOGGER.error("Failed to save chunk {},{}", chunkpos.x, chunkpos.z, exception);
                this.level.getServer().reportChunkSaveFailure(chunkpos);
                return false;
            }
        }
    }

    private boolean isExistingChunkFull(ChunkPos p_140426_) {
        byte b0 = this.chunkTypeCache.get(p_140426_.toLong());
        if (b0 != 0) {
            return b0 == 1;
        } else {
            CompoundTag compoundtag;
            try {
                compoundtag = this.readChunk(p_140426_).join().orElse(null);
                if (compoundtag == null) {
                    this.markPositionReplaceable(p_140426_);
                    return false;
                }
            } catch (Exception exception) {
                LOGGER.error("Failed to read chunk {}", p_140426_, exception);
                this.markPositionReplaceable(p_140426_);
                return false;
            }

            ChunkType chunktype = ChunkSerializer.getChunkTypeFromTag(compoundtag);
            return this.markPosition(p_140426_, chunktype) == 1;
        }
    }

    protected void setServerViewDistance(int p_295758_) {
        int i = Mth.clamp(p_295758_, 2, 32);
        if (i != this.serverViewDistance) {
            this.serverViewDistance = i;
            this.distanceManager.updatePlayerTickets(this.serverViewDistance);

            for (ServerPlayer serverplayer : this.playerMap.getAllPlayers()) {
                this.updateChunkTracking(serverplayer);
            }
        }
    }

    int getPlayerViewDistance(ServerPlayer p_295024_) {
        return Mth.clamp(p_295024_.requestedViewDistance(), 2, this.serverViewDistance);
    }

    private void markChunkPendingToSend(ServerPlayer p_294638_, ChunkPos p_296183_) {
        LevelChunk levelchunk = this.getChunkToSend(p_296183_.toLong());
        if (levelchunk != null) {
            markChunkPendingToSend(p_294638_, levelchunk);
        }
    }

    private static void markChunkPendingToSend(ServerPlayer p_295834_, LevelChunk p_296281_) {
        p_295834_.connection.chunkSender.markChunkPendingToSend(p_296281_);
        net.neoforged.neoforge.event.EventHooks.fireChunkWatch(p_295834_, p_296281_, p_295834_.serverLevel());
    }

    private static void dropChunk(ServerPlayer p_294215_, ChunkPos p_294758_) {
        net.neoforged.neoforge.event.EventHooks.fireChunkUnWatch(p_294215_, p_294758_, p_294215_.serverLevel());
        p_294215_.connection.chunkSender.dropChunk(p_294215_, p_294758_);
    }

    @Nullable
    public LevelChunk getChunkToSend(long p_300929_) {
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_300929_);
        return chunkholder == null ? null : chunkholder.getChunkToSend();
    }

    public int size() {
        return this.visibleChunkMap.size();
    }

    public net.minecraft.server.level.DistanceManager getDistanceManager() {
        return this.distanceManager;
    }

    protected Iterable<ChunkHolder> getChunks() {
        return Iterables.unmodifiableIterable(this.visibleChunkMap.values());
    }

    void dumpChunks(Writer p_140275_) throws IOException {
        CsvOutput csvoutput = CsvOutput.builder()
            .addColumn("x")
            .addColumn("z")
            .addColumn("level")
            .addColumn("in_memory")
            .addColumn("status")
            .addColumn("full_status")
            .addColumn("accessible_ready")
            .addColumn("ticking_ready")
            .addColumn("entity_ticking_ready")
            .addColumn("ticket")
            .addColumn("spawning")
            .addColumn("block_entity_count")
            .addColumn("ticking_ticket")
            .addColumn("ticking_level")
            .addColumn("block_ticks")
            .addColumn("fluid_ticks")
            .build(p_140275_);
        TickingTracker tickingtracker = this.distanceManager.tickingTracker();

        for (Entry<ChunkHolder> entry : this.visibleChunkMap.long2ObjectEntrySet()) {
            long i = entry.getLongKey();
            ChunkPos chunkpos = new ChunkPos(i);
            ChunkHolder chunkholder = entry.getValue();
            Optional<ChunkAccess> optional = Optional.ofNullable(chunkholder.getLastAvailable());
            Optional<LevelChunk> optional1 = optional.flatMap(
                p_214932_ -> p_214932_ instanceof LevelChunk ? Optional.of((LevelChunk)p_214932_) : Optional.empty()
            );
            csvoutput.writeRow(
                chunkpos.x,
                chunkpos.z,
                chunkholder.getTicketLevel(),
                optional.isPresent(),
                optional.map(ChunkAccess::getStatus).orElse(null),
                optional1.map(LevelChunk::getFullStatus).orElse(null),
                printFuture(chunkholder.getFullChunkFuture()),
                printFuture(chunkholder.getTickingChunkFuture()),
                printFuture(chunkholder.getEntityTickingChunkFuture()),
                this.distanceManager.getTicketDebugString(i),
                this.anyPlayerCloseEnoughForSpawning(chunkpos),
                optional1.<Integer>map(p_214953_ -> p_214953_.getBlockEntities().size()).orElse(0),
                tickingtracker.getTicketDebugString(i),
                tickingtracker.getLevel(i),
                optional1.<Integer>map(p_214946_ -> p_214946_.getBlockTicks().count()).orElse(0),
                optional1.<Integer>map(p_214937_ -> p_214937_.getFluidTicks().count()).orElse(0)
            );
        }
    }

    private static String printFuture(CompletableFuture<ChunkResult<LevelChunk>> p_140279_) {
        try {
            ChunkResult<LevelChunk> chunkresult = p_140279_.getNow(null);
            if (chunkresult != null) {
                return chunkresult.isSuccess() ? "done" : "unloaded";
            } else {
                return "not completed";
            }
        } catch (CompletionException completionexception) {
            return "failed " + completionexception.getCause().getMessage();
        } catch (CancellationException cancellationexception) {
            return "cancelled";
        }
    }

    private CompletableFuture<Optional<CompoundTag>> readChunk(ChunkPos p_214964_) {
        return this.read(p_214964_).thenApplyAsync(p_214907_ -> p_214907_.map(this::upgradeChunkTag), Util.backgroundExecutor());
    }

    private CompoundTag upgradeChunkTag(CompoundTag p_214948_) {
        return this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, p_214948_, this.generator.getTypeNameForDataFixer());
    }

    boolean anyPlayerCloseEnoughForSpawning(ChunkPos p_183880_) {
        if (!this.distanceManager.hasPlayersNearby(p_183880_.toLong())) {
            return false;
        } else {
            for (ServerPlayer serverplayer : this.playerMap.getAllPlayers()) {
                if (this.playerIsCloseEnoughForSpawning(serverplayer, p_183880_)) {
                    return true;
                }
            }

            return false;
        }
    }

    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos p_183889_) {
        long i = p_183889_.toLong();
        if (!this.distanceManager.hasPlayersNearby(i)) {
            return List.of();
        } else {
            Builder<ServerPlayer> builder = ImmutableList.builder();

            for (ServerPlayer serverplayer : this.playerMap.getAllPlayers()) {
                if (this.playerIsCloseEnoughForSpawning(serverplayer, p_183889_)) {
                    builder.add(serverplayer);
                }
            }

            return builder.build();
        }
    }

    private boolean playerIsCloseEnoughForSpawning(ServerPlayer p_183752_, ChunkPos p_183753_) {
        if (p_183752_.isSpectator()) {
            return false;
        } else {
            double d0 = euclideanDistanceSquared(p_183753_, p_183752_);
            return d0 < 16384.0;
        }
    }

    private boolean skipPlayer(ServerPlayer p_140330_) {
        return p_140330_.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
    }

    void updatePlayerStatus(ServerPlayer p_140193_, boolean p_140194_) {
        boolean flag = this.skipPlayer(p_140193_);
        boolean flag1 = this.playerMap.ignoredOrUnknown(p_140193_);
        if (p_140194_) {
            this.playerMap.addPlayer(p_140193_, flag);
            this.updatePlayerPos(p_140193_);
            if (!flag) {
                this.distanceManager.addPlayer(SectionPos.of(p_140193_), p_140193_);
            }

            p_140193_.setChunkTrackingView(ChunkTrackingView.EMPTY);
            this.updateChunkTracking(p_140193_);
        } else {
            SectionPos sectionpos = p_140193_.getLastSectionPos();
            this.playerMap.removePlayer(p_140193_);
            if (!flag1) {
                this.distanceManager.removePlayer(sectionpos, p_140193_);
            }

            this.applyChunkTrackingView(p_140193_, ChunkTrackingView.EMPTY);
        }
    }

    private void updatePlayerPos(ServerPlayer p_140374_) {
        SectionPos sectionpos = SectionPos.of(p_140374_);
        p_140374_.setLastSectionPos(sectionpos);
    }

    public void move(ServerPlayer p_140185_) {
        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
            if (chunkmap$trackedentity.entity == p_140185_) {
                chunkmap$trackedentity.updatePlayers(this.level.players());
            } else {
                chunkmap$trackedentity.updatePlayer(p_140185_);
            }
        }

        SectionPos sectionpos = p_140185_.getLastSectionPos();
        SectionPos sectionpos1 = SectionPos.of(p_140185_);
        boolean flag = this.playerMap.ignored(p_140185_);
        boolean flag1 = this.skipPlayer(p_140185_);
        boolean flag2 = sectionpos.asLong() != sectionpos1.asLong();
        if (flag2 || flag != flag1) {
            this.updatePlayerPos(p_140185_);
            if (!flag) {
                this.distanceManager.removePlayer(sectionpos, p_140185_);
            }

            if (!flag1) {
                this.distanceManager.addPlayer(sectionpos1, p_140185_);
            }

            if (!flag && flag1) {
                this.playerMap.ignorePlayer(p_140185_);
            }

            if (flag && !flag1) {
                this.playerMap.unIgnorePlayer(p_140185_);
            }

            //PATCH 1.20.2: Figure out the firing of the watch and unwatch events when chunk tracking updates.
            this.updateChunkTracking(p_140185_);
        }
    }

    private void updateChunkTracking(ServerPlayer p_183755_) {
        ChunkPos chunkpos = p_183755_.chunkPosition();
        int i = this.getPlayerViewDistance(p_183755_);
        if (p_183755_.getChunkTrackingView() instanceof ChunkTrackingView.Positioned chunktrackingview$positioned
            && chunktrackingview$positioned.center().equals(chunkpos)
            && chunktrackingview$positioned.viewDistance() == i) {
            return;
        }

        this.applyChunkTrackingView(p_183755_, ChunkTrackingView.of(chunkpos, i));
    }

    private void applyChunkTrackingView(ServerPlayer p_294188_, ChunkTrackingView p_294174_) {
        if (p_294188_.level() == this.level) {
            ChunkTrackingView chunktrackingview = p_294188_.getChunkTrackingView();
            if (p_294174_ instanceof ChunkTrackingView.Positioned chunktrackingview$positioned
                && (
                    !(chunktrackingview instanceof ChunkTrackingView.Positioned chunktrackingview$positioned1)
                        || !chunktrackingview$positioned1.center().equals(chunktrackingview$positioned.center())
                )) {
                p_294188_.connection
                    .send(new ClientboundSetChunkCacheCenterPacket(chunktrackingview$positioned.center().x, chunktrackingview$positioned.center().z));
            }

            ChunkTrackingView.difference(
                chunktrackingview, p_294174_, p_293802_ -> this.markChunkPendingToSend(p_294188_, p_293802_), p_293800_ -> dropChunk(p_294188_, p_293800_)
            );
            p_294188_.setChunkTrackingView(p_294174_);
        }
    }

    @Override
    public List<ServerPlayer> getPlayers(ChunkPos p_183801_, boolean p_183802_) {
        Set<ServerPlayer> set = this.playerMap.getAllPlayers();
        Builder<ServerPlayer> builder = ImmutableList.builder();

        for (ServerPlayer serverplayer : set) {
            if (p_183802_ && this.isChunkOnTrackedBorder(serverplayer, p_183801_.x, p_183801_.z)
                || !p_183802_ && this.isChunkTracked(serverplayer, p_183801_.x, p_183801_.z)) {
                builder.add(serverplayer);
            }
        }

        return builder.build();
    }

    protected void addEntity(Entity p_140200_) {
        if (!(p_140200_ instanceof net.neoforged.neoforge.entity.PartEntity)) {
            EntityType<?> entitytype = p_140200_.getType();
            int i = entitytype.clientTrackingRange() * 16;
            if (i != 0) {
                int j = entitytype.updateInterval();
                if (this.entityMap.containsKey(p_140200_.getId())) {
                    throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
                } else {
                    ChunkMap.TrackedEntity chunkmap$trackedentity = new ChunkMap.TrackedEntity(p_140200_, i, j, entitytype.trackDeltas());
                    this.entityMap.put(p_140200_.getId(), chunkmap$trackedentity);
                    chunkmap$trackedentity.updatePlayers(this.level.players());
                    if (p_140200_ instanceof ServerPlayer serverplayer) {
                        this.updatePlayerStatus(serverplayer, true);

                        for (ChunkMap.TrackedEntity chunkmap$trackedentity1 : this.entityMap.values()) {
                            if (chunkmap$trackedentity1.entity != serverplayer) {
                                chunkmap$trackedentity1.updatePlayer(serverplayer);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void removeEntity(Entity p_140332_) {
        if (p_140332_ instanceof ServerPlayer serverplayer) {
            this.updatePlayerStatus(serverplayer, false);

            for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
                chunkmap$trackedentity.removePlayer(serverplayer);
            }
        }

        ChunkMap.TrackedEntity chunkmap$trackedentity1 = this.entityMap.remove(p_140332_.getId());
        if (chunkmap$trackedentity1 != null) {
            chunkmap$trackedentity1.broadcastRemoved();
        }
    }

    protected void tick() {
        for (ServerPlayer serverplayer : this.playerMap.getAllPlayers()) {
            this.updateChunkTracking(serverplayer);
        }

        List<ServerPlayer> list = Lists.newArrayList();
        List<ServerPlayer> list1 = this.level.players();

        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values()) {
            SectionPos sectionpos = chunkmap$trackedentity.lastSectionPos;
            SectionPos sectionpos1 = SectionPos.of(chunkmap$trackedentity.entity);
            boolean flag = !Objects.equals(sectionpos, sectionpos1);
            if (flag) {
                chunkmap$trackedentity.updatePlayers(list1);
                Entity entity = chunkmap$trackedentity.entity;
                if (entity instanceof ServerPlayer) {
                    list.add((ServerPlayer)entity);
                }

                chunkmap$trackedentity.lastSectionPos = sectionpos1;
            }

            if (flag || this.distanceManager.inEntityTickingRange(sectionpos1.chunk().toLong())) {
                chunkmap$trackedentity.serverEntity.sendChanges();
            }
        }

        if (!list.isEmpty()) {
            for (ChunkMap.TrackedEntity chunkmap$trackedentity1 : this.entityMap.values()) {
                chunkmap$trackedentity1.updatePlayers(list);
            }
        }
    }

    public void broadcast(Entity p_140202_, Packet<?> p_140203_) {
        ChunkMap.TrackedEntity chunkmap$trackedentity = this.entityMap.get(p_140202_.getId());
        if (chunkmap$trackedentity != null) {
            chunkmap$trackedentity.broadcast(p_140203_);
        }
    }

    protected void broadcastAndSend(Entity p_140334_, Packet<?> p_140335_) {
        ChunkMap.TrackedEntity chunkmap$trackedentity = this.entityMap.get(p_140334_.getId());
        if (chunkmap$trackedentity != null) {
            chunkmap$trackedentity.broadcastAndSend(p_140335_);
        }
    }

    public void resendBiomesForChunks(List<ChunkAccess> p_275577_) {
        Map<ServerPlayer, List<LevelChunk>> map = new HashMap<>();

        for (ChunkAccess chunkaccess : p_275577_) {
            ChunkPos chunkpos = chunkaccess.getPos();
            LevelChunk levelchunk;
            if (chunkaccess instanceof LevelChunk levelchunk1) {
                levelchunk = levelchunk1;
            } else {
                levelchunk = this.level.getChunk(chunkpos.x, chunkpos.z);
            }

            for (ServerPlayer serverplayer : this.getPlayers(chunkpos, false)) {
                map.computeIfAbsent(serverplayer, p_274834_ -> new ArrayList<>()).add(levelchunk);
            }
        }

        map.forEach((p_293803_, p_293804_) -> p_293803_.connection.send(ClientboundChunksBiomesPacket.forChunks((List<LevelChunk>)p_293804_)));
    }

    protected PoiManager getPoiManager() {
        return this.poiManager;
    }

    public String getStorageName() {
        return this.storageName;
    }

    void onFullChunkStatusChange(ChunkPos p_287612_, FullChunkStatus p_287685_) {
        this.chunkStatusListener.onChunkStatusChange(p_287612_, p_287685_);
    }

    public void waitForLightBeforeSending(ChunkPos p_301194_, int p_301130_) {
        int i = p_301130_ + 1;
        ChunkPos.rangeClosed(p_301194_, i).forEach(p_300775_ -> {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_300775_.toLong());
            if (chunkholder != null) {
                chunkholder.addSendDependency(this.lightEngine.waitForPendingTasks(p_300775_.x, p_300775_.z));
            }
        });
    }

    class DistanceManager extends net.minecraft.server.level.DistanceManager {
        protected DistanceManager(Executor p_140459_, Executor p_140460_) {
            super(p_140459_, p_140460_);
        }

        @Override
        protected boolean isChunkToRemove(long p_140462_) {
            return ChunkMap.this.toDrop.contains(p_140462_);
        }

        @Nullable
        @Override
        protected ChunkHolder getChunk(long p_140469_) {
            return ChunkMap.this.getUpdatingChunkIfPresent(p_140469_);
        }

        @Nullable
        @Override
        protected ChunkHolder updateChunkScheduling(long p_140464_, int p_140465_, @Nullable ChunkHolder p_140466_, int p_140467_) {
            return ChunkMap.this.updateChunkScheduling(p_140464_, p_140465_, p_140466_, p_140467_);
        }
    }

    class TrackedEntity {
        final ServerEntity serverEntity;
        final Entity entity;
        private final int range;
        SectionPos lastSectionPos;
        private final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

        public TrackedEntity(Entity p_140478_, int p_140479_, int p_140480_, boolean p_140481_) {
            this.serverEntity = new ServerEntity(ChunkMap.this.level, p_140478_, p_140480_, p_140481_, this::broadcast);
            this.entity = p_140478_;
            this.range = p_140479_;
            this.lastSectionPos = SectionPos.of(p_140478_);
        }

        @Override
        public boolean equals(Object p_140506_) {
            return p_140506_ instanceof ChunkMap.TrackedEntity ? ((ChunkMap.TrackedEntity)p_140506_).entity.getId() == this.entity.getId() : false;
        }

        @Override
        public int hashCode() {
            return this.entity.getId();
        }

        public void broadcast(Packet<?> p_140490_) {
            for (ServerPlayerConnection serverplayerconnection : this.seenBy) {
                serverplayerconnection.send(p_140490_);
            }
        }

        public void broadcastAndSend(Packet<?> p_140500_) {
            this.broadcast(p_140500_);
            if (this.entity instanceof ServerPlayer) {
                ((ServerPlayer)this.entity).connection.send(p_140500_);
            }
        }

        public void broadcastRemoved() {
            for (ServerPlayerConnection serverplayerconnection : this.seenBy) {
                this.serverEntity.removePairing(serverplayerconnection.getPlayer());
            }
        }

        public void removePlayer(ServerPlayer p_140486_) {
            if (this.seenBy.remove(p_140486_.connection)) {
                this.serverEntity.removePairing(p_140486_);
            }
        }

        public void updatePlayer(ServerPlayer p_140498_) {
            if (p_140498_ != this.entity) {
                Vec3 vec3 = p_140498_.position().subtract(this.entity.position());
                int i = ChunkMap.this.getPlayerViewDistance(p_140498_);
                double d0 = (double)Math.min(this.getEffectiveRange(), i * 16);
                double d1 = vec3.x * vec3.x + vec3.z * vec3.z;
                double d2 = d0 * d0;
                boolean flag = d1 <= d2
                    && this.entity.broadcastToPlayer(p_140498_)
                    && ChunkMap.this.isChunkTracked(p_140498_, this.entity.chunkPosition().x, this.entity.chunkPosition().z);
                if (flag) {
                    if (this.seenBy.add(p_140498_.connection)) {
                        this.serverEntity.addPairing(p_140498_);
                    }
                } else if (this.seenBy.remove(p_140498_.connection)) {
                    this.serverEntity.removePairing(p_140498_);
                }
            }
        }

        private int scaledRange(int p_140484_) {
            return ChunkMap.this.level.getServer().getScaledTrackingDistance(p_140484_);
        }

        private int getEffectiveRange() {
            int i = this.range;

            for (Entity entity : this.entity.getIndirectPassengers()) {
                int j = entity.getType().clientTrackingRange() * 16;
                if (j > i) {
                    i = j;
                }
            }

            return this.scaledRange(i);
        }

        public void updatePlayers(List<ServerPlayer> p_140488_) {
            for (ServerPlayer serverplayer : p_140488_) {
                this.updatePlayer(serverplayer);
            }
        }
    }

    /**
     * Neo: PR #937
     * This is for mainly pre-generation usage such as Neoforge's generate command.
     * Use this to schedule chunk load tasks into ChunkTaskPriorityQueueSorter so a chunk is fully finished all of their tasks before scheduling more chunks to load.
     * Reason for this is when scheduling a huge ton of Full Status chunk tasks to the server (to load chunks),
     * you could cause the server to only process those loading tasks and never reach the two chunk tasks that are
     * automatically scheduled to run after the chunk is loaded to Full. As a result of flooding the system with Full Status chunk tasks,
     * the queue for the two kind of successor chunk tasks will grow and become a memory leak of lambdas and chunk references.
     * Use this method to schedule tasks for loading chunks in your whenCompleteAsync method call so the tasks gets processed properly over time and not leak.
     * See {@link net.neoforged.neoforge.server.command.generation.GenerationTask#enqueueChunks} as an example usage of this method.
     */
    public void scheduleOnMainThreadMailbox(ChunkTaskPriorityQueueSorter.Message<Runnable> msg) {
        mainThreadMailbox.tell(msg);
    }
}
