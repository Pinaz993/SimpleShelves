package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.pinaz993.simpleshelves.BookPosition;
import net.pinaz993.simpleshelves.ShelfQuadrant;

import java.util.List;

@Environment(EnvType.CLIENT)
public enum UnbakedBookModel {
    ALPHA_1(BookPosition.ALPHA_1, 6.74f, 6.8f, 0b000_000_000_001),
    ALPHA_2(BookPosition.ALPHA_2, 6.52f, 7.0f, 0b000_000_000_010),
    ALPHA_3(BookPosition.ALPHA_3, 7.13f, 6.6f, 0b000_000_000_100),
    BETA_1 (BookPosition.BETA_1,  6.07f, 6.5f, 0b000_000_001_000),
    BETA_2 (BookPosition.BETA_2,  5.97f, 6.4f, 0b000_000_010_000),
    BETA_3 (BookPosition.BETA_3,  6.25f, 7.4f, 0b000_000_100_000),
    GAMMA_1(BookPosition.GAMMA_1, 6.99f, 5.7f, 0b000_001_000_000),
    GAMMA_2(BookPosition.GAMMA_2, 6.62f, 6.2f, 0b000_010_000_000),
    GAMMA_3(BookPosition.GAMMA_3, 6.79f, 6.6f, 0b000_100_000_000),
    DELTA_1(BookPosition.DELTA_1, 5.75f, 6.0f, 0b001_000_000_000),
    DELTA_2(BookPosition.DELTA_2, 6.76f, 7.2f, 0b010_000_000_000),
    DELTA_3(BookPosition.DELTA_3, 6.65f, 6.2f, 0b100_000_000_000);

    // The Z coordinate of the front surface of the rear plate of the shelf. This is as low in Z as a vertex can be.
    private static final float Z_BACK_STOP = 1 / 16f;
    // The identifier that allows us to fetch the sprite when it comes time to render the books.
    private static final SpriteIdentifier SPRITE_ID = new SpriteIdentifier(
            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
            new Identifier("simple_shelves:block/shelf_books")
    );

    UnbakedBookModel(BookPosition bookPosition, float height, float depth, int bitFlag){
        this.POSITION = bookPosition;
        this.HEIGHT = height/16f;
        this.DEPTH = depth/16f;
        this.BIT_FLAG = bitFlag;
    }

    public final  BookPosition POSITION;
    public final float HEIGHT; // Length in meters from top to bottom
    public final float DEPTH; // Length in meters from spine to rear shelf
    public final int BIT_FLAG; // A bit mask for use in quickly determining which books to render client-side


    public void emitBookQuads(QuadEmitter e) {
        // Get the left edge, and right edge and bottom coordinates
        ShelfQuadrant q = POSITION.getQuadrant();
        float left = POSITION.getLeftEdge(q);
        float right = POSITION.getRightEdge(q);
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
        e.spriteBake()

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
