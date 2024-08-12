package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class ChiseledBookShelfBlock extends BaseEntityBlock {
    public static final MapCodec<ChiseledBookShelfBlock> CODEC = simpleCodec(ChiseledBookShelfBlock::new);
    private static final int MAX_BOOKS_IN_STORAGE = 6;
    public static final int BOOKS_PER_ROW = 3;
    public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED,
        BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED
    );

    @Override
    public MapCodec<ChiseledBookShelfBlock> codec() {
        return CODEC;
    }

    public ChiseledBookShelfBlock(BlockBehaviour.Properties p_249989_) {
        super(p_249989_);
        BlockState blockstate = this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);

        for (BooleanProperty booleanproperty : SLOT_OCCUPIED_PROPERTIES) {
            blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
        }

        this.registerDefaultState(blockstate);
    }

    @Override
    protected RenderShape getRenderShape(BlockState p_251274_) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(
        ItemStack p_316457_, BlockState p_316201_, Level p_316747_, BlockPos p_316462_, Player p_316228_, InteractionHand p_316721_, BlockHitResult p_316464_
    ) {
        if (p_316747_.getBlockEntity(p_316462_) instanceof ChiseledBookShelfBlockEntity chiseledbookshelfblockentity) {
            if (!p_316457_.is(ItemTags.BOOKSHELF_BOOKS)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
                OptionalInt optionalint = this.getHitSlot(p_316464_, p_316201_);
                if (optionalint.isEmpty()) {
                    return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
                } else if (p_316201_.getValue(SLOT_OCCUPIED_PROPERTIES.get(optionalint.getAsInt()))) {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                } else {
                    addBook(p_316747_, p_316462_, p_316228_, chiseledbookshelfblockentity, p_316457_, optionalint.getAsInt());
                    return ItemInteractionResult.sidedSuccess(p_316747_.isClientSide);
                }
            }
        } else {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_316403_, Level p_316842_, BlockPos p_316539_, Player p_316349_, BlockHitResult p_316278_) {
        if (p_316842_.getBlockEntity(p_316539_) instanceof ChiseledBookShelfBlockEntity chiseledbookshelfblockentity) {
            OptionalInt optionalint = this.getHitSlot(p_316278_, p_316403_);
            if (optionalint.isEmpty()) {
                return InteractionResult.PASS;
            } else if (!p_316403_.getValue(SLOT_OCCUPIED_PROPERTIES.get(optionalint.getAsInt()))) {
                return InteractionResult.CONSUME;
            } else {
                removeBook(p_316842_, p_316539_, p_316349_, chiseledbookshelfblockentity, optionalint.getAsInt());
                return InteractionResult.sidedSuccess(p_316842_.isClientSide);
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private OptionalInt getHitSlot(BlockHitResult p_316156_, BlockState p_316148_) {
        return getRelativeHitCoordinatesForBlockFace(p_316156_, p_316148_.getValue(HorizontalDirectionalBlock.FACING)).map(p_316073_ -> {
            int i = p_316073_.y >= 0.5F ? 0 : 1;
            int j = getSection(p_316073_.x);
            return OptionalInt.of(j + i * 3);
        }).orElseGet(OptionalInt::empty);
    }

    private static Optional<Vec2> getRelativeHitCoordinatesForBlockFace(BlockHitResult p_261714_, Direction p_262116_) {
        Direction direction = p_261714_.getDirection();
        if (p_262116_ != direction) {
            return Optional.empty();
        } else {
            BlockPos blockpos = p_261714_.getBlockPos().relative(direction);
            Vec3 vec3 = p_261714_.getLocation().subtract((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
            double d0 = vec3.x();
            double d1 = vec3.y();
            double d2 = vec3.z();

            return switch (direction) {
                case NORTH -> Optional.of(new Vec2((float)(1.0 - d0), (float)d1));
                case SOUTH -> Optional.of(new Vec2((float)d0, (float)d1));
                case WEST -> Optional.of(new Vec2((float)d2, (float)d1));
                case EAST -> Optional.of(new Vec2((float)(1.0 - d2), (float)d1));
                case DOWN, UP -> Optional.empty();
            };
        }
    }

    private static int getSection(float p_261599_) {
        float f = 0.0625F;
        float f1 = 0.375F;
        if (p_261599_ < 0.375F) {
            return 0;
        } else {
            float f2 = 0.6875F;
            return p_261599_ < 0.6875F ? 1 : 2;
        }
    }

    private static void addBook(
        Level p_262592_, BlockPos p_262669_, Player p_262572_, ChiseledBookShelfBlockEntity p_262606_, ItemStack p_262587_, int p_262692_
    ) {
        if (!p_262592_.isClientSide) {
            p_262572_.awardStat(Stats.ITEM_USED.get(p_262587_.getItem()));
            SoundEvent soundevent = p_262587_.is(Items.ENCHANTED_BOOK)
                ? SoundEvents.CHISELED_BOOKSHELF_INSERT_ENCHANTED
                : SoundEvents.CHISELED_BOOKSHELF_INSERT;
            p_262606_.setItem(p_262692_, p_262587_.split(1));
            p_262592_.playSound(null, p_262669_, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (p_262572_.isCreative()) {
                p_262587_.grow(1);
            }
        }
    }

    private static void removeBook(Level p_262654_, BlockPos p_262601_, Player p_262636_, ChiseledBookShelfBlockEntity p_262605_, int p_262673_) {
        if (!p_262654_.isClientSide) {
            ItemStack itemstack = p_262605_.removeItem(p_262673_, 1);
            SoundEvent soundevent = itemstack.is(Items.ENCHANTED_BOOK)
                ? SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED
                : SoundEvents.CHISELED_BOOKSHELF_PICKUP;
            p_262654_.playSound(null, p_262601_, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!p_262636_.getInventory().add(itemstack)) {
                p_262636_.drop(itemstack, false);
            }

            p_262654_.gameEvent(p_262636_, GameEvent.BLOCK_CHANGE, p_262601_);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_250440_, BlockState p_248729_) {
        return new ChiseledBookShelfBlockEntity(p_250440_, p_248729_);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_250973_) {
        p_250973_.add(HorizontalDirectionalBlock.FACING);
        SLOT_OCCUPIED_PROPERTIES.forEach(p_261456_ -> p_250973_.add(p_261456_));
    }

    @Override
    protected void onRemove(BlockState p_250071_, Level p_251485_, BlockPos p_251954_, BlockState p_251852_, boolean p_252250_) {
        if (!p_250071_.is(p_251852_.getBlock())) {
            if (p_251485_.getBlockEntity(p_251954_) instanceof ChiseledBookShelfBlockEntity chiseledbookshelfblockentity
                && !chiseledbookshelfblockentity.isEmpty()) {
                for (int i = 0; i < 6; i++) {
                    ItemStack itemstack = chiseledbookshelfblockentity.getItem(i);
                    if (!itemstack.isEmpty()) {
                        Containers.dropItemStack(p_251485_, (double)p_251954_.getX(), (double)p_251954_.getY(), (double)p_251954_.getZ(), itemstack);
                    }
                }

                chiseledbookshelfblockentity.clearContent();
                p_251485_.updateNeighbourForOutputSignal(p_251954_, this);
            }

            super.onRemove(p_250071_, p_251485_, p_251954_, p_251852_, p_252250_);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_251318_) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, p_251318_.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState p_288975_, Rotation p_288993_) {
        return p_288975_.setValue(HorizontalDirectionalBlock.FACING, p_288993_.rotate(p_288975_.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    public BlockState mirror(BlockState p_289000_, Mirror p_288962_) {
        return p_289000_.rotate(p_288962_.getRotation(p_289000_.getValue(HorizontalDirectionalBlock.FACING)));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState p_249302_) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState p_249192_, Level p_252207_, BlockPos p_248999_) {
        if (p_252207_.isClientSide()) {
            return 0;
        } else {
            return p_252207_.getBlockEntity(p_248999_) instanceof ChiseledBookShelfBlockEntity chiseledbookshelfblockentity
                ? chiseledbookshelfblockentity.getLastInteractedSlot() + 1
                : 0;
        }
    }
}
