package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final boolean SHOW_GOAL_FOR_ALL_BEES = true;
    private static final boolean SHOW_NAME_FOR_ALL_BEES = true;
    private static final boolean SHOW_HIVE_FOR_ALL_BEES = true;
    private static final boolean SHOW_FLOWER_POS_FOR_ALL_BEES = true;
    private static final boolean SHOW_TRAVEL_TICKS_FOR_ALL_BEES = true;
    private static final boolean SHOW_PATH_FOR_ALL_BEES = false;
    private static final boolean SHOW_GOAL_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_NAME_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_HIVE_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_FLOWER_POS_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_TRAVEL_TICKS_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_PATH_FOR_SELECTED_BEE = true;
    private static final boolean SHOW_HIVE_MEMBERS = true;
    private static final boolean SHOW_BLACKLISTS = true;
    private static final int MAX_RENDER_DIST_FOR_HIVE_OVERLAY = 30;
    private static final int MAX_RENDER_DIST_FOR_BEE_OVERLAY = 30;
    private static final int MAX_TARGETING_DIST = 8;
    private static final int HIVE_TIMEOUT = 20;
    private static final float TEXT_SCALE = 0.02F;
    private static final int WHITE = -1;
    private static final int YELLOW = -256;
    private static final int ORANGE = -23296;
    private static final int GREEN = -16711936;
    private static final int GRAY = -3355444;
    private static final int PINK = -98404;
    private static final int RED = -65536;
    private final Minecraft minecraft;
    private final Map<BlockPos, BeeDebugRenderer.HiveDebugInfo> hives = new HashMap<>();
    private final Map<UUID, BeeDebugPayload.BeeInfo> beeInfosPerEntity = new HashMap<>();
    @Nullable
    private UUID lastLookedAtUuid;

    public BeeDebugRenderer(Minecraft p_113053_) {
        this.minecraft = p_113053_;
    }

    @Override
    public void clear() {
        this.hives.clear();
        this.beeInfosPerEntity.clear();
        this.lastLookedAtUuid = null;
    }

    public void addOrUpdateHiveInfo(HiveDebugPayload.HiveInfo p_294147_, long p_296079_) {
        this.hives.put(p_294147_.pos(), new BeeDebugRenderer.HiveDebugInfo(p_294147_, p_296079_));
    }

    public void addOrUpdateBeeInfo(BeeDebugPayload.BeeInfo p_294571_) {
        this.beeInfosPerEntity.put(p_294571_.uuid(), p_294571_);
    }

    public void removeBeeInfo(int p_173764_) {
        this.beeInfosPerEntity.values().removeIf(p_293626_ -> p_293626_.id() == p_173764_);
    }

    @Override
    public void render(PoseStack p_113061_, MultiBufferSource p_113062_, double p_113063_, double p_113064_, double p_113065_) {
        this.clearRemovedHives();
        this.clearRemovedBees();
        this.doRender(p_113061_, p_113062_);
        if (!this.minecraft.player.isSpectator()) {
            this.updateLastLookedAtUuid();
        }
    }

    private void clearRemovedBees() {
        this.beeInfosPerEntity.entrySet().removeIf(p_293633_ -> this.minecraft.level.getEntity(p_293633_.getValue().id()) == null);
    }

    private void clearRemovedHives() {
        long i = this.minecraft.level.getGameTime() - 20L;
        this.hives.entrySet().removeIf(p_293628_ -> p_293628_.getValue().lastSeen() < i);
    }

    private void doRender(PoseStack p_270886_, MultiBufferSource p_270808_) {
        BlockPos blockpos = this.getCamera().getBlockPosition();
        this.beeInfosPerEntity.values().forEach(p_293636_ -> {
            if (this.isPlayerCloseEnoughToMob(p_293636_)) {
                this.renderBeeInfo(p_270886_, p_270808_, p_293636_);
            }
        });
        this.renderFlowerInfos(p_270886_, p_270808_);

        for (BlockPos blockpos1 : this.hives.keySet()) {
            if (blockpos.closerThan(blockpos1, 30.0)) {
                highlightHive(p_270886_, p_270808_, blockpos1);
            }
        }

        Map<BlockPos, Set<UUID>> map = this.createHiveBlacklistMap();
        this.hives.values().forEach(p_293646_ -> {
            if (blockpos.closerThan(p_293646_.info.pos(), 30.0)) {
                Set<UUID> set = map.get(p_293646_.info.pos());
                this.renderHiveInfo(p_270886_, p_270808_, p_293646_.info, (Collection<UUID>)(set == null ? Sets.newHashSet() : set));
            }
        });
        this.getGhostHives().forEach((p_269699_, p_269700_) -> {
            if (blockpos.closerThan(p_269699_, 30.0)) {
                this.renderGhostHive(p_270886_, p_270808_, p_269699_, (List<String>)p_269700_);
            }
        });
    }

    private Map<BlockPos, Set<UUID>> createHiveBlacklistMap() {
        Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
        this.beeInfosPerEntity
            .values()
            .forEach(
                p_293638_ -> p_293638_.blacklistedHives()
                        .forEach(p_293641_ -> map.computeIfAbsent(p_293641_, p_173777_ -> Sets.newHashSet()).add(p_293638_.uuid()))
            );
        return map;
    }

    private void renderFlowerInfos(PoseStack p_270578_, MultiBufferSource p_270098_) {
        Map<BlockPos, Set<UUID>> map = Maps.newHashMap();
        this.beeInfosPerEntity.values().forEach(p_293651_ -> {
            if (p_293651_.flowerPos() != null) {
                map.computeIfAbsent(p_293651_.flowerPos(), p_293649_ -> new HashSet<>()).add(p_293651_.uuid());
            }
        });
        map.forEach((p_339302_, p_339303_) -> {
            Set<String> set = p_339303_.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
            int i = 1;
            renderTextOverPos(p_270578_, p_270098_, set.toString(), p_339302_, i++, -256);
            renderTextOverPos(p_270578_, p_270098_, "Flower", p_339302_, i++, -1);
            float f = 0.05F;
            DebugRenderer.renderFilledBox(p_270578_, p_270098_, p_339302_, 0.05F, 0.8F, 0.8F, 0.0F, 0.3F);
        });
    }

    private static String getBeeUuidsAsString(Collection<UUID> p_113116_) {
        if (p_113116_.isEmpty()) {
            return "-";
        } else {
            return p_113116_.size() > 3
                ? p_113116_.size() + " bees"
                : p_113116_.stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet()).toString();
        }
    }

    private static void highlightHive(PoseStack p_270133_, MultiBufferSource p_270766_, BlockPos p_270687_) {
        float f = 0.05F;
        DebugRenderer.renderFilledBox(p_270133_, p_270766_, p_270687_, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
    }

    private void renderGhostHive(PoseStack p_270949_, MultiBufferSource p_270718_, BlockPos p_270550_, List<String> p_270221_) {
        float f = 0.05F;
        DebugRenderer.renderFilledBox(p_270949_, p_270718_, p_270550_, 0.05F, 0.2F, 0.2F, 1.0F, 0.3F);
        renderTextOverPos(p_270949_, p_270718_, p_270221_ + "", p_270550_, 0, -256);
        renderTextOverPos(p_270949_, p_270718_, "Ghost Hive", p_270550_, 1, -65536);
    }

    private void renderHiveInfo(PoseStack p_270194_, MultiBufferSource p_270431_, HiveDebugPayload.HiveInfo p_295856_, Collection<UUID> p_270946_) {
        int i = 0;
        if (!p_270946_.isEmpty()) {
            renderTextOverHive(p_270194_, p_270431_, "Blacklisted by " + getBeeUuidsAsString(p_270946_), p_295856_, i++, -65536);
        }

        renderTextOverHive(p_270194_, p_270431_, "Out: " + getBeeUuidsAsString(this.getHiveMembers(p_295856_.pos())), p_295856_, i++, -3355444);
        if (p_295856_.occupantCount() == 0) {
            renderTextOverHive(p_270194_, p_270431_, "In: -", p_295856_, i++, -256);
        } else if (p_295856_.occupantCount() == 1) {
            renderTextOverHive(p_270194_, p_270431_, "In: 1 bee", p_295856_, i++, -256);
        } else {
            renderTextOverHive(p_270194_, p_270431_, "In: " + p_295856_.occupantCount() + " bees", p_295856_, i++, -256);
        }

        renderTextOverHive(p_270194_, p_270431_, "Honey: " + p_295856_.honeyLevel(), p_295856_, i++, -23296);
        renderTextOverHive(p_270194_, p_270431_, p_295856_.hiveType() + (p_295856_.sedated() ? " (sedated)" : ""), p_295856_, i++, -1);
    }

    private void renderPath(PoseStack p_270424_, MultiBufferSource p_270123_, BeeDebugPayload.BeeInfo p_294313_) {
        if (p_294313_.path() != null) {
            PathfindingRenderer.renderPath(
                p_270424_,
                p_270123_,
                p_294313_.path(),
                0.5F,
                false,
                false,
                this.getCamera().getPosition().x(),
                this.getCamera().getPosition().y(),
                this.getCamera().getPosition().z()
            );
        }
    }

    private void renderBeeInfo(PoseStack p_270154_, MultiBufferSource p_270397_, BeeDebugPayload.BeeInfo p_294247_) {
        boolean flag = this.isBeeSelected(p_294247_);
        int i = 0;
        renderTextOverMob(p_270154_, p_270397_, p_294247_.pos(), i++, p_294247_.toString(), -1, 0.03F);
        if (p_294247_.hivePos() == null) {
            renderTextOverMob(p_270154_, p_270397_, p_294247_.pos(), i++, "No hive", -98404, 0.02F);
        } else {
            renderTextOverMob(p_270154_, p_270397_, p_294247_.pos(), i++, "Hive: " + this.getPosDescription(p_294247_, p_294247_.hivePos()), -256, 0.02F);
        }

        if (p_294247_.flowerPos() == null) {
            renderTextOverMob(p_270154_, p_270397_, p_294247_.pos(), i++, "No flower", -98404, 0.02F);
        } else {
            renderTextOverMob(p_270154_, p_270397_, p_294247_.pos(), i++, "Flower: " + this.getPosDescription(p_294247_, p_294247_.flowerPos()), -256, 0.02F);
        }

        for (String s : p_294247_.goals()) {
            renderTextOverMob(p_270154_, p_270397_, p_294247_.pos(), i++, s, -16711936, 0.02F);
        }

        if (flag) {
            this.renderPath(p_270154_, p_270397_, p_294247_);
        }

        if (p_294247_.travelTicks() > 0) {
            int j = p_294247_.travelTicks() < 600 ? -3355444 : -23296;
            renderTextOverMob(p_270154_, p_270397_, p_294247_.pos(), i++, "Travelling: " + p_294247_.travelTicks() + " ticks", j, 0.02F);
        }
    }

    private static void renderTextOverHive(
        PoseStack p_270915_, MultiBufferSource p_270663_, String p_270119_, HiveDebugPayload.HiveInfo p_294995_, int p_270930_, int p_270094_
    ) {
        renderTextOverPos(p_270915_, p_270663_, p_270119_, p_294995_.pos(), p_270930_, p_270094_);
    }

    private static void renderTextOverPos(PoseStack p_270438_, MultiBufferSource p_270244_, String p_270486_, BlockPos p_270062_, int p_270574_, int p_270228_) {
        double d0 = 1.3;
        double d1 = 0.2;
        double d2 = (double)p_270062_.getX() + 0.5;
        double d3 = (double)p_270062_.getY() + 1.3 + (double)p_270574_ * 0.2;
        double d4 = (double)p_270062_.getZ() + 0.5;
        DebugRenderer.renderFloatingText(p_270438_, p_270244_, p_270486_, d2, d3, d4, p_270228_, 0.02F, true, 0.0F, true);
    }

    private static void renderTextOverMob(
        PoseStack p_270426_, MultiBufferSource p_270600_, Position p_270548_, int p_270592_, String p_270198_, int p_270792_, float p_270938_
    ) {
        double d0 = 2.4;
        double d1 = 0.25;
        BlockPos blockpos = BlockPos.containing(p_270548_);
        double d2 = (double)blockpos.getX() + 0.5;
        double d3 = p_270548_.y() + 2.4 + (double)p_270592_ * 0.25;
        double d4 = (double)blockpos.getZ() + 0.5;
        float f = 0.5F;
        DebugRenderer.renderFloatingText(p_270426_, p_270600_, p_270198_, d2, d3, d4, p_270792_, p_270938_, false, 0.5F, true);
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }

    private Set<String> getHiveMemberNames(HiveDebugPayload.HiveInfo p_296213_) {
        return this.getHiveMembers(p_296213_.pos()).stream().map(DebugEntityNameGenerator::getEntityName).collect(Collectors.toSet());
    }

    private String getPosDescription(BeeDebugPayload.BeeInfo p_296210_, BlockPos p_113070_) {
        double d0 = Math.sqrt(p_113070_.distToCenterSqr(p_296210_.pos()));
        double d1 = (double)Math.round(d0 * 10.0) / 10.0;
        return p_113070_.toShortString() + " (dist " + d1 + ")";
    }

    private boolean isBeeSelected(BeeDebugPayload.BeeInfo p_295996_) {
        return Objects.equals(this.lastLookedAtUuid, p_295996_.uuid());
    }

    private boolean isPlayerCloseEnoughToMob(BeeDebugPayload.BeeInfo p_294724_) {
        Player player = this.minecraft.player;
        BlockPos blockpos = BlockPos.containing(player.getX(), p_294724_.pos().y(), player.getZ());
        BlockPos blockpos1 = BlockPos.containing(p_294724_.pos());
        return blockpos.closerThan(blockpos1, 30.0);
    }

    private Collection<UUID> getHiveMembers(BlockPos p_113130_) {
        return this.beeInfosPerEntity
            .values()
            .stream()
            .filter(p_293648_ -> p_293648_.hasHive(p_113130_))
            .map(BeeDebugPayload.BeeInfo::uuid)
            .collect(Collectors.toSet());
    }

    private Map<BlockPos, List<String>> getGhostHives() {
        Map<BlockPos, List<String>> map = Maps.newHashMap();

        for (BeeDebugPayload.BeeInfo beedebugpayload$beeinfo : this.beeInfosPerEntity.values()) {
            if (beedebugpayload$beeinfo.hivePos() != null && !this.hives.containsKey(beedebugpayload$beeinfo.hivePos())) {
                map.computeIfAbsent(beedebugpayload$beeinfo.hivePos(), p_113140_ -> Lists.newArrayList()).add(beedebugpayload$beeinfo.generateName());
            }
        }

        return map;
    }

    private void updateLastLookedAtUuid() {
        DebugRenderer.getTargetedEntity(this.minecraft.getCameraEntity(), 8).ifPresent(p_113059_ -> this.lastLookedAtUuid = p_113059_.getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    static record HiveDebugInfo(HiveDebugPayload.HiveInfo info, long lastSeen) {
    }
}
