package net.pinaz993.simpleshelves;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class ItemTags {
    public static final Tag<Item> BOOK_LIKE = TagFactory.ITEM.create(new Identifier("simple_shelves", "book_like"));
}
