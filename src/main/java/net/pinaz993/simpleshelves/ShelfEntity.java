package net.pinaz993.simpleshelves;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
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

public class ShelfEntity extends BlockEntity implements ShelfInventory, BlockEntityClientSerializable {

    DefaultedList<ItemStack> items;    // The items that are in the inventory.
    boolean hasGenericItems;   // Since I don't want to query the inventory every frame, let's set this if the block is
                               // dirty, and refer to this every frame, which should be faster.

    public ShelfEntity(BlockPos pos, BlockState state) {
        super(SimpleShelves.SHELF_BLOCK_ENTITY, pos, state);
        // Initialize the list of items that are stored in this inventory.
        this.items = DefaultedList.ofSize(16, ItemStack.EMPTY);
        this.hasGenericItems = false;
    }

    // Item getter provided because I couldn't figure out a way to implement the item field in ShelfInventory, but that
    // class still needs to refer to the list. This is one instance where Java causes a little bit of bloat. If I could
    // extend multiple classes, I wouldn't need a separate block entity class, and thus I'd be able to just reference
    // the field.
    @Override
    public DefaultedList<ItemStack> getItems() {return items;}

    @Override
    public void readNbt(NbtCompound nbt) {
        // No call to super, because it has an empty body.
        // The server only sends full item stacks in NBT sync messages. Thus...
        // Clear the inventory, so that stacks that aren't sent by the server are cleared.
        items.clear();
        Inventories.readNbt(nbt, items);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, items);
        return super.writeNbt(nbt);
    }

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
        DefaultedList<ItemStack> stack = world.isClient() ? getItems(): null;
        // TODO: Implement inventory validation.
        this.hasGenericItems = this.shelfHasGenericItem(); // Are there any generic items to render?
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
        // If this is running on the server, sync to the client.
        if(!world.isClient()) sync();
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }
}

