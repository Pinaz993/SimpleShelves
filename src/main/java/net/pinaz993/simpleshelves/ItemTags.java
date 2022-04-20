package net.pinaz993.simpleshelves;


import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemTags {
    public static final TagKey<Item> BOOK_LIKE = TagKey.of(Registry.ITEM_KEY, new Identifier("simple_shelves", "book_like"));
}
