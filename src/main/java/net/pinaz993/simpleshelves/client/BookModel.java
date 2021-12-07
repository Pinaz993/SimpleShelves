package net.pinaz993.simpleshelves.client;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.pinaz993.simpleshelves.BookPosition;
import net.pinaz993.simpleshelves.ShelfQuadrant;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static net.minecraft.util.math.MathHelper.abs;

/* Represents the four quads that need to be created to render a book on a shelf. Contains a constructor that randomly
   determines the book's variable qualities, and a method for emitting the proper quads. 12 of these objects exist for
   every shelf in client-loaded chunks. Be ware.
 */
public class BookModel {

    //<editor-fold desc="Static Stuff">
    // The minimum and maximum values for the variable dimensions of a book. A pixel is 1/16m, or .0625. Variables so I
    // can adjust them later if need be.
    private static final float MIN_HEIGHT = (float) 6 / 16;
    private static final float MAX_HEIGHT = (float) 7 / 16;
    private static final float MIN_DEPTH = (float) 6 / 16;
    private static final float MAX_DEPTH = (float) 7 / 16;

    // The Z coordinate of the front surface of the rear plate of the shelf. This is as low in Z as a vertex can be.
    private static final float Z_BACK_STOP = (float) 1 / 16;
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

    private boolean enabled = false; // Should this book be rendered?

    /**
     * Given a BookPosition to occupy, randomly determines the height and depth of the book (within static limits), and
     * the textures to be displayed on all sides of the book. May need to determine more beforehand to speed up quad
     * emission.
     * @param random: A Random with a seed that depends on the seed of the world and the block position of the shelf.
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
        // Grab random textures for the head of the book and its cover.
        this.HEAD = BookTextureList.PAPER.getRandomSpriteID(random);
        this.COVER = BookTextureList.getCoverTexture(PIXELS).getRandomSpriteID(random);
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
        // Use the randomly chosen horizontal offset for the page texture.
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

    public boolean isEnabled() {return enabled;}

    public void setEnabled(boolean enabled) {this.enabled = enabled;}

    /**
     * Perhaps this is overkill to avoid magic numbers, but I'm fastidious. Give me a break.
     * Stores the string identifiers for each texture, and can dish out a random texture from a given list.
     */
    private enum BookTextureList{
        PAPER(List.of(
                getSpriteID("simple_shelves:block/book_paper")
        )),
        ONE_PIXEL(List.of(
                getSpriteID("simple_shelves:block/book_one_px_alpha")
        )),
        TWO_PIXEL(List.of(
                getSpriteID("simple_shelves:block/book_two_px_alpha"),
                getSpriteID("simple_shelves:block/book_two_px_beta"),
                getSpriteID("simple_shelves:block/book_two_px_gamma"),
                getSpriteID("simple_shelves:block/book_two_px_delta")
        )),
        THREE_PIXEL(List.of(
                getSpriteID("simple_shelves:block/book_three_px_alpha"),
                getSpriteID("simple_shelves:block/book_three_px_beta"),
                getSpriteID("simple_shelves:block/book_three_px_gamma"),
                getSpriteID("simple_shelves:block/book_three_px_delta"),
                getSpriteID("simple_shelves:block/book_three_px_epsilon")
        )),
        FOUR_PIXEL(List.of(
                getSpriteID("simple_shelves:block/book_four_px_alpha"),
                getSpriteID("simple_shelves:block/book_four_px_beta")
        ));

        /**
         * Grab this texture for me, would you?
         */
        private static SpriteIdentifier getSpriteID (String s){
            return new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier(s));
        }

        /**
         * @param pixels the width of the book model in pixels,
         * @return the proper texture list.
         */
        static BookTextureList getCoverTexture(int pixels){
            return switch (pixels) {
                case 1 -> ONE_PIXEL;
                case 2 -> TWO_PIXEL;
                case 3 -> THREE_PIXEL;
                case 4 -> FOUR_PIXEL;
                default -> throw new IllegalArgumentException(String.format(
                        "Pixel width value out of range: %s is not between 1 and 4 inclusive.", pixels
                ));
            };
        }

        // The titular list.
        final List<SpriteIdentifier> LIST;

        BookTextureList(List<SpriteIdentifier> list) {
            this.LIST = list;
        }

        /**
         * Given a source of randomness, return a random texture from the list.
         */
        SpriteIdentifier getRandomSpriteID(Random random) {
            return LIST.get((int) (Math.ceil(random.nextDouble() % 1) * LIST.size()));
        }
    }
}
