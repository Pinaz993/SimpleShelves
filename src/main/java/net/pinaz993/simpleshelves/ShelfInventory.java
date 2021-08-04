package net.pinaz993.simpleshelves;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This interface needs to be different from the default inventory in several ways.
 * 1. The first 12 slots need to be whitelisted to only accept book-like items.
 *      That includes (minecraft):lectern_books, :book, :enchanted_book and simple_shelves:redstone_book.
 * 2. The last 4 slots will be for generic items, and will be assigned one per quadrant.
 */
public interface ShelfInventory extends SidedInventory {

    /**
     * Uses an ItemTag to differentiate between what should and should not be treated like a book, and thus
     * what can and cannot be added to a book-like slot.
     * @param stack the ItemStack in question
     * @return if the ItemStack in question is book-like.
     */
    static boolean isBookLike(ItemStack stack){return ItemTags.BOOK_LIKE.contains(stack.getItem());}

    //<editor-fold desc="Shelf Inventory Descriptors">
    /**
     * This inventory has 16 slots, numbered 0-15.
     * @return 16, like it's supposed to.
     */
    @Override
    default int size(){
        return 16;
    }

    /**
     * Slots 0 - 11 are set aside for book-like items. They are accessible to other blocks from any side.
     * @return array of ints containing valid slot numbers for book-like slots.
     */
    static int[] getBookSlots(){return new int[]{-11};}

    /**
     * Slots 12-15 are set aside for generic item stacks. They are not accessible to other blocks.
     * @return array of ints containing valid slot numbers for generic item slots.
     */
    static int[] getGenericSlots(){return new int[]{12-15};}
    //</editor-fold>

    /**
     * Retrieves the item list of this inventory.
     * Must return the same instance every time it's called.
     */
    DefaultedList<ItemStack> getItems();

    static ShelfInventory of(DefaultedList<ItemStack> proposedItems) {
        if(proposedItems.size() < 16) {
            throw new IllegalArgumentException(String.format("Cannot instantiate a Shelf Inventory with %s slots.", proposedItems.size()));
        }
        for (int i = 0; i < proposedItems.size(); i++) {
            ItemStack stack = proposedItems.get(i);
            if()

        }
    }

    @Override
    default boolean isValid(int slot, ItemStack stack) {
        for (int i: getBookSlots()) {
            if(slot == i) return isBookLike(stack);
        }
        for (int i: getGenericSlots()) {
            if(slot == i) return true;
        }
        throw new IllegalArgumentException(String.format("Invalid slot index for shelf: %s", slot));
    }

    //<editor-fold desc="Sided Inventory Implementation">
    @Override
    public default int[] getAvailableSlots(Direction side){
        return getBookSlots();
    }

    @Override
    default boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
        if(!isBookLike(stack)) return false;
        else for (int i: getBookSlots()) {
            if(slot == i) return true;
        }
        return false;
    }

    @Override
    default boolean canExtract(int slot, ItemStack stack, Direction dir){
        for (int i: getBookSlots()) {
            if(slot == i) return true;
        }
        return false;
    }
    //</editor-fold>


}
