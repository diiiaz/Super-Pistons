package io.github.diiiaz.super_pistons.block;

import io.github.diiiaz.super_pistons.Mod;
import io.github.diiiaz.super_pistons.block.piston.SuperPistonBlock;
import io.github.diiiaz.super_pistons.block.piston.SuperPistonExtensionBlock;
import io.github.diiiaz.super_pistons.block.piston.SuperPistonHeadBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ModBlocks {

    public static final Block SUPER_PISTON = registerBlockItem("super_piston", ModBlocks.createSuperPistonBlock(false));
    public static final Block SUPER_STICKY_PISTON = registerBlockItem("super_sticky_piston", ModBlocks.createSuperPistonBlock(true));

    public static final Block SUPER_PISTON_HEAD = registerBlock("super_piston_head", new SuperPistonHeadBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(1.5f).dropsNothing().pistonBehavior(PistonBehavior.BLOCK)));
    public static final Block MOVING_SUPER_PISTON = registerBlock("moving_super_piston", new SuperPistonExtensionBlock(AbstractBlock.Settings.create().strength(-1.0f).dynamicBounds().dropsNothing().nonOpaque().solidBlock(ModBlocks::never).suffocates(ModBlocks::never).blockVision(ModBlocks::never)));

    private static Block registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(Mod.ID, name), new BlockItem(block, new FabricItemSettings()));
        return registerBlock(name, block);
    }

    private static Block registerBlock(String name, Block block) {
        return Registry.register(Registries.BLOCK, new Identifier(Mod.ID, name), block);
    }

    private static SuperPistonBlock createSuperPistonBlock(boolean sticky) {
        AbstractBlock.ContextPredicate contextPredicate = (state, world, pos) -> !state.get(SuperPistonBlock.EXTENDED);
        return new SuperPistonBlock(sticky,
                AbstractBlock.Settings.create()
                        .mapColor(MapColor.TERRACOTTA_ORANGE)
                        .strength(1.5f)
                        .solidBlock(Blocks::never)
                        .suffocates(contextPredicate)
                        .blockVision(contextPredicate)
                        .pistonBehavior(PistonBehavior.BLOCK));
    }

    public static Boolean never(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

}
