package net.pinaz993.simpleshelves;


import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public enum BookPosition {
    ALPHA_1(0, 3),
    ALPHA_2(1, 1),
    ALPHA_3(2, 4),
    BETA_1(3, 2),
    BETA_2(4, 3),
    BETA_3(5, 3),
    GAMMA_1(6, 3),
    GAMMA_2(7, 2),
    GAMMA_3(8,  3),
    DELTA_1(9,  2),
    DELTA_2(10, 4),
    DELTA_3(11, 2);


    public final int SLOT; // The index of the slot that this book position stores books in.
    public final int PIXELS; // How wide this book is in pixels (1/16th of a meter).
    public final float WIDTH; // How wide this book is in meters.
    private ShelfQuadrant quadrant = null; // The quadrant this book sits in.
    private float leftEdge = -1; // The X coordinate of the left edge of the book. Calculated lazily.
    private float rightEdge = -1; // The X coordinate of the right edge of the book. Calculated lazily.
    // Yes, I could have statically assigned those, but both edges are emergent properties of the books. My gut says
    // that setting them leaves me open to problems if I change something.

    BookPosition(int slot, int pixels) {
        this.SLOT = slot;
        this.PIXELS = pixels;
        this.WIDTH = (float) pixels / 16;
    }

    /** When the player uses a shelf, calculate which book they're clicking on.
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
                    if (u >= bp.getLeftEdge() && u < bp.getRightEdge()) return bp;
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
                // BETA and DELTA start halfway into the block, horizontally. Subtract half to calculate from the edge.
                if (q == ShelfQuadrant.BETA || q == ShelfQuadrant.DELTA) u -= .5;
                // Iterate through all book positions in the quadrant that was clicked on.
                for (BookPosition bp : q.BOOK_POSITIONS)
                    // If u is between the edges, return the current book.
                    if (u >= bp.getLeftEdge() && u < bp.getRightEdge()) return bp;
                throw new IllegalStateException( // Inaccessible unless none of the books in the quadrant are a match.
                        "Horizontal Position " + u + " doesn't hit any book in " + q);
            }
            default:
                throw new IllegalStateException(String.format("No books are accessible from the %s side.", localSide));

        }
    }

    /**
     * Lazily calculates and returns the quadrant this book belongs to. Sets an internal property if this is the first
     * time. You might ask why I'm doing this instead of setting it on enum creation, like the slot, and the width in
     * pixels. I tried that. But since ShelfQuadrant enums have BookPosition integrated into their creation, one has to
     * be defined first. If you leave a circular dependency in like that, one or the other will be initialized to null.
     * This avoids that, while only going through the switch statement once per enum value.
     */
    public ShelfQuadrant getQuadrant() {
        if(quadrant == null) {
            quadrant = switch (this) {
                case ALPHA_1, ALPHA_2, ALPHA_3 -> ShelfQuadrant.ALPHA;
                case BETA_1, BETA_2, BETA_3 -> ShelfQuadrant.BETA;
                case GAMMA_1, GAMMA_2, GAMMA_3 -> ShelfQuadrant.GAMMA;
                case DELTA_1, DELTA_2, DELTA_3 -> ShelfQuadrant.DELTA;
            };
        }
        return quadrant;
    }

    /**
     * Lazily calculates and returns the distance in meters between the beginning of this book's quadrant to the left
     * edge of the book. Can also be used to get the X coordinate for the left edge.
     * e.g.: GAMMA_1 is .1875m wide at the spine. GAMMA_2: .125m. GAMMA_3: .1875. If this is called from GAMMA_3,
     * it will return .1875 + .125, or .3125, exactly 5 pixels across. I feel like this one isn't as easy to understand
     * as some of my other work. That being said, it allows for some very nice elegance.
     *
     * I am assigning a field to this because, while I could calculate this on the fly, I don't want to access up to
     * four objects in memory every time a shelf is placed down.
     */
    public float getLeftEdge(){
        if(leftEdge >= 0) return leftEdge; // We've already calculated this,so we don't need to do so again.
        leftEdge = 0; // Start at the left edge of the shelf.
        // It occurs to me that this is not guaranteed to iterate through all the books in the proper order.
        // It works for now, so I'll just put a note in ShelfQuadrant that order is important.
        for(BookPosition bp: getQuadrant().BOOK_POSITIONS){
            if(bp == this) return leftEdge; // Stop and return if we've reached this book.
            leftEdge += bp.WIDTH; // Add width to the return value iff we're not done.
        }
        // This is still here because the return statement is conditional.
        throw new IllegalArgumentException("Book Position " + this
                + " is not located in Shelf Quadrant " + this.quadrant);
    }

    /**
     * Lazily calculates the distance in meters between the beginning of this book's quadrant and the right edge of the
     * book. Can also be used to get the X coordinate of the right edge. Depends on getLeftEdge.
     */
    public float getRightEdge(){
        if(rightEdge >= 0) return rightEdge;
        rightEdge = getLeftEdge() + WIDTH;
        return rightEdge;
    }
}
