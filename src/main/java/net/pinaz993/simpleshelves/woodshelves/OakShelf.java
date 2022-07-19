package net.pinaz993.simpleshelves.woodshelves;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.pinaz993.simpleshelves.AbstractShelf;
import net.pinaz993.simpleshelves.ShelfEntity;
import org.jetbrains.annotations.Nullable;

public class OakShelf extends AbstractShelf {
    /**
     * Implementation of Abstract Shelf that uses the :oak_planks texture.
     * This is as simple as an implementation gets, I think.
     * @param settings: used for super HorizontalFacingBlock
     */
    public OakShelf(Settings settings) {super(settings);}

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ShelfEntity(pos, state);
    }
}
