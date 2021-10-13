package net.pinaz993.simpleshelves.client;

import net.minecraft.client.util.SpriteIdentifier;
import net.pinaz993.simpleshelves.BookPosition;

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
    // HEAD (Upwards facing part of the book that shows the edges of the book's pages.) (The other two faces of the
    // rectangular prism are not rendered, as they butt against the shelf and wouldn't be visible.)
    // I expect to be using JSON model loading to handle horizontal orientation, so I won't need to worry about NESW
    // (or +/-X, +/-Z) orientation.
    // I'll also handle keeping track of the textures in here. Fields for the IDs of those.

    // TODO: Determine what information will need to be present in this class to render the four quads.
    // Quad orientation? Points?

    public final BookPosition BOOK_POSITION; // The position this Book Model will correspond to. Will determine the
                                             // width of the model itself.
    public final double HEIGHT; // The height of the book, as determined by a random seed depending on the coordinates
                                // of the block and the seed of the world.
    public final double DEPTH; // The depth of the book, as determined in the same way as above.

    public final SpriteIdentifier COVER_SPRITE_ID; // The ID for the texture that will be applied to the REAR_COVER,
                                                   // SPINE, and FRONT_COVER quads.

    public final SpriteIdentifier HEAD_SPRITE_ID; // The ID for the texture that will be applied to the HEAD quad.

    // TODO: Implement constructor.

    // TODO: Implement a method that returns a baked or unbaked model for the book.
    // This model will be included in the final baked model for the shelf.
}
