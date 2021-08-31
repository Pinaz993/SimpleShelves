package net.pinaz993.simpleshelves;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * This interface needs to be different from the default inventory in several ways.
 * 1. The first 12 slots need to be whitelisted to only accept book-like items.
 *    That includes (minecraft):lectern_books, :book, :enchanted_book and simple_shelves:redstone_book.
 * 2. The last 4 slots will be for generic items, and will be assigned one per quadrant.
 *
 * I made the (perhaps unadvised) decision to implement Inventory myself instead of using Juuz's fine generic
 * implementation. I'm also packing it with things that the tutorial puts in the block entity itself, because I want to
 * handle all the inventory stuff in one place. I'm on my own here, and it's a little scary, but fun.
 */
public interface ShelfInventory extends SidedInventory {

    //<editor-fold desc="Shelf Inventory Descriptors">
    /**
     * Uses an ItemTag to differentiate between what should and should not be treated like a book, and thus
     * what can and cannot be added to a book-like slot.
     * @param stack the ItemStack in question
     * @return if the ItemStack in question is book-like.
     */
    static boolean isBookLike(ItemStack stack){return ItemTags.BOOK_LIKE.contains(stack.getItem());}

    /**
     * This inventory has 16 slots.
     */
    @Override
    default int size(){return getItems().size();}

    /**
     * Slots 0 - 11 are set aside for book-like items. They are accessible to other blocks from any side.
     * @return array of ints containing valid slot numbers for book-like slots.
     */
    static int[] getBookSlots(){return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};}

    /**
     * Slots 12-15 are set aside for generic item stacks. They are not accessible to other blocks.
     * @return array of ints containing valid slot numbers for generic item slots.
     */
    static int[] getGenericSlots(){return new int[]{12, 13, 14, 15};}
    //</editor-fold>\

    // The items in this inventory. I'm making this a
    DefaultedList<ItemStack> getItems();

    /**
     * Is this inventory empty?
     */
    @Override
    default boolean isEmpty() {
        // Iterate through items. If any ItemStack isn't empty, return false.
        for (ItemStack s: getItems()) if (!s.isEmpty()) return false;
        // Else, return true.
        return true;
    }

    @Override
    default void clear() {getItems().clear();}

    //<editor-fold desc="Slot Stack Manipulation">
    /**
     * Can this ItemStack go into this slot?
     */
    @Override
    default boolean isValid(int slot, ItemStack stack) {
        // If slot is book-like, defer to if the stack is book-like.
        for (int i: getBookSlots()) if(slot == i) return isBookLike(stack);
        // If slot is in the generic slots, then yes.
        for (int i: getGenericSlots()) if(slot == i) return true;
        // If not in either, throw an exception.
        throw new IllegalArgumentException(String.format("Invalid slot index for shelf: %s", slot));
    }

    /**
     * Give me the stack that is in this slot.
     */
    @Override
    default ItemStack getStack(int slot) {return getItems().get(slot);}

    /**
     * Replace the stack in this slot with this stack. Mark the block dirty.
     */
    @Override
    default void setStack(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        // I don't think this bit applies to me, but I'm putting it here anyway.
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    /**
     * Remove the stack that is in this slot and give it to me. If the stack isn't empty, mark the block dirty.
     */
    @Override
    default ItemStack removeStack(int slot) {
        ItemStack rtn = Inventories.removeStack(getItems(), slot);
        if(!rtn.isEmpty()) markDirty();
        return rtn;
    }

    /**
     * Remove up to this many items from the stack in that slot and give them to me as a new stack. If you did something,
     * mark the block as dirty.
     */
    @Override
    default ItemStack removeStack(int slot, int amount) {
        ItemStack rtn = Inventories.splitStack(getItems(), slot, amount);
        if (!rtn.isEmpty()) markDirty();
        return rtn;
    }
    //</editor-fold>


    /**
     * If this player aims at and attempts to use this block, can they use it? Should the block be highlighted?
     */
    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true; // Yup.
    }
    default boolean quadrantHasBook(ShelfQuadrant quadrant){
        switch (quadrant){
            // For each quadrant, return true if one or more of the book slots are full.
            case ALPHA -> {return !getItems().get(BookPosition.ALPHA_1.SLOT).isEmpty()
                    && !getItems().get(BookPosition.ALPHA_2.SLOT).isEmpty()
                    && !getItems().get(BookPosition.ALPHA_3.SLOT).isEmpty();}
            case BETA -> {return !getItems().get(BookPosition.BETA_1.SLOT).isEmpty()
                    && !getItems().get(BookPosition.BETA_2.SLOT).isEmpty()
                    && !getItems().get(BookPosition.BETA_3.SLOT).isEmpty();}
            case GAMMA -> {return !getItems().get(BookPosition.GAMMA_1.SLOT).isEmpty()
                    && !getItems().get(BookPosition.GAMMA_2.SLOT).isEmpty()
                    && !getItems().get(BookPosition.GAMMA_3.SLOT).isEmpty();}
            case DELTA -> {return !getItems().get(BookPosition.DELTA_1.SLOT).isEmpty()
                    && !getItems().get(BookPosition.DELTA_2.SLOT).isEmpty()
                    && !getItems().get(BookPosition.DELTA_3.SLOT).isEmpty();}
            default -> throw new IllegalArgumentException(String.format("Invalid Quadrant: %s", quadrant));
        }
    }
    //<editor-fold desc="Sided Inventory Implementation">
    /**
     * What slots can we insert into and extract from?
     */
    @Override
    default int[] getAvailableSlots(Direction side){
        // Just the book slots, sir. Side is irrelevant.
        return getBookSlots();
    }

    /**
     * Can I insert this item in this slot, possibly from a particular side?
     */
    @Override
    default boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
        // If the item isn't book-like, then the answer is no.
        if(!isBookLike(stack)) return false;
        // Slot has to be in the book-like slots to be inserted into. What a one-liner!
        else for (int i: getBookSlots()) if(slot == i) return true;
        // If all else fails, then no, you cannot.
        return false;
    }

    /**
     * Can I extract this item from this slot, from a particular side?
     */
    @Override
    default boolean canExtract(int slot, ItemStack stack, Direction dir){
        // Only if slot is a book-like slot.
        for (int i: getBookSlots()) if(slot == i) return true;
        // Else, no.
        return false;
    }
    //</editor-fold>

}
