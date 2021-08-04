package net.pinaz993.simpleshelves;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ItemTags {
    public static final Tag<Item> BOOK_LIKE = TagRegistry.item(new Identifier("simple_shelves","book_like"));
}
