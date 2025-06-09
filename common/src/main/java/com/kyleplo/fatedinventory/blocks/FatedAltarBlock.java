package com.kyleplo.fatedinventory.blocks;

import org.jetbrains.annotations.Nullable;

import com.kyleplo.fatedinventory.FatedInventory;
import com.kyleplo.fatedinventory.IFatedInventoryContainer;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FatedAltarBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<FatedAltarBlock> CODEC = simpleCodec(FatedAltarBlock::new);
    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 11.0, 15.0),
            Block.box(3.0, 11.0, 3.0, 13.0, 22.0, 13.0));
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty CHARGE = IntegerProperty.create("charges", 0, 4);
    public static final TagKey<Item> CHARGES_FATED_ALTAR = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(FatedInventory.MOD_ID, "charges_fated_altar"));
    public static final TagKey<Item> SHEARS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "tools/shear"));

    public MapCodec<FatedAltarBlock> codec() {
        return CODEC;
    }

    public FatedAltarBlock(Properties properties) {
        super(properties
                .pushReaction(PushReaction.BLOCK)
                .requiresCorrectToolForDrops()
                .explosionResistance(1200f)
                .destroyTime(25f)
                .mapColor(MapColor.STONE)
                .strength(5f));
        this.registerDefaultState(
                this.stateDefinition.any().setValue(WATERLOGGED, false).setValue(CHARGE, 0));
    }

    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos,
            CollisionContext collisionContext) {
        return SHAPE;
    }

    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER).setValue(CHARGE, 0);
    }

    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level,
            BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (FatedInventory.config.fatedAltarRequiresCharges && itemStack.is(FatedAltarBlock.CHARGES_FATED_ALTAR)
                && (Integer) blockState.getValue(CHARGE) < 4) {
            charge(player, level, blockPos, blockState);
            itemStack.consume(1, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else if (itemStack.is(FatedAltarBlock.SHEARS)) {
            IFatedInventoryContainer fatedInventory = FatedInventory.getFatedInventoryContainer(player);
            fatedInventory.clearFatedInventory();
            itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            level.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                        FatedInventoryBlocks.FATED_ALTAR_FATE_CUT, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    public static void charge(@Nullable Entity entity, Level level, BlockPos blockPos, BlockState blockState) {
        BlockState newState = blockState.setValue(CHARGE, blockState.getValue(CHARGE) + 1);
        level.setBlock(blockPos, newState, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, Context.of(entity, newState));
        level.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), FatedInventoryBlocks.FATED_ALTAR_CHARGE,
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player,
            BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            IFatedInventoryContainer fatedInventory = FatedInventory.getFatedInventoryContainer(player);
            if (blockState.getValue(CHARGE) > 0 || !FatedInventory.config.fatedAltarRequiresCharges) {
                if (fatedInventory.hasStored()) {
                    fatedInventory.dropInventoryFor(player);

                    if (blockState.getValue(CHARGE) == 1 && FatedInventory.config.fatedAltarRequiresCharges && FatedInventory.config.runningOutOfChargesClearsFate) {
                        fatedInventory.clearFatedInventory();
                    }
                    
                    level.setBlock(blockPos, blockState.setValue(CHARGE, Math.max(0, blockState.getValue(CHARGE) - 1)), 3);
                    level.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                            FatedInventoryBlocks.FATED_ALTAR_DEPLETE, SoundSource.BLOCKS, 1.0F, 1.0F);
                } else {
                    fatedInventory.putInventory(player);

                    player.displayClientMessage(Component.translatable("gui.fated_inventory.fated_altar.fated_sealed"),
                            false);
                    level.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                            FatedInventoryBlocks.FATED_ALTAR_FATE_SEALED, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            } else {
                player.displayClientMessage(Component.translatable("gui.fated_inventory.fated_altar.needs_charge"),
                        false);
            }
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[] { WATERLOGGED, CHARGE });
    }

    protected FluidState getFluidState(BlockState blockState) {
        return (Boolean) blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false)
                : super.getFluidState(blockState);
    }

    protected BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2,
            LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if ((Boolean) blockState.getValue(WATERLOGGED)) {
            levelAccessor.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    public static int getScaledChargeLevel(BlockState blockState, int i) {
        return Mth.floor((float) ((Integer) blockState.getValue(CHARGE) - 0) / 4.0F * (float) i);
    }

    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return getScaledChargeLevel(blockState, 15);
    }
}
