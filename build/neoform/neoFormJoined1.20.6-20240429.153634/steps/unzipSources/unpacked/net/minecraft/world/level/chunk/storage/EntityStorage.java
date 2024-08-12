package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.slf4j.Logger;

public class EntityStorage implements EntityPersistentStorage<Entity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITIES_TAG = "Entities";
    private static final String POSITION_TAG = "Position";
    private final ServerLevel level;
    private final SimpleRegionStorage simpleRegionStorage;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final ProcessorMailbox<Runnable> entityDeserializerQueue;

    public EntityStorage(SimpleRegionStorage p_321748_, ServerLevel p_196924_, Executor p_196928_) {
        this.simpleRegionStorage = p_321748_;
        this.level = p_196924_;
        this.entityDeserializerQueue = ProcessorMailbox.create(p_196928_, "entity-deserializer");
    }

    @Override
    public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos p_156551_) {
        return this.emptyChunks.contains(p_156551_.toLong())
            ? CompletableFuture.completedFuture(emptyChunk(p_156551_))
            : this.simpleRegionStorage.read(p_156551_).thenApplyAsync(p_321470_ -> {
                if (p_321470_.isEmpty()) {
                    this.emptyChunks.add(p_156551_.toLong());
                    return emptyChunk(p_156551_);
                } else {
                    try {
                        ChunkPos chunkpos = readChunkPos(p_321470_.get());
                        if (!Objects.equals(p_156551_, chunkpos)) {
                            LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", p_156551_, p_156551_, chunkpos);
                        }
                    } catch (Exception exception) {
                        LOGGER.warn("Failed to parse chunk {} position info", p_156551_, exception);
                    }

                    CompoundTag compoundtag = this.simpleRegionStorage.upgradeChunkTag(p_321470_.get(), -1);
                    ListTag listtag = compoundtag.getList("Entities", 10);
                    List<Entity> list = EntityType.loadEntitiesRecursive(listtag, this.level).collect(ImmutableList.toImmutableList());
                    return new ChunkEntities<>(p_156551_, list);
                }
            }, this.entityDeserializerQueue::tell);
    }

    private static ChunkPos readChunkPos(CompoundTag p_156571_) {
        int[] aint = p_156571_.getIntArray("Position");
        return new ChunkPos(aint[0], aint[1]);
    }

    private static void writeChunkPos(CompoundTag p_156563_, ChunkPos p_156564_) {
        p_156563_.put("Position", new IntArrayTag(new int[]{p_156564_.x, p_156564_.z}));
    }

    private static ChunkEntities<Entity> emptyChunk(ChunkPos p_156569_) {
        return new ChunkEntities<>(p_156569_, ImmutableList.of());
    }

    @Override
    public void storeEntities(ChunkEntities<Entity> p_156559_) {
        ChunkPos chunkpos = p_156559_.getPos();
        if (p_156559_.isEmpty()) {
            if (this.emptyChunks.add(chunkpos.toLong())) {
                this.simpleRegionStorage.write(chunkpos, null);
            }
        } else {
            ListTag listtag = new ListTag();
            p_156559_.getEntities().forEach(p_156567_ -> {
                CompoundTag compoundtag1 = new CompoundTag();
                try {
                if (p_156567_.save(compoundtag1)) {
                    listtag.add(compoundtag1);
                }
                } catch (Exception e) {
                    LOGGER.error("An Entity type {} has thrown an exception trying to write state. It will not persist. Report this to the mod author", p_156567_.getType(), e);
                }
            });
            CompoundTag compoundtag = NbtUtils.addCurrentDataVersion(new CompoundTag());
            compoundtag.put("Entities", listtag);
            writeChunkPos(compoundtag, chunkpos);
            this.simpleRegionStorage.write(chunkpos, compoundtag).exceptionally(p_156554_ -> {
                LOGGER.error("Failed to store chunk {}", chunkpos, p_156554_);
                return null;
            });
            this.emptyChunks.remove(chunkpos.toLong());
        }
    }

    @Override
    public void flush(boolean p_182487_) {
        this.simpleRegionStorage.synchronize(p_182487_).join();
        this.entityDeserializerQueue.runAll();
    }

    @Override
    public void close() throws IOException {
        this.simpleRegionStorage.close();
    }
}
