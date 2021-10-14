package net.pinaz993.simpleshelves.client;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.pinaz993.simpleshelves.BookPosition;
import net.pinaz993.simpleshelves.ShelfQuadrant;

import java.util.Random;
import java.util.function.Function;

import static net.minecraft.util.math.MathHelper.abs;

@SuppressWarnings("deprecation") //Because Mojang abuses @Deprecated. Not the smartest practice, I'll say. Again.
public class BookModel {
    // I think I want to delegate generating the quads for the books to here. This way, when the ShelfEntity is
    // constructed, I can just make 12 of these and either include their quads in the baked model or not.
    // The position the book is in will determine it's width, but it's height and depth are flexible. Fields for those.

    // I'll also need an extra bit of information that tells the quad in which direction it can be seen. That will be a
    // static class variable for each of the four quads that need to be rendered: SPINE, REAR_COVER, FRONT_COVER, and
    // HEAD (Upwards facing part of the book that shows the edges of the book's pages.)
    // After further research, it looks like the QuadEmitter that will be drawing the quad just takes a Direction.

    // I'll also handle keeping track of the textures in here. Fields for the IDs of the textures to be used for each
    // book, as well as static arrays for all of the texture names, because that info does not belong in ShelfEntity or
    // ShelfModel.

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
    public static SpriteIdentifier getRandomSprite(int list, Random random) {
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

    // The Z coordinate of the front surface of the rear plate of the shelf. This is as low in Z as a vertex can be.
    public static final float Z_BACK_STOP = (float) 1 / 16;


    public final BookPosition BOOK_POSITION; // The position this Book Model will correspond to. Will determine the
                                             // width of the model itself.
    public final float HEIGHT; // The height of the book, as determined by a random seed depending on the coordinates
                               // of the block and the seed of the world. Calculated to be on the correct shelf at
                               // object creation.
    public final float SHELF; // The height of the bottom of the book. Calculated to land on the correct shelf at object
                              // creation.
    public final float DEPTH; // The depth of the book, as determined in the same way as above.

    private final SpriteIdentifier HEAD; // The texture to use for the head quad.
    private final SpriteIdentifier COVER; // The texture to use for the rear cover, spine, and front cover quads.

    private final int HEAD_HORIZONTAL_OFFSET;

    public BookModel(BookPosition bookPosition, Random random){
        this.BOOK_POSITION = bookPosition;
        // Is the book on the top shelf?
        boolean topShelf = BOOK_POSITION.getQuadrant() == ShelfQuadrant.ALPHA || BOOK_POSITION.getQuadrant() == ShelfQuadrant.BETA;
        // Calculate a random height between MIN_HEIGHT and MAX_HEIGHT. Add .5 if the book is on the top shelf.
        this.HEIGHT = MIN_HEIGHT * random.nextFloat() * (MAX_HEIGHT-MIN_HEIGHT) + (topShelf ? .5f : 0f);
        // Set the height of SHELF.
        this.SHELF = topShelf ? 8f / 16f: 1f / 16f;
        // Calculate a random depth between MIN_DEPTH and MAX_DEPTH.
        this.DEPTH = MIN_DEPTH * random.nextFloat() * (MAX_DEPTH-MIN_DEPTH);
        // Calculate a horizontal offset for the head texture.
        this.HEAD_HORIZONTAL_OFFSET = abs(random.nextInt() % (32-BOOK_POSITION.PIXELS));
        // Grab random textures. HEAD, from the paper textures:
        this.HEAD = getRandomSprite(0, random);
        // and COVER, from the appropriate cover textures.
        this.COVER = getRandomSprite(BOOK_POSITION.PIXELS, random);

    }

    // TODO: Implement constructor.

    // Instead of trying to return a model, I'll simply get a method that takes in the emitter and returns nothing.
    // That way, I can simply tell the emitter to emit at the decided upon points and emit the quads, and be confident
    // that the book will be rendered.

    public void drawBook(QuadEmitter e, Function<SpriteIdentifier, Sprite> tg) {
        // TODO: Make the quads visible only from the correct side, if possible.
        // Get the sprites.
        Sprite head = tg.apply(HEAD);
        Sprite cover = tg.apply(COVER);
        // Draw the head quad.
        e.pos(0, BOOK_POSITION.getLeftEdge(), HEIGHT, Z_BACK_STOP);
        e.pos(1, BOOK_POSITION.getRightEdge(), HEIGHT, Z_BACK_STOP);
        e.pos(2, BOOK_POSITION.getRightEdge(), HEIGHT, DEPTH);
        e.pos(3, BOOK_POSITION.getLeftEdge(), HEIGHT, DEPTH);
        // Now that we've designated all four corners of the book, we need to set the sprite position for each vertex.
        // Calculate a random horizontal offset for the page texture. Don't go over the edge of the texture.
        e.sprite(0, 0, HEAD_HORIZONTAL_OFFSET, 0);
        e.sprite(1, 0, HEAD_HORIZONTAL_OFFSET + BOOK_POSITION.PIXELS, 0);
        // The texture dimensions don't change with the random sizing of the books. This causes the texture to be scaled
        // along with the book model.
        e.sprite(2, 0, HEAD_HORIZONTAL_OFFSET + BOOK_POSITION.PIXELS, 7);
        e.sprite(3, 0, HEAD_HORIZONTAL_OFFSET, 7);
        // Apply the texture and emit.
        e.spriteBake(0, head, MutableQuadView.BAKE_ROTATE_NONE).emit();
        // Now we render the rear cover. Vertex coordinates:
        e.pos(0, BOOK_POSITION.getLeftEdge(), HEIGHT, Z_BACK_STOP);
        e.pos(1, BOOK_POSITION.getLeftEdge(), HEIGHT, DEPTH);
        e.pos(2, BOOK_POSITION.getLeftEdge(), SHELF, DEPTH);
        e.pos(3, BOOK_POSITION.getLeftEdge(), SHELF, Z_BACK_STOP);
        // Sprite coordinates (No offset this time, as we already know where in the texture this face falls.)
        e.sprite(0, 0, 0, 0);
        e.sprite(1, 0, 7, 0);
        e.sprite(2, 0, 7, 7);
        e.sprite(3, 0, 0, 7);
        // Apply the cover texture and emit.
        e.spriteBake(0, cover, MutableQuadView.BAKE_ROTATE_NONE).emit();
        // Now for the spine. Vertices:
        e.pos(0, BOOK_POSITION.getLeftEdge(), HEIGHT, DEPTH);
        e.pos(1, BOOK_POSITION.getRightEdge(), HEIGHT, DEPTH);
        e.pos(2, BOOK_POSITION.getRightEdge(), SHELF, DEPTH);
        e.pos(3, BOOK_POSITION.getLeftEdge(), SHELF, DEPTH);
        // Sprite Coordinates:
        e.sprite(0, 0, 7, 0);
        e.sprite(1, 0, 7 + BOOK_POSITION.PIXELS, 0);
        e.sprite(2, 0, 7 + BOOK_POSITION.PIXELS, 7);
        e.sprite(3, 0, 7, 7);
        // Apply the cover texture and emit.
        e.spriteBake(0, cover, MutableQuadView.BAKE_ROTATE_NONE).emit();
        // Lastly, we have the front cover. Vertices:
        e.pos(0, BOOK_POSITION.getRightEdge(), HEIGHT, DEPTH);
        e.pos(1, BOOK_POSITION.getRightEdge(), HEIGHT, Z_BACK_STOP);
        e.pos(2, BOOK_POSITION.getRightEdge(), SHELF, Z_BACK_STOP);
        e.pos(3, BOOK_POSITION.getRightEdge(), SHELF, DEPTH);
        // Sprite coordinates:
        e.sprite(0, 0, 7 + BOOK_POSITION.PIXELS, 0);
        e.sprite(1, 0, 7 + BOOK_POSITION.PIXELS + 7, 0);
        e.sprite(2, 0, 7 + BOOK_POSITION.PIXELS + 7, 7);
        e.sprite(3, 0, 7 + BOOK_POSITION.PIXELS, 7);
        // For the final time in this book, apply the cover texture and emit.
        e.spriteBake(0, cover, MutableQuadView.BAKE_ROTATE_NONE).emit();
    }
}
