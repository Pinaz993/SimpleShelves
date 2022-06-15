package net.pinaz993.simpleshelves.woodshelves;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.pinaz993.simpleshelves.AbstractShelf;
import net.pinaz993.simpleshelves.ShelfEntity;
import org.jetbrains.annotations.Nullable;

public class CrimsonShelf extends AbstractShelf {
    public CrimsonShelf(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShelfEntity(pos, state);
    }
}
