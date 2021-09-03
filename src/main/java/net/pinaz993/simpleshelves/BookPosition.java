package net.pinaz993.simpleshelves;


import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public enum BookPosition {
    ALPHA_1(0, AbstractShelf.BOOK_ALPHA_1),
    ALPHA_2(1, AbstractShelf.BOOK_ALPHA_2),
    ALPHA_3(2, AbstractShelf.BOOK_ALPHA_3),
    BETA_1(3, AbstractShelf.BOOK_BETA_1),
    BETA_2(4, AbstractShelf.BOOK_BETA_2),
    BETA_3(5, AbstractShelf.BOOK_BETA_3),
    GAMMA_1(6, AbstractShelf.BOOK_GAMMA_1),
    GAMMA_2(7, AbstractShelf.BOOK_GAMMA_2),
    GAMMA_3(8, AbstractShelf.BOOK_GAMMA_3),
    DELTA_1(9, AbstractShelf.BOOK_DELTA_1),
    DELTA_2(10, AbstractShelf.BOOK_DELTA_2),
    DELTA_3(11, AbstractShelf.BOOK_DELTA_3);


    public final int SLOT; // The index of the slot that this book position stores books in.
    // The property that tells the model loading system whether to load this book or not.
    public final BooleanProperty BLOCK_STATE_PROPERTY;

    BookPosition(int slot, BooleanProperty blockStateProperty) {
        this.SLOT = slot;
        this.BLOCK_STATE_PROPERTY = blockStateProperty;
    }

    /** When the player uses this block, calculate which book they're clicking on.
     * @param hit The BlockHitResult that tells you how and where the player clicked.
     * @param facing which way the block was facing.
     */
    public static BookPosition getBookPos (BlockHitResult hit, Direction facing) {
        LocalHorizontalSide localSide = LocalHorizontalSide.getLocalSide(hit.getSide(), facing);
        Vec3d localCoords = hit.getPos().subtract(Vec3d.of(hit.getBlockPos()));
        ShelfQuadrant quadrant = ShelfQuadrant.getQuadrant(hit, facing);
        double u;
        switch(localSide) {
            // Left side only has access to the first books in Alpha and Delta quadrants.
            case LEFT:
                return switch (quadrant) {
                    case ALPHA -> ALPHA_1;
                    case GAMMA -> GAMMA_1;
                    default -> throw new IllegalStateException(String.format("Cannot access %s from %s side.", quadrant, localSide));
                };
            // Right side only has access to the last books in Beta and Gamma quadrants.
            case RIGHT:
                return switch (quadrant) {
                    case BETA -> BETA_3;
                    case DELTA -> DELTA_3;
                    default -> throw new IllegalStateException(String.format("Cannot access %s from %s side.", quadrant, localSide));
                };
            // Top side has access to all three books in both Alpha and Beta. Measure by the horizontal coordinate.
            case TOP:
                // What is the horizontal coordinate? That depends on which way the shelf is facing.
                u = switch (facing) {
                    case NORTH -> 1 - localCoords.x;
                    case EAST -> 1 - localCoords.z;
                    case SOUTH -> localCoords.x;
                    case WEST -> localCoords.z;
                    default -> throw new IllegalStateException("Shelves cannot face ".concat(facing.toString()).concat("."));
                };
                // TODO: Transition magic width numbers to enum fields.
                // ALPHA_1 is three pixels across. Each pixel corresponds to 62.5mm, or .0625m.
                if (u < .1875) return ALPHA_1;
                // ALPHA_2 is one pixel across.
                else if (u < .25) return ALPHA_2;
                // ALPHA_3 is four pixels across.
                else if (u < .5) return ALPHA_3;
                // BETA_1 is two pixels across.
                else if (u < .625) return BETA_1;
                // BETA_2 is three pixels across.
                else if (u < .8125) return BETA_2;
                // BETA_3 is three pixels across, but it's also the last resort for the top half.
                else return BETA_3;
            // Front face has access to all books on shelf.
            case FRONT:
                // Again, horizontal depends on which way the shelf is facing.
                u = switch (facing) {
                    case NORTH -> 1 - localCoords.x;
                    case EAST -> 1 - localCoords.z;
                    case SOUTH -> localCoords.x;
                    case WEST -> localCoords.z;
                    default -> throw new IllegalStateException("Shelves cannot face ".concat(facing.toString()).concat("."));
                };
                if(localCoords.y > .5){ // If we're in the top half of the block...
                    // ALPHA_1 is three pixels across. Again, each pixel corresponds to 62.5mm, or .0625m.
                    if (u < .1875) return ALPHA_1;
                    // ALPHA_2 is one pixel across.
                    else if (u < .25) return ALPHA_2;
                    // ALPHA_3 is four pixels across.
                    else if (u < .5) return ALPHA_3;
                    // BETA_1 is two pixels across.
                    else if (u < .625) return BETA_1;
                    // BETA_2 is three pixels across.
                    else if (u < .8125) return BETA_2;
                    // BETA_3 is three pixels across, but it's also the last resort for the top half.
                    else return BETA_3;
                } else { // We're in the bottom half of the block, so...
                    // DELTA_1 is three pixels across.
                    if (u < .1875) return GAMMA_1;
                    // DELTA_2 is two pixels across.
                    else if (u < .3125) return GAMMA_2;
                    // DELTA 3 is 3 pixels across.
                    else if (u < .5) return GAMMA_3;
                    // GAMMA_1 is 2 pixels across.
                    else if (u < .625) return DELTA_1;
                    // GAMMA_2 is 4 pixels across.
                    else if (u < .875) return DELTA_2;
                    // GAMMA_3 is 2 pixels across, but is also the last resort for the bottom half.
                    else return DELTA_3;
                }
            default: throw new IllegalStateException(String.format("No books are accessible from the %s side.", localSide));
        }
    }
}
