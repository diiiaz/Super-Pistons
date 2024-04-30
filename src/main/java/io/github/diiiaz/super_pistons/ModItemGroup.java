package io.github.diiiaz.super_pistons;

import io.github.diiiaz.super_pistons.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class ModItemGroup {


    public static final RegistryKey<ItemGroup> ITEM_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Mod.ID, "super_pistons"));

    public static void register() {
        // create custom item group and add mod blocks to it
        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP,
			FabricItemGroup.builder()
					.displayName(Text.translatable("item_group.super-pistons"))
					.icon(ModBlocks.SUPER_PISTON.asItem()::getDefaultStack)
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.SUPER_PISTON);
                        entries.add(ModBlocks.SUPER_STICKY_PISTON);
                    })
					.build());

        // add pistons to the redstone tab after corresponding pistons
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
            content.addAfter(Items.PISTON, ModBlocks.SUPER_PISTON);
            content.addAfter(Items.STICKY_PISTON, ModBlocks.SUPER_STICKY_PISTON);
        });

    }




}
