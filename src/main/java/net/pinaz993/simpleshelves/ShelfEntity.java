package net.pinaz993.simpleshelves;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.pinaz993.simpleshelves.client.BookModel;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * A block entity for shelves. Only contains methods that could not be implemented in ShelfInventory. Pretty much all
 * inventory stuff lives over there.
 */

public class ShelfEntity extends BlockEntity
        implements ShelfInventory, BlockEntityClientSerializable, RenderAttachmentBlockEntity {

    DefaultedList<ItemStack> items;    // The items that are in the inventory.
    private boolean hasGenericItems;   // Referring to this should be faster than querying the inventory every frame.
    public boolean hasGenericItems() { return hasGenericItems;} // Private with getter, because nothing should be
                                                                // setting it except for markDirtyInWorld().
    private int redstoneValue; // Cached value so we don't have to query the inventory for every redstone update.
    public int getRedstoneValue() {return this.redstoneValue;} // Private with getter for same reason as above.

    private final BookModel[] MODELS = new BookModel[12];
    private boolean needsUpdate = true;


    public ShelfEntity(BlockPos pos, BlockState state) {
        super(SimpleShelves.SHELF_BLOCK_ENTITY, pos, state);
        // Initialize the list of items that are stored in this inventory.
        this.items = DefaultedList.ofSize(16, ItemStack.EMPTY);
        // Shelf is empty, so no generic items to render.
        this.hasGenericItems = false;
        // No redstone signal at the moment.
        this.redstoneValue = 0;
        // Initialize the array of block models, but only if this is running on the client.
        if(world != null && world.isClient()) {
            Random r = new Random(pos.asLong()); // Source randomness from the shelf's position in the world.
            for(BookPosition bp: BookPosition.class.getEnumConstants()){
                MODELS[bp.SLOT] = new BookModel(bp, r);
            }
        }
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
        // Iterate through all block positions, updating this.bookSlotsOccupied and redstone value if needed.
        for(BookPosition bp: BookPosition.class.getEnumConstants()) {
            // Grab the stack for this book pos.
            ItemStack stack = getStack(bp.SLOT);
            // Update the enabled flag for the book model for this slot.
            this.MODELS[bp.SLOT].setEnabled(!stack.isEmpty());
            // Redstone book? Update the redstone value for the shelf.
            if(stack.isOf(SimpleShelves.REDSTONE_BOOK))
                this.redstoneValue = Math.max(this.redstoneValue, stack.getCount());
        }
        // Super calls World.markDirty() and possibly World.updateComparators().
        BlockEntity.markDirty(world, pos, state);
        if(!world.isClient()) { // If this is running on the server...
            world.updateNeighbors(pos, state.getBlock()); // Update all the neighbors.
            sync(); // Sync to the client.
        }
    }

    @Override
    public void fromClientTag(NbtCompound tag) {readNbt(tag);}

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {return writeNbt(tag);}

    /**
     * For passing information to the model to tell it which books to render. Only run on client instances.
     */
    @Override
    public @Nullable Object getRenderAttachmentData() {return MODELS;}
}

