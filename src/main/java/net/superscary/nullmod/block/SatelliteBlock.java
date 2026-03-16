package net.superscary.nullmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import net.superscary.nullmod.block.entity.QuarryFrameBlockEntity;
import net.superscary.nullmod.block.entity.SatelliteBlockEntity;
import net.superscary.nullmod.api.block.base.BaseBlock;
import net.superscary.nullmod.menu.QuarryMenu;
import net.superscary.nullmod.registries.NBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SatelliteBlock extends BaseBlock implements EntityBlock {

    private static final MutableComponent TITLE = Component.translatable("menu.nullmod.quarry");

    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public SatelliteBlock() {
        super(Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.IRON_BLOCK));
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var clickedPos = context.getClickedPos().relative(context.getClickedFace().getOpposite());
        if (context.getLevel().getBlockState(clickedPos).is(NBlocks.QUARRY_FRAME.getBlock())) {
            return this.defaultBlockState().setValue(FACING, context.getClickedFace());
        }
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SatelliteBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            revalidateController(level, pos, state);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide) {
            revalidateController(level, pos, state);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            revalidateController(level, pos, state);
        }
    }

    private static void revalidateController(Level level, BlockPos pos, BlockState state) {
        if (!state.hasProperty(FACING)) return;
        Direction facing = state.getValue(FACING);
        BlockPos framePos = pos.relative(facing.getOpposite());
        if (level.getBlockEntity(framePos) instanceof QuarryFrameBlockEntity frame) {
            frame.revalidateMultiblock();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos controllerPos = null;
            if (level.getBlockEntity(pos) instanceof SatelliteBlockEntity satellite) {
                controllerPos = satellite.getControllerPos();
            }
            if (controllerPos != null) {
                BlockPos finalControllerPos = controllerPos;
                MenuProvider provider = new SimpleMenuProvider(
                        (containerId, inv, p) -> new QuarryMenu(containerId, inv, level, finalControllerPos),
                        TITLE
                );
                ((IPlayerExtension) serverPlayer).openMenu(provider, buf -> buf.writeBlockPos(finalControllerPos));
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

}
