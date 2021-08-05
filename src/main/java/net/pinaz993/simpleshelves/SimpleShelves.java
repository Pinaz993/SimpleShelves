package net.pinaz993.simpleshelves;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SimpleShelves implements ModInitializer {
    public static final OakShelf OAK_SHELF = new OakShelf(FabricBlockSettings.of(Material.WOOD).strength(1.5f, 1.5f));
    public static final BirchShelf BIRCH_SHELF = new BirchShelf(FabricBlockSettings.of(Material.WOOD).strength(1.5f, 1.5f));
    public static BlockEntityType<ShelfBlockEntity> SHELF_BLOCK_ENTITY;

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("simple_shelves", "oak_shelf"), OAK_SHELF);
        Registry.register(Registry.BLOCK, new Identifier("simple_shelves", "birch_shelf"), BIRCH_SHELF);

        Registry.register(Registry.ITEM, new Identifier("simple_shelves", "oak_shelf"), new BlockItem(OAK_SHELF, new FabricItemSettings().group(ItemGroup.DECORATIONS)));
        Registry.register(Registry.ITEM, new Identifier("simple_shelves", "birch_shelf"), new BlockItem(BIRCH_SHELF, new FabricItemSettings().group(ItemGroup.DECORATIONS)));

        SHELF_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "simple_shelves:shelf_block_entity", FabricBlockEntityTypeBuilder.create(ShelfBlockEntity::new, OAK_SHELF, BIRCH_SHELF).build(null));
    }
}