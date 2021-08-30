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

    @Override
    public void markDirty() {if(this.world != null) markDirty(this.world, this.pos, this.getCachedState());}

    protected static void markDirty(World world, BlockPos pos, BlockState state){
        world.markDirty(pos);
        // TODO: Implement inventory validation.
        // TODO: Implement BlockState updates.
        // Lifted directly from BlockEntity.
        if (!state.isAir()) {
            world.updateComparators(pos, state.getBlock());
        }
    }
}

