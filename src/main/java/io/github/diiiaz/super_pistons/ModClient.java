package io.github.diiiaz.super_pistons;

import io.github.diiiaz.super_pistons.client.SuperPistonBlockEntityRenderer;
import io.github.diiiaz.super_pistons.entity.ModBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class ModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModBlockEntities.SUPER_PISTON, SuperPistonBlockEntityRenderer::new);
    }
}
