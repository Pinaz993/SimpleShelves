package net.pinaz993.simpleshelves;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A block entity for shelves. Only contains methods that could not be implemented in ShelfInventory. Pretty much all
 * inventory stuff lives over there.
 */

public class ShelfBlockEntity extends BlockEntity implements ShelfInventory {

    //<editor-fold desc="Standard Inventory Boilerplate">

    // The items that are in the inventory.
    DefaultedList<ItemStack> items;

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleShelves.SHELF_BLOCK_ENTITY, pos, state);
        // Initialize the list of items that are stored in this inventory.
        this.items = DefaultedList.ofSize(16, ItemStack.EMPTY);
    }

    // Item getter provided because I couldn't figure out a way to implement the item field in ShelfInventory, but that
    // class still needs to refer to the list. This is one instance where Java causes a little bit of bloat. If I could
    // extend multiple classes, I wouldn't need a separate block entity class, and thus I'd be able to just reference
    // the field.
    @Override
    public DefaultedList<ItemStack> getItems() {return items;}

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, items);
        return super.writeNbt(nbt);
    }
    //</editor-fold>

    // Lifted almost directly from BlockEntity. We can't get a World object from in ShelfInventory, so we have to
    // implement marking dirty here.
    @Override
    public void markDirty() {if(this.world != null) markDirtyInWorld(this.world, this.pos, this.getCachedState());}

    // In BlockEntity, this method has the same name as the one above. Java doesn't want me to override that, as it's
    // 'pRoTeCtEd'. Bah!
    /**
     * Tell the world that the inventory changed, so that inventory monitoring blocks/entities can be notified.
     */
    protected void markDirtyInWorld(World world, BlockPos pos, BlockState state){
        // TODO: Implement inventory validation.
        // Iterate through all block positions, updating state iff needed.
        for(BookPosition bpos: BookPosition.class.getEnumConstants()){
            // What is the state now?
            boolean oldState = state.get(bpos.BLOCK_STATE_PROPERTY);
            // Is the associated slot empty?
            boolean newState = !this.getItems().get(bpos.SLOT).isEmpty();
            // If the old state is different than the new state, tell the world to update the state to the new one.
            // I don't just update all of them because I don't know how intensive that is, and I don't want to lag.
            if(oldState != newState) world.setBlockState(pos, state.with(bpos.BLOCK_STATE_PROPERTY, newState));
        }
        // Super calls World.markDirty() and possibly World.updateComparators().
        markDirty(world, pos, state);
    }
}

