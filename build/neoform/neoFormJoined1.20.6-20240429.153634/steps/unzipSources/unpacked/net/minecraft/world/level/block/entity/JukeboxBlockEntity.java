package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;

public class JukeboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem {
    private static final int SONG_END_PADDING = 20;
    private ItemStack item = ItemStack.EMPTY;
    private int ticksSinceLastEvent;
    private long tickCount;
    private long recordStartedTick;
    private boolean isPlaying;

    public JukeboxBlockEntity(BlockPos p_155613_, BlockState p_155614_) {
        super(BlockEntityType.JUKEBOX, p_155613_, p_155614_);
    }

    @Override
    protected void loadAdditional(CompoundTag p_155616_, HolderLookup.Provider p_324026_) {
        super.loadAdditional(p_155616_, p_324026_);
        if (p_155616_.contains("RecordItem", 10)) {
            this.item = ItemStack.parse(p_324026_, p_155616_.getCompound("RecordItem")).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }

        this.isPlaying = p_155616_.getBoolean("IsPlaying");
        this.recordStartedTick = p_155616_.getLong("RecordStartTick");
        this.tickCount = p_155616_.getLong("TickCount");
    }

    @Override
    protected void saveAdditional(CompoundTag p_187507_, HolderLookup.Provider p_323723_) {
        super.saveAdditional(p_187507_, p_323723_);
        if (!this.getTheItem().isEmpty()) {
            p_187507_.put("RecordItem", this.getTheItem().save(p_323723_));
        }

        p_187507_.putBoolean("IsPlaying", this.isPlaying);
        p_187507_.putLong("RecordStartTick", this.recordStartedTick);
        p_187507_.putLong("TickCount", this.tickCount);
    }

    public boolean isRecordPlaying() {
        return !this.getTheItem().isEmpty() && this.isPlaying;
    }

    private void setHasRecordBlockState(@Nullable Entity p_273308_, boolean p_273038_) {
        if (this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(JukeboxBlock.HAS_RECORD, Boolean.valueOf(p_273038_)), 2);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(p_273308_, this.getBlockState()));
        }
    }

    @VisibleForTesting
    public void startPlaying() {
        this.recordStartedTick = this.tickCount;
        this.isPlaying = true;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent(null, 1010, this.getBlockPos(), Item.getId(this.getTheItem().getItem()));
        this.setChanged();
    }

    private void stopPlaying() {
        this.isPlaying = false;
        this.level.gameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.level.levelEvent(1011, this.getBlockPos(), 0);
        this.setChanged();
    }

    private void tick(Level p_273615_, BlockPos p_273143_, BlockState p_273372_) {
        this.ticksSinceLastEvent++;
        if (this.isRecordPlaying() && this.getTheItem().getItem() instanceof RecordItem recorditem) {
            if (this.shouldRecordStopPlaying(recorditem)) {
                this.stopPlaying();
            } else if (this.shouldSendJukeboxPlayingEvent()) {
                this.ticksSinceLastEvent = 0;
                p_273615_.gameEvent(GameEvent.JUKEBOX_PLAY, p_273143_, GameEvent.Context.of(p_273372_));
                this.spawnMusicParticles(p_273615_, p_273143_);
            }
        }

        this.tickCount++;
    }

    private boolean shouldRecordStopPlaying(RecordItem p_273267_) {
        return this.tickCount >= this.recordStartedTick + (long)p_273267_.getLengthInTicks() + 20L;
    }

    private boolean shouldSendJukeboxPlayingEvent() {
        return this.ticksSinceLastEvent >= 20;
    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int p_304604_) {
        ItemStack itemstack = this.item;
        this.item = ItemStack.EMPTY;
        if (!itemstack.isEmpty()) {
            this.setHasRecordBlockState(null, false);
            this.stopPlaying();
        }

        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack p_304781_) {
        if (p_304781_.is(ItemTags.MUSIC_DISCS) && this.level != null) {
            this.item = p_304781_;
            this.setHasRecordBlockState(null, true);
            this.startPlaying();
        } else if (p_304781_.isEmpty()) {
            this.splitTheItem(1);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public boolean canPlaceItem(int p_273369_, ItemStack p_273689_) {
        return p_273689_.is(ItemTags.MUSIC_DISCS) && this.getItem(p_273369_).isEmpty();
    }

    @Override
    public boolean canTakeItem(Container p_273497_, int p_273168_, ItemStack p_273785_) {
        return p_273497_.hasAnyMatching(ItemStack::isEmpty);
    }

    private void spawnMusicParticles(Level p_270782_, BlockPos p_270940_) {
        if (p_270782_ instanceof ServerLevel serverlevel) {
            Vec3 vec3 = Vec3.atBottomCenterOf(p_270940_).add(0.0, 1.2F, 0.0);
            float f = (float)p_270782_.getRandom().nextInt(4) / 24.0F;
            serverlevel.sendParticles(ParticleTypes.NOTE, vec3.x(), vec3.y(), vec3.z(), 0, (double)f, 0.0, 0.0, 1.0);
        }
    }

    public void popOutRecord() {
        if (this.level != null && !this.level.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            ItemStack itemstack = this.getTheItem();
            if (!itemstack.isEmpty()) {
                this.removeTheItem();
                Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockpos, 0.5, 1.01, 0.5).offsetRandom(this.level.random, 0.7F);
                ItemStack itemstack1 = itemstack.copy();
                ItemEntity itementity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
                itementity.setDefaultPickUpDelay();
                this.level.addFreshEntity(itementity);
            }
        }
    }

    public static void playRecordTick(Level p_239938_, BlockPos p_239939_, BlockState p_239940_, JukeboxBlockEntity p_239941_) {
        p_239941_.tick(p_239938_, p_239939_, p_239940_);
    }

    @VisibleForTesting
    public void setRecordWithoutPlaying(ItemStack p_272693_) {
        this.item = p_272693_;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
        this.setChanged();
    }
}
