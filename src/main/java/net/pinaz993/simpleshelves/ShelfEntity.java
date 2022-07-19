package net.pinaz993.simpleshelves;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.pinaz993.simpleshelves.SimpleShelves.SHELF_BLOCK_ENTITY;

/**
 * A block entity for shelves. Only contains methods that could not be implemented in ShelfInventory. Pretty much all
 * inventory stuff lives over there.
 */

public class ShelfEntity extends BlockEntity implements ShelfInventory, RenderAttachmentBlockEntity {

    DefaultedList<ItemStack> items;    // The items that are in the inventory.
    private boolean hasGenericItems;   // Referring to this should be faster than querying the inventory every frame.
    public boolean hasGenericItems() { return hasGenericItems;} // Private with getter, because nothing should be
                                                                // setting it except for markDirtyInWorld().
    private int redstoneValue; // Cached value so we don't have to query the inventory for every redstone update.
    public int getRedstoneValue() {return this.redstoneValue;} // Private with getter for same reason as above.

    // An int that is used to contain binary flags to see if the book slots are filled. Initialized as empty.
    private int BookSlotBinaryFlagContainer = 0b000_000_000_000; // Private with getter for yet again the same reason.
    // If you're wondering why I would make such a thing, consider this:
    // To bake the model for each book shelf, the model needs to know which books to bake into the model. As such,
    // unless I wish to throw all such things into the entity renderer, I need to get that information to the baked
    // model. Critically, it is <i>much</i> faster to load a single int than it is to actually query the array of
    // items. Thus, instead of asking the inventory if a book slot is occupied, I'll simply calculate and cache this
    // value here every time the inventory is marked dirty, and thus be able to quickly query which books are available
    // using bit masks and binary logic to speed up the rendering process. As a bonus, this is also very easy to compare
    // against a cached value to see if the model even needs to be re-baked before we go through all that trouble.


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
                       // The server will not send empty item stacks in NBT updates.
        items.clear(); // Thus, we clear the list before populating our items list.
        Inventories.readNbt(nbt, items); // Populate our list of items.
        markDirty(); // Update the look of the block.
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
        // Sometimes I hate @Nullable.
        rtn.putString("id", Objects.requireNonNull(BlockEntityType.getId(SHELF_BLOCK_ENTITY)).toString());
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
                world.spawnEntity(new ItemEntity(world, // Spit the generic item out the top of the shelf.
                    pos.getX() +.5, pos.getY()+1.5, pos.getZ() +.5,
                    removeStack(quad.GENERIC_ITEM_SLOT)));
                LogManager.getLogger().warn("Shelf quadrant " + quad + " at " + pos
                    + " contains both book-like items and generic items. "
                    + "Ejecting Generic item to block space above."); // Log the anomaly.
            }
        hasGenericItems = this.shelfHasGenericItem(); // Are there any generic items to render? Cache the answer.
        redstoneValue = 0; // Reset the redstone value.
        BookSlotBinaryFlagContainer = 0b000_000_000_000; // Reset the binary flags.
        // Iterate over all positions and record the new state values, updating redstone value if needed.
        for(BookPosition bp: BookPosition.values()){
            ItemStack stack = getStack(bp.SLOT); // Get the stack in the slot.
            // Flip the bit flag if the slot isn't empty. Otherwise, it stays 0.
            if(!stack.isEmpty()) BookSlotBinaryFlagContainer |= bp.BIT_FLAG;
            // If the stack is of redstone books, update redstone value if this is higher than what we've seen thus far.
            if(stack.isOf(SimpleShelves.REDSTONE_BOOK))
                this.redstoneValue = Math.max(this.redstoneValue, stack.getCount());
        }
        // Cause clients to reevaluate block model.
        world.updateListeners(pos, state, state, Block.SKIP_LIGHTING_UPDATES | Block.NOTIFY_ALL);
        world.updateNeighborsAlways(pos, state.getBlock()); // Cause all neighbors to receive a block update.
        if(!world.isClient()) // If this is running on the server...
            ((ServerWorld)world).getChunkManager().markForUpdate(pos); // Mark changes to be synced to the client.
    }


    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.toNbt();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return BookSlotBinaryFlagContainer;
    }
}

