package io.github.diiiaz.super_pistons.entity;

import io.github.diiiaz.super_pistons.Mod;
import io.github.diiiaz.super_pistons.block.ModBlocks;
import io.github.diiiaz.super_pistons.block.piston.SuperPistonBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {

    public static BlockEntityType<SuperPistonBlockEntity> SUPER_PISTON;

    public static void registerAllBlockEntities() {
        SUPER_PISTON = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(Mod.ID, "piston"),
                FabricBlockEntityTypeBuilder.create(SuperPistonBlockEntity::new, ModBlocks.MOVING_SUPER_PISTON).build(null));
    }


}