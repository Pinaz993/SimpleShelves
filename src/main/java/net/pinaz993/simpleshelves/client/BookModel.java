package net.pinaz993.simpleshelves.client;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.pinaz993.simpleshelves.BookPosition;

import java.util.Random;

@SuppressWarnings("deprecation") //Because Mojang abuses @Deprecated. Not the smartest practice, I'll say. Again.
public class BookModel {
    // I think I want to delegate generating the quads for the books to here. This way, when the ShelfEntity is
    // constructed, I can just make 12 of these and either include their quads in the baked model or not.
    // The position the book is in will determine it's width, but it's height and depth are flexible. Fields for those.

    // When the quads that need to be rendered are determined, their positions will be based on their position in the
    // shelf. I don't know how rectangles are drawn in this system, but I'm guessing that I'll need at least two points.
    // Thus, the position and shape of each quad will be determined by the position of the points that define it.
    // Depending on what points I need to define a quad, I'll need to calculate those upon construction.
    // Probably need fields for those.

    // I'll also need an extra bit of information that tells the quad in which direction it can be seen. That will be a
    // static class variable for each of the four quads that need to be rendered: SPINE, REAR_COVER, FRONT_COVER, and
    // HEAD (Upwards facing part of the book that shows the edges of the book's pages.)
    // After further research, it looks like the QuadEmitter that will be drawing the quad just takes a Direction.

    // I'll also handle keeping track of the textures in here. Fields for the IDs of the textures to be used for each
    // book, as well as static arrays for all of the texture names, because that info does not belong in ShelfEntity or
    // ShelfModel.

    // TODO: Determine what information will need to be present in this class to render the four quads.
    // The origin for the block is at the North-West bottom corner of the block, or at the bottom rear left corner of
    // the shelf, at least according to block bench.

    // Arrays of strings for identifying textures.
    public static final String[] ONE_PX_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_one_px_alpha"
    };
    public static final String[] TWO_PX_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_two_px_alpha",
            "simple_shelves:block/book_two_px_beta",
            "simple_shelves:block/book_two_px_gamma",
            "simple_shelves:block/book_two_px_delta"
    };
    public static final String[] THREE_PX_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_three_px_alpha",
            "simple_shelves:block/book_three_px_beta",
            "simple_shelves:block/book_three_px_gamma",
            "simple_shelves:block/book_three_px_delta",
            "simple_shelves:block/book_three_px_epsilon"
    };
    public static final String[] FOUR_PX_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_four_px_alpha",
            "simple_shelves:block/book_four_px_beta",
    };
    public static final String[] PAPER_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_paper"
    };

    /**
     * Chooses a random texture from the lists above.
     * This is very complicated, but it's also very compact. I've yet to decide if the latter is a good thing.
     * @param list: An integer between 0 and 4 inclusive. 0 indicates the paper list, 1-4 indicate the list for that
     *            amount of pixels.
     * @param random: An RNG seeded with the world seed and the shelf's block position.
     * @return The sprite identifier for a texture that will be used, either for the cover of the book, or its head.
     */
    public static SpriteIdentifier getRandomSpriteIdentifier(int list, Random random) {
        String[] names;
        names = switch (list) {
            case 0 -> PAPER_TEXTURE_NAMES;
            case 1 -> ONE_PX_TEXTURE_NAMES;
            case 2 -> TWO_PX_TEXTURE_NAMES;
            case 3 -> THREE_PX_TEXTURE_NAMES;
            case 4 -> FOUR_PX_TEXTURE_NAMES;
            default -> throw new IllegalArgumentException(String.format(
                    "Pixel width value out of range: %s is not between 0 and 4 inclusive.", list
            ));
        };
        // Chose a random (determined by the passed in random object) string from the indicated name array,
        // and return the texture associated with that string in the block atlas.
        return new SpriteIdentifier(
                SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
                        new Identifier(names[(int) (Math.ceil(random.nextDouble() % 1) * names.length)]));
    }

    // Static directions for each quad, before rotation. Horizontal orientation taken from Blockbench. I hope it works.
    private static final Direction HEAD_DIRECTION = Direction.UP; // Direction for the head (or top edge) quad.
    private static final Direction REAR_COVER_DIRECTION = Direction.WEST; // Direction for the rear cover quad.
    private static final Direction SPINE_DIRECTION = Direction.SOUTH; // Direction for the spine quad.
    private static final Direction FRONT_COVER_DIRECTION = Direction.EAST; // Direction for the front cover quad.

    // The minimum and maximum values for the variable dimensions of a book. A pixel is 1/16m, or .0625. Variables so I
    // can adjust them later if need be.
    public static final float MIN_HEIGHT = (float) 6 / 16;
    public static final float MAX_HEIGHT = (float) 7 / 16;
    public static final float MIN_DEPTH = (float) 6 / 16;
    public static final float MAX_DEPTH = (float) 7 / 16;


    public final BookPosition BOOK_POSITION; // The position this Book Model will correspond to. Will determine the
                                             // width of the model itself.
    public final float HEIGHT; // The height of the book, as determined by a random seed depending on the coordinates
                                // of the block and the seed of the world.
    public final float DEPTH; // The depth of the book, as determined in the same way as above.

    public final SpriteIdentifier COVER_SPRITE_ID; // The ID for the texture that will be applied to the REAR_COVER,
                                                   // SPINE, and FRONT_COVER quads.

    public final SpriteIdentifier HEAD_SPRITE_ID; // The ID for the texture that will be applied to the HEAD quad.

    // TODO: Implement constructor.

    // TODO: Implement a method that returns a baked or unbaked model for the book.
    // This model will be included in the final baked model for the shelf.
}
