package net.pinaz993.simpleshelves;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShelfBlockEntity extends BlockEntity implements ShelfInventory {
    DefaultedList<ItemStack> items;

    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleShelves.SHELF_BLOCK_ENTITY, pos, state);
        this.items = DefaultedList.ofSize(16, ItemStack.EMPTY);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return items;
    }

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

    // Lifted almost directly from BlockEntity.
    @Override
    public void markDirty() {if(this.world != null) markDirtyInWorld(this.world, this.pos, this.getCachedState());}

    // In BlockEntity, this method has the same name as the one above. Java doesn't want me to override that, as it's
    // 'pRoTeCtEd'. Bah!
    protected void markDirtyInWorld(World world, BlockPos pos, BlockState state){
        // TODO: Implement inventory validation.
        // Iterate through all block positions, updating state iff needed.
        for(BookPosition bpos: BookPosition.class.getEnumConstants()){
            // What is the state now?
            boolean oldState = state.get(bpos.BLOCK_STATE_PROPERTY);
            // Is the associated slot empty?
            boolean newState = !this.getItems().get(bpos.SLOT).isEmpty();
            // If the old state is different than the new state, tell the world to update the state to the new one.
            if(oldState != newState) world.setBlockState(pos, state.with(bpos.BLOCK_STATE_PROPERTY, newState));
        }
        markDirty(world, pos, state);
    }
}

