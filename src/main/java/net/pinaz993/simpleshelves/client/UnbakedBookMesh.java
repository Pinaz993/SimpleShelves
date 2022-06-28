package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.pinaz993.simpleshelves.BookPosition;
import net.pinaz993.simpleshelves.ShelfQuadrant;

import java.util.List;

@Environment(EnvType.CLIENT)
public enum UnbakedBookMesh {
    ALPHA_1(BookPosition.ALPHA_1, 6.74f, 6.8f, 0b000_000_000_001,
            new Vec2f(0, 0), new Vec2f(6/16f, 1/16f)),
    ALPHA_2(BookPosition.ALPHA_2, 6.52f, 7.0f, 0b000_000_000_010,
            new Vec2f(17/16f, 0), new Vec2f(6/16f, 1/16f)),
    ALPHA_3(BookPosition.ALPHA_3, 7.13f, 6.6f, 0b000_000_000_100,
            new Vec2f(32/16f, 0), new Vec2f(11/16f, 1/16f)),
    BETA_1 (BookPosition.BETA_1,  6.07f, 6.5f, 0b000_000_001_000,
            new Vec2f(0, 7/16f), new Vec2f(20/16f, 1/16f)),
    BETA_2 (BookPosition.BETA_2,  5.97f, 6.4f, 0b000_000_010_000,
            new Vec2f(16/16f, 7/16f), new Vec2f(2/16f, 1/16f)),
    BETA_3 (BookPosition.BETA_3,  6.25f, 7.4f, 0b000_000_100_000,
            new Vec2f(33/16f, 7/16f), new Vec2f(19/16f, 1/16f)),
    GAMMA_1(BookPosition.GAMMA_1, 6.99f, 5.7f, 0b000_001_000_000,
            new Vec2f(0, 14/16f), new Vec2f(3/16f, 1/16f)),
    GAMMA_2(BookPosition.GAMMA_2, 6.62f, 6.2f, 0b000_010_000_000,
            new Vec2f(16/16f, 14/16f), new Vec2f(5/16f, 1/16f)),
    GAMMA_3(BookPosition.GAMMA_3, 6.79f, 6.6f, 0b000_100_000_000,
            new Vec2f(34/16f, 14/16f), new Vec2f(13/16f, 1/16f)),
    DELTA_1(BookPosition.DELTA_1, 5.75f, 6.0f, 0b001_000_000_000,
            new Vec2f(0, 21/16f), new Vec2f(3/16f, 1/16f)),
    DELTA_2(BookPosition.DELTA_2, 6.76f, 7.2f, 0b010_000_000_000,
            new Vec2f(17/17f, 21/16f), new Vec2f(10/16f, 1/16f)),
    DELTA_3(BookPosition.DELTA_3, 6.65f, 6.2f, 0b100_000_000_000,
            new Vec2f(33/16f, 21/16f), new Vec2f(12/16f, 1/16f));

    // The Z coordinate of the front surface of the rear plate of the shelf. This is as low in Z as a vertex can be.
    private static final float Z_BACK_STOP = 1 / 16f;
    // The identifier that allows us to fetch the sprite when it comes time to render the books.
    private static final SpriteIdentifier SPRITE_ID = new SpriteIdentifier(
            PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
            new Identifier("simple_shelves:block/shelf_books")
    );

    public final  BookPosition POSITION;
    public final float HEIGHT; // Length in meters from top to bottom
    public final float DEPTH; // Length in meters from spine to rear shelf
    public final int BIT_FLAG; // A bit mask for use in quickly determining which books to render client-side
    // This group of vectors represent the UV coordinates for every vertex of every quad in this model. They only need
    // to be calculated once, so I made them final.
    public final Vec2f HEAD_UV_V0, HEAD_UV_V1, HEAD_UV_V2, HEAD_UV_V3,
                       REAR_UV_V0, REAR_UV_V1, REAR_UV_V2, REAR_UV_V3,
                       SPINE_UV_V0, SPINE_UV_V1, SPINE_UV_V2, SPINE_UV_V3,
                       FRONT_UV_V0, FRONT_UV_V1, FRONT_UV_V2, FRONT_UV_V3;

