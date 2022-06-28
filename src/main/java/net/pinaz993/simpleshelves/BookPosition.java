package net.pinaz993.simpleshelves;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public enum BookPosition {
    ALPHA_1(0, 3),// Top Left Most
    ALPHA_2(1, 1),
    ALPHA_3(2, 4),
    BETA_1( 3, 2),
    BETA_2( 4, 3),
    BETA_3( 5, 3),// Top Right Most
    GAMMA_1(6, 3),// Bottom Left Most
    GAMMA_2(7, 2),
    GAMMA_3(8, 3),
    DELTA_1(9, 2),
    DELTA_2(10,4),
    DELTA_3(11,2); // Bottom Right Most


    public final int SLOT; // The index of the slot that this book position stores books in.
    public final float WIDTH; // Width in meters from front cover to rear cover

    BookPosition(int slot, int width) {
        this.SLOT = slot;
        // Since this is provided in pixels, divide by 16 to get measurement in meters.
        this.WIDTH = width/16f;

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
}
