package net.pinaz993.simpleshelves;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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
    // Tags are wierd now. You have to get the registry entry of the item stack, and then you can ask if it's in a tag.
    static boolean isBookLike(ItemStack stack){return stack.getRegistryEntry().isIn(ItemTags.BOOK_LIKE);}

    /**
     * This inventory has 16 slots. That is defined in ShelfBlockEntity.
     */
    @Override
    default int size(){return getItems().size();}

    /**
     * Slots 0 - 11 are set aside for book-like items. They are accessible to other blocks from any side.
     * Defined in terms of the enum fields because magic numbers bad.
     * @return array of ints containing valid slot numbers for book-like slots.
     */
    static int[] getBookSlots(){return new int[]{
            BookPosition.ALPHA_1.SLOT,
            BookPosition.ALPHA_2.SLOT,
            BookPosition.ALPHA_3.SLOT,
            BookPosition.BETA_1.SLOT,
            BookPosition.BETA_2.SLOT,
            BookPosition.BETA_3.SLOT,
            BookPosition.GAMMA_1.SLOT,
            BookPosition.GAMMA_2.SLOT,
            BookPosition.GAMMA_3.SLOT,
            BookPosition.DELTA_1.SLOT,
            BookPosition.DELTA_2.SLOT,
            BookPosition.DELTA_3.SLOT,
    };}

    /**
     * Slots 12-15 are set aside for generic item stacks. They are not accessible to other blocks.
     * Defined in terms of the enum fields because magic numbers bad.
     * @return array of ints containing valid slot numbers for generic item slots.
     */
    static int[] getGenericSlots(){return new int[]{
            ShelfQuadrant.ALPHA.GENERIC_ITEM_SLOT,
            ShelfQuadrant.BETA.GENERIC_ITEM_SLOT,
            ShelfQuadrant.GAMMA.GENERIC_ITEM_SLOT,
            ShelfQuadrant.DELTA.GENERIC_ITEM_SLOT
    };}
    //</editor-fold>\

    /**
     * Get the list of items in this inventory. Using a getter because I can't initialize the field here, because Java.
     */
    DefaultedList<ItemStack> getItems();

    /**
     * Is this inventory empty?
     * This could be done faster with a hash comparison, but I don't think it's worth the effort.
     */
    @Override
    default boolean isEmpty() {
        // Iterate through items. If any ItemStack isn't empty, return false.
        for (ItemStack s: getItems()) if (!s.isEmpty()) return false;
        // Else, return true.
        return true;
    }

    /**
     * Make this inventory empty.
     */
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
     * Attempts to insert the given ItemStack into the given inventory slot. If the slot isn't occupied, inserts the
     * entire ItemStack into the slot and returns an empty ItemStack.
     * If the slot is occupied by a stack that isn't full of items and is the same kind of item as the new stack, it
     * inserts as many of the items as it can into the stack, and returns either the amount of items that are left, or
     * an empty stack if they all go in.
     * If the slot is occupied by an incompatible stack or a full stack, it does nothing and returns the new stack.
     * @param slot The slot to insert into.
     * @param newStack The stack to insert.
     * @return Any leftover items, or an empty stack.
     */
    default ItemStack attemptInsertion(int slot, ItemStack newStack){
        ItemStack oldStack = getStack(slot);
        // Is the inventory slot empty?
        if(oldStack.isEmpty()){
            // If so, put the new stack in the inventory slot and return an empty stack.
            getItems().set(slot, newStack);
            // We changed items. Update the looks of the block.
            markDirty();
            return ItemStack.EMPTY;
        }
        // Is the is the old stack stackable, of the same Item as the new stack, and less than full??
        else if (oldStack.isStackable() && oldStack.isItemEqual(newStack) && oldStack.getCount() < oldStack.getMaxCount()) {
            // If so, try to insert the new stack on top of the old stack.
            // Can the new stack completely stack with the old stack?
            if (newStack.getCount() <= oldStack.getMaxCount() - oldStack.getCount()) {
                // If so, combine the two stacks and return an empty stack.
                oldStack.setCount(oldStack.getCount() + newStack.getCount());
                // We changed items. Update the looks of the block.
                markDirty();
                return ItemStack.EMPTY;
            }
            // No? We must have too many items in the new stack to fit in the old stack.
            else {
                // How many items can we fit in the old stack?
                int diff = oldStack.getMaxCount() - oldStack.getCount();
                // Subtract that from the new stack.
                newStack.setCount(newStack.getCount() - diff);
                // The old stack is now full.
                oldStack.setCount(oldStack.getMaxCount());
                // return what's left of the new stack.
                return newStack;
            }

        }
        // Otherwise, we can't insert at all. Return the stack that can't be inserted.
        else return newStack;
    }

    /**
     * Give me the stack that is in this slot.
     * Not sure if this should be returning a copy or the original. I'll stick with the original for now,
     * as I can always take a copy.
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
    default boolean canPlayerUse(PlayerEntity player) {return true; /* Yup. */}

    /**
     * Does the given quadrant have any books in it?
     */
    default boolean quadrantHasBook(ShelfQuadrant quadrant){
        // Iterate through all book positions in the quadrant. If any of them aren't empty, return true.
        for (BookPosition bpos: quadrant.BOOK_POSITIONS) if (!getItems().get(bpos.SLOT).isEmpty()) return true;
        // Otherwise, return false.
        return false;
    }

    /**
     * Does the given quadrant have a generic item in it?
     */
    default boolean quadrantHasGenericItem(ShelfQuadrant quadrant){
        return !getStack(quadrant.GENERIC_ITEM_SLOT).isEmpty();
    }

    /**
     * Does the shelf have any generic items?
     */
    default boolean shelfHasGenericItem(){
        // Iterate through all four quadrants. If any of them possess generic items, return true.
        for(ShelfQuadrant q: ShelfQuadrant.class.getEnumConstants()) if (quadrantHasGenericItem(q)) return true;
        // If you don't get any hits, return false.
        return false;
    }

    //<editor-fold desc="Sided Inventory Implementation">
    /**
     * What slots can we insert into and extract from?
     */
    @Override
    default int[] getAvailableSlots(Direction side){return getBookSlots();}

    /**
     * Can I insert this item in this slot, possibly from a particular side?
     */
    @Override
    default boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir){
        // If the item isn't book-like, then the answer is no.
        if(!isBookLike(stack)) return false;
        // Iterate over all book positions in all quadrants. If the given slot belongs to the current book position,
        // defer to if the quadrant that book lives in already has a generic item.
        // This isn't the most efficient way to do this, I know. I don't like it all that much, TBH.
        // But short of writing out a 12-entry if-else, there's no way I can do this without magic numbers.
        // Also, I really like the fact that you can nest iterators in a single line like this.
        for (ShelfQuadrant q: ShelfQuadrant.class.getEnumConstants()) for (BookPosition bp: q.BOOK_POSITIONS) {
            if(slot == bp.SLOT) return !quadrantHasGenericItem(q);
        }
        // If all else fails, return false.
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

    /**
     * Given the inventory in this object, returns an int between 0 and 15 inclusive, indicating how full the inventory is.
     * Considers only book-like slots. It would be possible to disregard quadrants that have generic items, but I'm on
     * the fence as to whether to do that.
     */
    default int getComparatorOutput(){
        float c = 0; // Counter
        for(BookPosition bp: BookPosition.class.getEnumConstants()){ // For each possible book position...
            ItemStack stack = getStack(bp.SLOT); // Grab the ItemStack in the associated slot
            // Get the fullness ratio of that slot and add it to the counter.
            c += (float)stack.getCount() / (float)Math.min(getMaxCountPerStack(), stack.getMaxCount());
        }
        // Divide the counter by the number of possible slots to get the fullness ratio for the entire shelf.
        c /= getBookSlots().length;
        // Return the fullness ratio multiplied by 14, then rounded down, then incremented by one.
        // e.g.: c = .95 (shelf is almost full of books) -> 1 * 14 (13.3) floored (13) + 1 = 14.
        // Rounding down ensures that only full inventories produce the strongest signals.
        // Adding one ensures that only empty inventories fail to output a signal at all.
        return MathHelper.floor(c * 14f) + 1;
    }
    //</editor-fold>

}
