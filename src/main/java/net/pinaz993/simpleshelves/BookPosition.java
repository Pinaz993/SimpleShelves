package net.pinaz993.simpleshelves;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public enum BookPosition {
    ALPHA_1(0, 3, 6.74f, 6.8f, 0b000_000_000_001),
    ALPHA_2(1, 1, 6.52f, 7.0f, 0b000_000_000_010),
    ALPHA_3(2, 4, 7.13f, 6.6f, 0b000_000_000_100),
    BETA_1( 3, 2, 6.07f, 6.5f, 0b000_000_001_000),
    BETA_2( 4, 3, 5.97f, 6.4f, 0b000_000_010_000),
    BETA_3( 5, 3, 6.25f, 7.4f, 0b000_000_100_000),
    GAMMA_1(6, 3, 6.99f, 5.7f, 0b000_001_000_000),
    GAMMA_2(7, 2, 6.62f, 6.2f, 0b000_010_000_000),
    GAMMA_3(8, 3, 6.79f, 6.6f, 0b000_100_000_000),
    DELTA_1(9, 2, 5.75f, 6.0f, 0b001_000_000_000),
    DELTA_2(10,4, 6.76f, 7.2f, 0b010_000_000_000),
    DELTA_3(11,2, 6.65f, 6.2f, 0b100_000_000_000);

    // The Z coordinate of the front surface of the rear plate of the shelf. This is as low in Z as a vertex can be.
    private static final float Z_BACK_STOP = 1 / 16f;

    public final int SLOT; // The index of the slot that this book position stores books in.
    public final float WIDTH; // Width in meters from front cover to rear cover
    public final float HEIGHT; // Length in meters from top to bottom
    public final float DEPTH; // Length in meters from spine to rear shelf
    public final int BIT_FLAG; // A bit mask for use in quickly determining which books to render client-side

    BookPosition(int slot, int width, float height, float depth, int bitFlag) {
        this.SLOT = slot;
        // Since all measurements here are provided in pixels, divide by 16 to get measurements in meters.
        this.WIDTH = width/16f;
        this.HEIGHT = height/16f;
        this.DEPTH = depth/16f;
        this.BIT_FLAG = bitFlag;
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
                    if (u >= bp.getLeftEdge(q) && u < bp.getRightEdge( q)) return bp;
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
                    if (u >= bp.getLeftEdge(q) && u < bp.getRightEdge( q)) return bp;
                throw new IllegalStateException( // Inaccessible unless none of the books in the quadrant are a match.
                        "Horizontal Position " + u + " doesn't hit any book in " + q);
            }
            default:
                throw new IllegalStateException(String.format("No books are accessible from the %s side.", localSide));

        }
    }

    /**
     * Iterate through all quadrants, and check if this book position exists in said quadrant.
     * @return the quadrant the position belongs to.
     */
    public ShelfQuadrant getQuadrant() {
        for(ShelfQuadrant q: ShelfQuadrant.class.getEnumConstants()) // Iterate through quadrants.
            for(BookPosition bp: q.BOOK_POSITIONS) // Iterate through positions in each quadrant.
                if(bp == this) return q; // If you find the book position in question, return the quadrant you found it in.
        throw new IllegalStateException(String.format("%s is not in any shelf quadrant.", this)); // How did we get here?
    }

    /**
     * Returns the distance in meters between the beginning of this book's quadrant to the left edge of the book.
     * e.g.: GAMMA_1 is .1875m wide at the spine. GAMMA_2: .125m. GAMMA_3: .1875. If this is called from GAMMA_3,
     * it will return .1875 + .125, or .3125, exactly 5 pixels across. I feel like this one isn't as easy to understand
     * as some of my other work. That being said, it allows for some very nice elegance.
     */
    public float getLeftEdge( ShelfQuadrant quadrant){
        float rtn = 0;
        for(BookPosition bp: quadrant.BOOK_POSITIONS){
            if(bp == this) return rtn; // Stop and return if we've reached this book.
            rtn += bp.WIDTH; // Add width to the return value iff we're not done.
        }
        throw new IllegalArgumentException("Book Position " + this
                + " is not located in Shelf Quadrant " + quadrant);
    }

    /**
     * Does the same as getLeftEdge(), except it includes the width of this book.
     */
    public float getRightEdge(ShelfQuadrant quadrant){
        float rtn = 0;
        for(BookPosition bp: quadrant.BOOK_POSITIONS){
            rtn += bp.WIDTH; // Add width to the return value iff we're not done.
            if(bp == this) return rtn; // Stop and return if we've reached this book.
        }
        throw new IllegalArgumentException("Book Position " + this
                + " is not located in Shelf Quadrant " + quadrant);
    }

    @Environment(EnvType.CLIENT)
    public void emitBookQuads(QuadEmitter e) {
        // Get the left edge, and right edge and bottom coordinates
        ShelfQuadrant q = this.getQuadrant();
        float left = this.getLeftEdge(q);
        float right = this.getRightEdge(q);
        // If the book is in the alpha or beta quadrants (AKA top shelf),
        // the bottom of the book should be at 9/16. Else, 1/16.
        // I'm pretty sure the List gets marked for garbage collection after this line.
        float shelf = List.of(ShelfQuadrant.ALPHA, ShelfQuadrant.BETA).contains(q) ? 9/16f : 1/16f;
        // First, the head face.
        e.pos(0, left, HEIGHT, Z_BACK_STOP);
        e.pos(1, left, HEIGHT, DEPTH);
        e.pos(2, right, HEIGHT, DEPTH);
        e.pos(3, right, HEIGHT, Z_BACK_STOP);
        // Sprite work:
        //TODO: Sprite work

        // Next, the rear cover.
        e.pos(0, left, HEIGHT, Z_BACK_STOP);
        e.pos(1, left, shelf, Z_BACK_STOP);
        e.pos(2, left, shelf, DEPTH);
        e.pos(3, left, HEIGHT, DEPTH);
        // Sprite work:

        // Now, the spine.
        e.pos(0, left, HEIGHT, DEPTH);
        e.pos(1, left, shelf, DEPTH);
        e.pos(2, right, shelf, DEPTH);
        e.pos(3, right, HEIGHT, DEPTH);
        // Sprite work:

        // Finally, the front cover.
        e.pos(0, right, HEIGHT, DEPTH);
        e.pos(1, right, shelf, DEPTH);
        e.pos(2, right, shelf, Z_BACK_STOP);
        e.pos(3, right, HEIGHT, Z_BACK_STOP);
        // Sprite work
    }
}
