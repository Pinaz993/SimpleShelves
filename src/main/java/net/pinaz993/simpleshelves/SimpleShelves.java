package net.pinaz993.simpleshelves;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SimpleShelves implements ModInitializer {
    public static final OakShelf OAK_SHELF = new OakShelf(FabricBlockSettings.of(Material.WOOD).strength(1.5f, 1.5f));

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("simple_shelves", "oak_shelf"), OAK_SHELF);
        Registry.register(Registry.ITEM, new Identifier("simple_shelves", "oak_shelf"), new BlockItem(OAK_SHELF, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
    }
}