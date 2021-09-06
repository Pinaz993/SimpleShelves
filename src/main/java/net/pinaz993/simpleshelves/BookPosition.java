package net.pinaz993.simpleshelves;


import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public enum BookPosition {
    ALPHA_1(0, AbstractShelf.BOOK_ALPHA_1, 3),
    ALPHA_2(1, AbstractShelf.BOOK_ALPHA_2, 1),
    ALPHA_3(2, AbstractShelf.BOOK_ALPHA_3, 4),
    BETA_1(3, AbstractShelf.BOOK_BETA_1, 2),
    BETA_2(4, AbstractShelf.BOOK_BETA_2, 3),
    BETA_3(5, AbstractShelf.BOOK_BETA_3, 3),
    GAMMA_1(6, AbstractShelf.BOOK_GAMMA_1, 3),
    GAMMA_2(7, AbstractShelf.BOOK_GAMMA_2, 2),
    GAMMA_3(8, AbstractShelf.BOOK_GAMMA_3, 3),
    DELTA_1(9, AbstractShelf.BOOK_DELTA_1, 2),
    DELTA_2(10, AbstractShelf.BOOK_DELTA_2, 4),
    DELTA_3(11, AbstractShelf.BOOK_DELTA_3, 2);


    public final int SLOT; // The index of the slot that this book position stores books in.
    // The property that tells the model loading system whether to load this book or not.
    public final BooleanProperty BLOCK_STATE_PROPERTY;
    // How wide this book is.
    public final double WIDTH;

    BookPosition(int slot, BooleanProperty blockStateProperty, int pixels) {
        this.SLOT = slot;
        this.BLOCK_STATE_PROPERTY = blockStateProperty;
        this.WIDTH = pixels *.0625;
    }

    /** When the player uses this block, calculate which book they're clicking on.
     * @param hit The BlockHitResult that tells you how and where the player clicked.
     * @param facing which way the block was facing.
     */
    public static BookPosition getBookPos (BlockHitResult hit, Direction facing) {
        LocalHorizontalSide localSide = LocalHorizontalSide.getLocalSide(hit.getSide(), facing);
        Vec3d localCoords = hit.getPos().subtract(Vec3d.of(hit.getBlockPos()));
        ShelfQuadrant q = ShelfQuadrant.getQuadrant(hit, facing);
        double u;
        switch(localSide) {
            // Left side only has access to the first books in Alpha and Delta quadrants.
            case LEFT:
                return switch (q) {
                    case ALPHA -> ALPHA_1;
                    case GAMMA -> GAMMA_1;
                    default -> throw new IllegalStateException("Cannot access " + q + " from " + localSide + " side.");
                };
            // Right side only has access to the last books in Beta and Gamma quadrants.
            case RIGHT:
                return switch (q) {
                    case BETA -> BETA_3;
                    case DELTA -> DELTA_3;
                    default -> throw new IllegalStateException("Cannot access " + q + " from " + localSide + " side.");
                };
            // Top side has access to all three books in both Alpha and Beta. Measure by the horizontal coordinate.
            case TOP: {
                // What is the horizontal coordinate? That depends on which way the shelf is facing.
                u = switch (facing) {
                    case NORTH -> 1 - localCoords.x;
                    case EAST -> 1 - localCoords.z;
                    case SOUTH -> localCoords.x;
                    case WEST -> localCoords.z;
                    default -> throw new IllegalStateException(
                            "Shelves cannot face " +facing + ".");
                };
                if (q == ShelfQuadrant.BETA) u -= .5; // BETA starts halfway into the block, horizontally.
                // Iterate through all book positions in the quadrant that was clicked on.
                for (BookPosition bp : q.BOOK_POSITIONS)
                    // If u is between the edges, return the current book.
                    if (u >= getLeftEdge(bp, q) && u < getRightEdge(bp, q)) return bp;
                throw new IllegalStateException( // Inaccessible unless none of the books in the quadrant are a match.
                        "Horizontal Position " + u + " doesn't hit any book in " + q);
            }
            // Front face has access to all books on shelf.
            case FRONT: {
                // Again, horizontal depends on which way the shelf is facing.
                u = switch (facing) {
                    case NORTH -> 1 - localCoords.x;
                    case EAST -> 1 - localCoords.z;
                    case SOUTH -> localCoords.x;
                    case WEST -> localCoords.z;
                    default -> throw new IllegalStateException(
                            "Shelves cannot face ".concat(facing.toString()).concat("."));
                };
                // BETA and DELTA start halfway into the block, horizontally.
                if (q == ShelfQuadrant.BETA || q == ShelfQuadrant.DELTA) u -= .5;
                // Iterate through all book positions in the quadrant that was clicked on.
                for (BookPosition bp : q.BOOK_POSITIONS)
                    // If u is between the edges, return the current book.
                    if (u >= getLeftEdge(bp, q) && u < getRightEdge(bp, q)) return bp;
                throw new IllegalStateException( // Inaccessible unless none of the books in the quadrant are a match.
                        "Horizontal Position " + u + " doesn't hit any book in " + q);
            }
            default:
                throw new IllegalStateException(String.format("No books are accessible from the %s side.", localSide));

        }
    }

    /**
     * Returns the distance in meters between the beginning of this book's quadrant to the left edge of the book.
     * e.g.: GAMMA_1 is .1875m wide at the spine. GAMMA_2: .125m. GAMMA_3: .1875. If this is called from GAMMA_3,
     * it will return .1875 + .125, or .3125, exactly 5 pixels across. I feel like this one isn't as easy to understand
     * as some of my other work. That being said, it allows for some very nice elegance.
     */
    public static double getLeftEdge(BookPosition pos, ShelfQuadrant quadrant){
        double rtn = 0;
        for(BookPosition bp: quadrant.BOOK_POSITIONS){
            if(bp == pos) return rtn; // Stop and return if we've reached this book.
            rtn += bp.WIDTH; // Add width to the return value iff we're not done.
        }
        throw new IllegalArgumentException("Book Position " + pos.toString()
                + " is not located in Shelf Quadrant " + quadrant);
    }

    /**
     * Does the same as getLeftEdge(), except it includes the width of this book.
     */
    public static double getRightEdge(BookPosition pos, ShelfQuadrant quadrant){
        double rtn = 0;
        for(BookPosition bp: quadrant.BOOK_POSITIONS){
            rtn += bp.WIDTH; // Add width to the return value iff we're not done.
            if(bp == pos) return rtn; // Stop and return if we've reached this book.
        }
        throw new IllegalArgumentException("Book Position " + pos.toString()
                + " is not located in Shelf Quadrant " + quadrant);
    }
}
