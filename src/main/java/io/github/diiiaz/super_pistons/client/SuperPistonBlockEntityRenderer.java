package io.github.diiiaz.super_pistons.client;

import io.github.diiiaz.super_pistons.block.ModBlocks;
import io.github.diiiaz.super_pistons.block.piston.SuperPistonBlockEntity;
import io.github.diiiaz.super_pistons.block.piston.SuperPistonHeadBlock;
import io.github.diiiaz.super_pistons.block.piston.SuperPistonType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

@Environment(value= EnvType.CLIENT)
public class SuperPistonBlockEntityRenderer implements BlockEntityRenderer<SuperPistonBlockEntity> {
    private final BlockRenderManager manager;
    private final BlockEntityRenderDispatcher dispatcher;


    public SuperPistonBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.manager = ctx.getRenderManager();
        this.dispatcher = ctx.getRenderDispatcher();
    }

    @Override
    public void render(SuperPistonBlockEntity superPistonBlockEntity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int overlay) {
        World world = superPistonBlockEntity.getWorld();
        if (world == null) {
            return;
        }
        BlockPos blockPos = superPistonBlockEntity.getPos().offset(superPistonBlockEntity.getMovementDirection().getOpposite());
        BlockState blockState = superPistonBlockEntity.getPushedBlock();
        if (blockState.isAir()) {
            return;
        }
        BlockModelRenderer.enableBrightnessCache();
        matrixStack.push();
        matrixStack.translate(
                superPistonBlockEntity.getRenderOffsetX(tickDelta),
                superPistonBlockEntity.getRenderOffsetY(tickDelta),
                superPistonBlockEntity.getRenderOffsetZ(tickDelta));

        if (blockState.isOf(ModBlocks.SUPER_PISTON_HEAD) && superPistonBlockEntity.getProgress(tickDelta) <= 4.0f) {
            blockState = (BlockState)blockState.with(SuperPistonHeadBlock.SHORT, superPistonBlockEntity.getProgress(tickDelta) <= 0.5f);
            this.renderModel(blockPos, blockState, matrixStack, vertexConsumerProvider, world, false, overlay);

        } else if (superPistonBlockEntity.isSource() && !superPistonBlockEntity.isExtending()) {
            SuperPistonType pistonType = blockState.isOf(ModBlocks.SUPER_PISTON) ? SuperPistonType.DEFAULT : SuperPistonType.STICKY;
            BlockState blockState2 = (BlockState)((BlockState)ModBlocks.SUPER_PISTON_HEAD.getDefaultState().with(SuperPistonHeadBlock.TYPE, pistonType)).with(SuperPistonHeadBlock.FACING, blockState.get(PistonBlock.FACING));
            blockState2 = (BlockState)blockState2.with(SuperPistonHeadBlock.SHORT, superPistonBlockEntity.getProgress(tickDelta) >= 0.5f);
            this.renderModel(blockPos, blockState2, matrixStack, vertexConsumerProvider, world, false, overlay);
            BlockPos blockPos2 = blockPos.offset(superPistonBlockEntity.getMovementDirection());
            matrixStack.pop();
            matrixStack.push();
            blockState = (BlockState)blockState.with(PistonBlock.EXTENDED, true);
            this.renderModel(blockPos2, blockState, matrixStack, vertexConsumerProvider, world, true, overlay);
        } else {
            this.renderModel(blockPos, blockState, matrixStack, vertexConsumerProvider, world, false, overlay);
            if (!superPistonBlockEntity.isRenderModeSet()) {
                superPistonBlockEntity.setRenderPushedBlockEntity(superPistonBlockEntity.getPushedBlockEntity() != null);
            }
        }

        // some of the code here is copied and modified from the carpet mod : https://modrinth.com/mod/carpet
        // renders the block entity moving with the piston if there is any.
        if (superPistonBlockEntity.getRenderPushedBlockEntity())
        {
            BlockEntity pushedBlockEntity = superPistonBlockEntity.getPushedBlockEntity();
            if (pushedBlockEntity != null)
            {
                dispatcher.render(pushedBlockEntity, tickDelta, matrixStack, vertexConsumerProvider);
            }
        }

        matrixStack.pop();
        BlockModelRenderer.disableBrightnessCache();
    }

    private void renderModel(BlockPos pos, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, World world, boolean cull, int overlay) {
        RenderLayer renderLayer = RenderLayers.getMovingBlockLayer(state);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
        this.manager.getModelRenderer().render(world, this.manager.getModel(state), state, pos, matrices, vertexConsumer, cull, Random.create(), state.getRenderingSeed(pos), overlay);
    }

    @Override
    public int getRenderDistance() {
        return 68;
    }
}

