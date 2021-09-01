package net.pinaz993.simpleshelves;

import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

/**
 * Which quadrant of the shelf are we dealing with? What slots can be interacted with in this quadrant? Whose quadrant
 * is it anyway? All these questions and more can be answered now!
 * Quadrant Alpha is top left.
 * Quadrant Beta is top right.
 * Quadrant Gamma is bottom left.
 * Quadrant Delta is bottom right.
 * In other words, the quadrants are labeled LtR, TtB.
 */
public enum ShelfQuadrant {
    ALPHA(12, new BookPosition[]{BookPosition.ALPHA_1, BookPosition.ALPHA_2, BookPosition.ALPHA_3}),
    BETA(13, new BookPosition[]{BookPosition.BETA_1, BookPosition.BETA_2, BookPosition.BETA_3}),
    GAMMA(14, new BookPosition[]{BookPosition.GAMMA_1, BookPosition.GAMMA_2, BookPosition.GAMMA_3}),
    DELTA(14, new BookPosition[]{BookPosition.DELTA_1, BookPosition.DELTA_2, BookPosition.DELTA_3});

    public final int GENERIC_ITEM_SLOT;
    public final BookPosition[] BOOK_POSITIONS;

    ShelfQuadrant(int slot, BookPosition[] bookPositions){
        this.GENERIC_ITEM_SLOT = slot;
        this.BOOK_POSITIONS = bookPositions;
    }

    /**
     * Evaluates a BlockHitEvent to determine which quadrant of the shelf should be interacted with.
     *
     * @param hit The BlockHitEvent to evaluate.
     * @param facing The direction that the block is facing.
     * @return the quadrant that should be interacted with.
     */
    public static ShelfQuadrant getQuadrant(@NotNull BlockHitResult hit, Direction facing) {
        LocalHorizontalSide localSide = LocalHorizontalSide.getLocalSide(hit.getSide(), facing);
        Vec3d localCoords = hit.getPos().subtract(Vec3d.of(hit.getBlockPos())); // Spits out a new Vec3d.
        // Gather 'round the Switch-Tree, one and all! We're about to have a great time.
        switch (localSide) {
            // Left Side
            case LEFT:
                return localCoords.getY() >= .5 ? ALPHA : DELTA;
            // Well. That was a lot less fuss than I thought. Right side?
            case RIGHT:
                return localCoords.getY() >= .5 ? BETA : GAMMA;
            // Now I KNOW top isn't going to be that easy. I've got x and z to deal with there. Ditto for front.
            case TOP:
                return switch (facing) {
                    case NORTH -> localCoords.getX() >= .5 ? ALPHA : BETA;
                    case EAST -> localCoords.getZ() >= .5 ? ALPHA : BETA;
                    case SOUTH -> localCoords.getX() >= .5 ? BETA : ALPHA;
                    case WEST -> localCoords.getZ() >= .5 ? BETA : ALPHA;
                    default -> throw new IllegalStateException(String.format("Shelves cannot face %s.", facing));
                };
            case FRONT:
                switch (facing) {
                    case NORTH:
                        if(localCoords.getX() >=.5) return localCoords.getY() >= .5 ? ALPHA : GAMMA;
                        else return localCoords.getY() >= .5 ? BETA : DELTA;
                    case EAST:
                        if(localCoords.getZ() >=.5) return localCoords.getY() >= .5 ? ALPHA : GAMMA;
                        else return localCoords.getY() >= .5 ? BETA : DELTA;
                    case SOUTH:
                        if(localCoords.getX() <=.5) return localCoords.getY() >= .5 ? ALPHA : GAMMA;
                        else return localCoords.getY() >= .5 ? BETA : DELTA;
                    case WEST:
                        if(localCoords.getZ() <=.5) return localCoords.getY() >= .5 ? ALPHA : GAMMA;
                        else return localCoords.getY() >= .5 ? BETA : DELTA;
                    default:
                        throw new IllegalStateException(String.format("Shelves cannot face %s.", facing));
                }
            default: throw new IllegalStateException(String.format("How did you even click the %s side?!", localSide));
        }
    }
}
