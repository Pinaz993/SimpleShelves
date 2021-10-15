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

/* Represents the four quads that need to be created to render a book on a shelf. Contains a constructor that randomly
   determines the book's variable qualities, and a method for emitting the proper quads. 12 of these objects exist for
   every shelf in client-loaded chunks. Be ware.
 */
@SuppressWarnings("deprecation") //Because Mojang abuses @Deprecated. Not the smartest practice, I'll say. Again.
public class BookModel {
    //<editor-fold desc="Static Stuff">
    // Arrays of strings for finding textures.
    private static final String[] ONE_PX_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_one_px_alpha"
    };
    private static final String[] TWO_PX_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_two_px_alpha",
            "simple_shelves:block/book_two_px_beta",
            "simple_shelves:block/book_two_px_gamma",
            "simple_shelves:block/book_two_px_delta"
    };
    private static final String[] THREE_PX_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_three_px_alpha",
            "simple_shelves:block/book_three_px_beta",
            "simple_shelves:block/book_three_px_gamma",
            "simple_shelves:block/book_three_px_delta",
            "simple_shelves:block/book_three_px_epsilon"
    };
    private static final String[] FOUR_PX_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_four_px_alpha",
            "simple_shelves:block/book_four_px_beta",
    };
    private static final String[] PAPER_TEXTURE_NAMES = new String[] {
            "simple_shelves:block/book_paper"
    };

    // The direction the various quads should be visible from. I don't know how to use these yet.
    private static final Direction HEAD_DIRECTION = Direction.UP; // Direction for the head (or top edge) quad.
    private static final Direction REAR_COVER_DIRECTION = Direction.WEST; // Direction for the rear cover quad.
    private static final Direction SPINE_DIRECTION = Direction.SOUTH; // Direction for the spine quad.
    private static final Direction FRONT_COVER_DIRECTION = Direction.EAST; // Direction for the front cover quad.

    // The minimum and maximum values for the variable dimensions of a book. A pixel is 1/16m, or .0625. Variables so I
    // can adjust them later if need be.
    private static final float MIN_HEIGHT = (float) 6 / 16;
    private static final float MAX_HEIGHT = (float) 7 / 16;
    private static final float MIN_DEPTH = (float) 6 / 16;
    private static final float MAX_DEPTH = (float) 7 / 16;

    // The Z coordinate of the front surface of the rear plate of the shelf. This is as low in Z as a vertex can be.
    private static final float Z_BACK_STOP = (float) 1 / 16;

    /**
     * Chooses a random texture from the lists above.
     * This is very complicated, but it's also very compact. I've yet to decide if the latter is a good thing.
     * @param list: An integer between 0 and 4 inclusive. 0 indicates the paper list, 1-4 indicate the list for that
     *            amount of pixels.
     * @param random: An RNG seeded with the world seed and the shelf's block position.
     * @return The sprite identifier for a texture that will be used, either for the cover of the book, or its head.
     */
    private static SpriteIdentifier getRandomSprite(int list, Random random) {
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
    //</editor-fold>

    // The height of the book, as determined by an RNG seeded with the coordinates of the block and the seed of the
    // world. Calculated to be on the correct shelf at object creation.
    private final float HEIGHT;
    // The height of the bottom of the book. Calculated to land on the correct shelf at object creation.
    private final float SHELF;
    private final float DEPTH; // The depth of the book, as determined in the same way as HEIGHT above.

    private final float LEFT_EDGE; // The X coordinate of the left edge of the spine, AKA the rear cover.
    private final float RIGHT_EDGE; // The X coordinate of the right edge of the spine, AKA the front cover.
    private final int PIXELS; // The width of the book in pixels. 1-4 are the valid values.

    private final SpriteIdentifier HEAD; // The texture to use for the head quad.
    private final SpriteIdentifier COVER; // The texture to use for the rear cover, spine, and front cover quads.

    private final int HEAD_HORIZONTAL_OFFSET; // An offset to make the paper look different on different books.

    /**
     * Given a BookPosition to occupy, randomly determines the height and depth of the book (within static limits), and
     * the textures to be displayed on all sides of the book. May need to determine more beforehand to speed up quad
     * emission.
     */
    public BookModel(BookPosition bookPosition, Random random){
        boolean topShelf = bookPosition.getQuadrant() == ShelfQuadrant.ALPHA || // Is the book on the top shelf?
                bookPosition.getQuadrant() == ShelfQuadrant.BETA;
        // Calculate a random height between MIN_HEIGHT and MAX_HEIGHT. Add .5 if the book is on the top shelf.
        this.HEIGHT = MIN_HEIGHT * random.nextFloat() * (MAX_HEIGHT-MIN_HEIGHT) + (topShelf ? .5f : 0f);
        this.SHELF = topShelf ? 8f / 16f: 1f / 16f; // Set the height of SHELF.
        // Calculate a random depth between MIN_DEPTH and MAX_DEPTH.
        this.DEPTH = MIN_DEPTH * random.nextFloat() * (MAX_DEPTH-MIN_DEPTH);
        // Calculate a horizontal offset for the head texture.
        this.HEAD_HORIZONTAL_OFFSET = abs(random.nextInt() % (32-bookPosition.PIXELS));
        // Get the edge locations and properties from bookPosition.
        this.LEFT_EDGE = bookPosition.getLeftEdge();
        this.RIGHT_EDGE = bookPosition.getRightEdge();
        this.PIXELS = bookPosition.PIXELS;
        this.HEAD = getRandomSprite(0, random);            // Grab random textures. HEAD, from the paper textures
        this.COVER = getRandomSprite(bookPosition.PIXELS, random);  // and COVER, from the appropriate cover textures.
    }

    /**
     * Takes the info determined at object creation and uses it to create and emit all four quads for this book.
     * May need refining to take less time to emit the quads, as this may be running 12 times for each shelf every time
     * that a chunk is updated client-side. This needs to be fast to avoid lag-spikes.
     * Of course, I might be able to cache a baked model until something changes in this shelf, but that's not
     * guaranteed.
     * @param e The QuadEmitter that keeps track of all the vertices in a quad and emits the quads to the mesh.
     * @param tg: A function for getting sprites from the atlas. Copied from the tutorial.
     */
    public void emitBookQuads(QuadEmitter e, Function<SpriteIdentifier, Sprite> tg) {
        // All points need to be ordered CCW to make sure that they render on the correct side.
        // I wish I had known that already.
        // Get the sprites.
        Sprite head = tg.apply(HEAD);
        Sprite cover = tg.apply(COVER);
        // Draw the head quad.
        e.pos(0, LEFT_EDGE, HEIGHT, Z_BACK_STOP);
        e.pos(1, LEFT_EDGE, HEIGHT, DEPTH);
        e.pos(2, RIGHT_EDGE, HEIGHT, DEPTH);
        e.pos(3, RIGHT_EDGE, HEIGHT, Z_BACK_STOP);
        // Now that we've designated all four corners of the book, we need to set the sprite position for each vertex.
        // Calculate a random horizontal offset for the page texture. Don't go over the edge of the texture.
        e.sprite(0, 0, HEAD_HORIZONTAL_OFFSET, 0);
        e.sprite(1, 0, HEAD_HORIZONTAL_OFFSET, 7);
        // The texture dimensions don't change with the random sizing of the books. This causes the texture to be scaled
        // along with the book model.
        e.sprite(2, 0, HEAD_HORIZONTAL_OFFSET + PIXELS, 7);
        e.sprite(3, 0, HEAD_HORIZONTAL_OFFSET + PIXELS, 0);
        // Enable texture usage
        e.spriteColor(0, -1, -1, -1, -1);
        // Apply the paper texture and emit.
        e.spriteBake(0, head, MutableQuadView.BAKE_ROTATE_NONE).emit();
        // Now we render the rear cover. Vertex coordinates:
        e.pos(0, LEFT_EDGE, HEIGHT, Z_BACK_STOP);
        e.pos(1, LEFT_EDGE, SHELF, Z_BACK_STOP);
        e.pos(2, LEFT_EDGE, SHELF, DEPTH);
        e.pos(3, LEFT_EDGE, HEIGHT, DEPTH);
        // Sprite coordinates (No offset this time, as we already know where in the texture this face falls.)
        e.sprite(0, 0, 0, 0);
        e.sprite(1, 0, 0, 7);
        e.sprite(2, 0, 7, 7);
        e.sprite(3, 0, 7, 0);
        // Enable texture usage
        e.spriteColor(0, -1, -1, -1, -1);
        // Apply the cover texture and emit.
        e.spriteBake(0, cover, MutableQuadView.BAKE_ROTATE_NONE).emit();
        // Now for the spine. Vertices:
        e.pos(0, LEFT_EDGE, HEIGHT, DEPTH);
        e.pos(1, LEFT_EDGE, SHELF, DEPTH);
        e.pos(2, RIGHT_EDGE, SHELF, DEPTH);
        e.pos(3, RIGHT_EDGE, HEIGHT, DEPTH);
        // Sprite Coordinates:
        e.sprite(0, 0, 7, 0);
        e.sprite(1, 0, 7, 7);
        e.sprite(2, 0, 7 + PIXELS, 7);
        e.sprite(3, 0, 7 + PIXELS, 0);
        // Enable texture usage
        e.spriteColor(0, -1, -1, -1, -1);
        // Apply the cover texture and emit.
        e.spriteBake(0, cover, MutableQuadView.BAKE_ROTATE_NONE).emit();
        // Lastly, we have the front cover. Vertices:
        e.pos(0, RIGHT_EDGE, HEIGHT, DEPTH);
        e.pos(1, RIGHT_EDGE, SHELF, DEPTH);
        e.pos(2, RIGHT_EDGE, SHELF, Z_BACK_STOP);
        e.pos(3, RIGHT_EDGE, HEIGHT, Z_BACK_STOP);
        // Sprite coordinates:
        e.sprite(0, 0, 7 + PIXELS, 0);
        e.sprite(1, 0, 7 + PIXELS, 7);
        e.sprite(2, 0, 7 + PIXELS + 7, 7);
        e.sprite(3, 0, 7 + PIXELS + 7, 0);
        // Enable texture usage
        e.spriteColor(0, -1, -1, -1, -1);
        // For the final time for this book, apply the cover texture and emit.
        e.spriteBake(0, cover, MutableQuadView.BAKE_ROTATE_NONE).emit();
    }
}
