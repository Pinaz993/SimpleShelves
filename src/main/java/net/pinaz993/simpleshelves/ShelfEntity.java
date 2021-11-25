package net.pinaz993.simpleshelves;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import static net.pinaz993.simpleshelves.SimpleShelves.SHELF_BLOCK_ENTITY;

/**
 * A block entity for shelves. Only contains methods that could not be implemented in ShelfInventory. Pretty much all
 * inventory stuff lives over there.
 */

public class ShelfEntity extends BlockEntity implements ShelfInventory {

    DefaultedList<ItemStack> items;    // The items that are in the inventory.
    private boolean hasGenericItems;   // Referring to this should be faster than querying the inventory every frame.
    public boolean hasGenericItems() { return hasGenericItems;} // Private with getter, because nothing should be
                                                                // setting it except for markDirtyInWorld().
    private int redstoneValue; // Cached value so we don't have to query the inventory for every redstone update.
    public int getRedstoneValue() {return this.redstoneValue;} // Private with getter for same reason as above.


    public ShelfEntity(BlockPos pos, BlockState state) {
        super(SHELF_BLOCK_ENTITY, pos, state);
        // Initialize the list of items that are stored in this inventory.
        this.items = DefaultedList.ofSize(16, ItemStack.EMPTY);
        this.hasGenericItems = false;
        this.redstoneValue = 0;
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
    public void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, items);
    }

    /**
     * Serializes the state of this block entity. I don't know if I need the id, x, y, and z, but ultimately, they're
     * not all that much to put in the NBT, and better safe than sorry. Can be used for saving and syncing.
     * @return The state of this block entity in NBT form.
     */
    public NbtCompound toNbt(){
        NbtCompound rtn = new NbtCompound();
        Inventories.writeNbt(rtn, items);
        rtn.putString("id", BlockEntityType.getId(SHELF_BLOCK_ENTITY).toString());
        rtn.putInt("x", this.pos.getX());
        rtn.putInt("y", this.pos.getY());
        rtn.putInt("z", this.pos.getZ());
        return rtn;
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
        // Verify that no quadrant has both generic items and books.
        for(ShelfQuadrant quad: ShelfQuadrant.class.getEnumConstants())
            if(quadrantHasGenericItem(quad) && quadrantHasBook(quad)) { // If one does...
                world.spawnEntity(new ItemEntity(world,
                    pos.getX() +.5, pos.getY()+1.5, pos.getZ() +.5,
                    removeStack(quad.GENERIC_ITEM_SLOT))); // Spit the generic item out the top of the shelf.
                LogManager.getLogger().warn("Shelf quadrant " + quad + " at " + pos
                    + " contains both book-like items and generic items. "
                    + "Ejecting Generic item to block space above."); // Log the anomaly.
            }
        this.hasGenericItems = this.shelfHasGenericItem(); // Are there any generic items to render?
        this.redstoneValue = 0; // Reset the redstone value.
        // Iterate through all block positions, updating state iff needed.
        for(BookPosition bp: BookPosition.class.getEnumConstants()){
            boolean oldState = state.get(bp.BLOCK_STATE_PROPERTY); // What is the state now?
            ItemStack stack = getStack(bp.SLOT); // Get the stack in the slot.
            boolean newState = !stack.isEmpty(); // Is the associated slot empty?
            // If the old state is different than the new state, tell the world to update the state to the new one.
            // I don't just update all of them because I don't know how intensive that is, and I don't want to lag.
            if(oldState != newState) world.setBlockState(pos, state.with(bp.BLOCK_STATE_PROPERTY, newState));
            // If the stack is of redstone books, update redstone value if this is higher than what we've seen thus far.
            if(stack.isOf(SimpleShelves.REDSTONE_BOOK))
                this.redstoneValue = Math.max(this.redstoneValue, stack.getCount());
        }
        // Super calls World.markDirty() and possibly World.updateComparators().
        BlockEntity.markDirty(world, pos, state);
        if(!world.isClient()) { // If this is running on the server...
            world.updateNeighbors(pos, state.getBlock()); // Update all the neighbors.
            // Sync to the client.
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(
                this,
                (BlockEntity b) -> this.toNbt()
        );
    }
}