    UnbakedBookMesh(BookPosition bookPosition, float height, float depth, int bitFlag,
                    Vec2f coverUpperLeftCorner, Vec2f headUpperLeftCorner){
        this.POSITION = bookPosition;
        this.HEIGHT = height/16f;
        this.DEPTH = depth/16f;
        this.BIT_FLAG = bitFlag;
        // Offsets:
        final float DEPTH_TEXTURE_OFFSET = 7/16f; // The covers of all books are 7 pixels wide.
        final float HEIGHT_TEXTURE_OFFSET = DEPTH_TEXTURE_OFFSET; // All books are as tall as their covers are wide.
        final float FRONT_OFFSET = DEPTH_TEXTURE_OFFSET + POSITION.WIDTH; // The width of the rear cover plus the width of the spine.
        final float RIGHT_EDGE_OFFSET = FRONT_OFFSET + DEPTH_TEXTURE_OFFSET; // The width of both covers plus the width of the spine.
        // Defining all of those constants may seem excessive, but I am not about to allow magic numbers to creep into my code.
        // Calculate the UV coordinates for the sprite once per book model.
        // Head UVs
        this.HEAD_UV_V0 = headUpperLeftCorner; // Upper Left
        this.HEAD_UV_V1 = new Vec2f( headUpperLeftCorner.x,
                headUpperLeftCorner.y + HEIGHT_TEXTURE_OFFSET); // Lower Left
        this.HEAD_UV_V2 = new Vec2f(headUpperLeftCorner.x + POSITION.WIDTH,
                headUpperLeftCorner.y + HEIGHT_TEXTURE_OFFSET); // Lower Right
        this.HEAD_UV_V3 = new Vec2f(headUpperLeftCorner.x + POSITION.WIDTH,
                headUpperLeftCorner.y); // Upper Right
        // Rear Cover UVs
        this.REAR_UV_V0 = coverUpperLeftCorner; // Upper Left
        this.REAR_UV_V1 = new Vec2f(coverUpperLeftCorner.x,
                coverUpperLeftCorner.y + HEIGHT_TEXTURE_OFFSET); // Lower Left
        this.REAR_UV_V2 = new Vec2f(coverUpperLeftCorner.x + DEPTH_TEXTURE_OFFSET,
                coverUpperLeftCorner.y + HEIGHT_TEXTURE_OFFSET); // Lower Right
        this.REAR_UV_V3 = new Vec2f(coverUpperLeftCorner.x + DEPTH_TEXTURE_OFFSET,
                coverUpperLeftCorner.y); // Upper Right
        // Spine UVs
        this.SPINE_UV_V0 = new Vec2f(coverUpperLeftCorner.x + DEPTH_TEXTURE_OFFSET,
                coverUpperLeftCorner.y); // Upper Left
        this.SPINE_UV_V1 = new Vec2f(coverUpperLeftCorner.x + DEPTH_TEXTURE_OFFSET,
                coverUpperLeftCorner.y + HEIGHT_TEXTURE_OFFSET); // Lower Left
        this.SPINE_UV_V2 = new Vec2f(coverUpperLeftCorner.x+ FRONT_OFFSET,
                coverUpperLeftCorner.y + HEIGHT_TEXTURE_OFFSET); // Lower Right
        this.SPINE_UV_V3 = new Vec2f(coverUpperLeftCorner.x + FRONT_OFFSET,
                coverUpperLeftCorner.y); // Upper Right

        this.FRONT_UV_V0 = new Vec2f(coverUpperLeftCorner.x + FRONT_OFFSET,
                coverUpperLeftCorner.y);// Upper Left
        this.FRONT_UV_V1 = new Vec2f(coverUpperLeftCorner.x + FRONT_OFFSET,
                coverUpperLeftCorner.y + HEIGHT_TEXTURE_OFFSET); // Lower Left
        this.FRONT_UV_V2 = new Vec2f(coverUpperLeftCorner.x + RIGHT_EDGE_OFFSET,
                coverUpperLeftCorner.y + HEIGHT_TEXTURE_OFFSET); // Lower Right
        this.FRONT_UV_V3 = new Vec2f(coverUpperLeftCorner.x + RIGHT_EDGE_OFFSET,
                coverUpperLeftCorner.y); // Upper Right
    }


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
        // Set the UV coords


        // Enable texture usage
        e.spriteColor(0, -1, -1, -1, -1);
        // Add the quad to the mesh
        e.emit();

        // Next, the rear cover.
        e.pos(0, left, HEIGHT, Z_BACK_STOP);
        e.pos(1, left, shelf, Z_BACK_STOP);
        e.pos(2, left, shelf, DEPTH);
        e.pos(3, left, HEIGHT, DEPTH);
        // Sprite work:

        // Enable texture usage
        e.spriteColor(0, -1, -1, -1, -1);
        // Add the quad to the mesh
        e.emit();

        // Now, the spine.
        e.pos(0, left, HEIGHT, DEPTH);
        e.pos(1, left, shelf, DEPTH);
        e.pos(2, right, shelf, DEPTH);
        e.pos(3, right, HEIGHT, DEPTH);
        // Sprite work:

        // Enable texture usage
        e.spriteColor(0, -1, -1, -1, -1);
        // Add the quad to the mesh
        e.emit();

        // Finally, the front cover.
        e.pos(0, right, HEIGHT, DEPTH);
        e.pos(1, right, shelf, DEPTH);
        e.pos(2, right, shelf, Z_BACK_STOP);
        e.pos(3, right, HEIGHT, Z_BACK_STOP);
        // Sprite work

        // Enable texture usage
        e.spriteColor(0, -1, -1, -1, -1);
        // Add the quad to the mesh
        e.emit();
    }
}
