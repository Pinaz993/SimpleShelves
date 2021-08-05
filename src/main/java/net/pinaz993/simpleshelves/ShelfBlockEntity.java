package net.pinaz993.simpleshelves;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class ShelfBlockEntity extends BlockEntity implements ShelfInventory {
    public ShelfBlockEntity(BlockPos pos, BlockState state) {
        super(SimpleShelves.SHELF_BLOCK_ENTITY, pos, state);
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
}

