package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Vec2f;
import net.pinaz993.simpleshelves.BookPosition;
import net.pinaz993.simpleshelves.ShelfQuadrant;

import java.util.List;

@Environment(EnvType.CLIENT)
public enum BookQuadEmitter {
    ALPHA_1(BookPosition.ALPHA_1, 6.74f, 6.8f, 0b000_000_000_001,
            new Vec2f(0, 0), new Vec2f(54/4f, 6/2f)),

    ALPHA_2(BookPosition.ALPHA_2, 6.52f, 7.0f, 0b000_000_000_010,
            new Vec2f(17/16f, 0), new Vec2f(51/4f, 13/2f)),

    ALPHA_3(BookPosition.ALPHA_3, 7.13f, 6.6f, 0b000_000_000_100,
            new Vec2f(32/16f, 0), new Vec2f(52/4f, 2/2f)),

    BETA_1 (BookPosition.BETA_1,  6.07f, 6.5f, 0b000_000_001_000,
            new Vec2f(0, 7/16f), new Vec2f(51/4f, 4/2f)),

    BETA_2 (BookPosition.BETA_2,  5.97f, 6.4f, 0b000_000_010_000,
            new Vec2f(16/16f, 7/16f), new Vec2f(50/4f, 11/2f)),

    BETA_3 (BookPosition.BETA_3,  6.25f, 7.4f, 0b000_000_100_000,
            new Vec2f(33/16f, 7/16f), new Vec2f(51/4f, 14/2f)),

    GAMMA_1(BookPosition.GAMMA_1, 6.99f, 5.7f, 0b000_001_000_000,
            new Vec2f(0, 14/16f), new Vec2f(52/4f, 9/2f)),

    GAMMA_2(BookPosition.GAMMA_2, 6.62f, 6.2f, 0b000_010_000_000,
            new Vec2f(16/16f, 14/16f), new Vec2f(51/4f, 16/2f)),

    GAMMA_3(BookPosition.GAMMA_3, 6.79f, 6.6f, 0b000_100_000_000,
            new Vec2f(34/16f, 14/16f), new Vec2f(53/4f, 2/2f)),

    DELTA_1(BookPosition.DELTA_1, 5.75f, 6.0f, 0b001_000_000_000,
            new Vec2f(0, 21/16f), new Vec2f(51/4f, 8/2f)),

    DELTA_2(BookPosition.DELTA_2, 6.76f, 7.2f, 0b010_000_000_000,
            new Vec2f(17/17f, 21/16f), new Vec2f(52/4f, 1/2f)),

    DELTA_3(BookPosition.DELTA_3, 6.65f, 6.2f, 0b100_000_000_000,
            new Vec2f(33/16f, 21/16f), new Vec2f(51/4f, 1/2f));

    // The Z coordinate of the front surface of the rear plate of the shelf. This is as low in Z as a vertex can be.
    private static final float Z_BACK_STOP = 1/16f;

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

    BookQuadEmitter(BookPosition bookPosition, float height, float depth, int bitFlag,
                    Vec2f coverUpperLeftCorner, Vec2f headUpperLeftCorner){
        this.POSITION = bookPosition;
        this.HEIGHT = height/16f;
        this.DEPTH = depth/16f;
        this.BIT_FLAG = bitFlag;
        // Texture Offsets:
        final float DEPTH_TEXTURE_OFFSET = 7/4f; // The covers of all books are 7 pixels wide.
        final float HEIGHT_TEXTURE_OFFSET = 7/2f; // All books are 7 pixels tall.
        final float FRONT_OFFSET = DEPTH_TEXTURE_OFFSET + POSITION.WIDTH/2; // The width of the rear cover plus the width of the spine.
        final float RIGHT_EDGE_OFFSET = FRONT_OFFSET + DEPTH_TEXTURE_OFFSET; // The width of both covers plus the width of the spine.
        // Calculate the UV coordinates for the sprite once per book model.
        // Defining all of those constants may seem excessive, but I am not about to allow magic numbers to creep into my code.
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

    public void emitBookQuads(QuadEmitter e, Sprite s) {
        // Get the left edge, and right edge and bottom coordinates
        ShelfQuadrant q = POSITION.getQuadrant();
        float left = POSITION.getLeftEdge(q);
        float right = POSITION.getRightEdge(q);
        // If the book is in the alpha or beta quadrants (AKA top shelf),
        // the bottom of the book should be at 9/16. Else, 1/16.
        // I'm pretty sure the List gets marked for garbage collection after this line.
        float shelf = List.of(ShelfQuadrant.ALPHA, ShelfQuadrant.BETA).contains(q) ? 9/16f : 1/16f;
        float top = HEIGHT + shelf;
        float front = DEPTH + Z_BACK_STOP;
        // First, the head face.
        e.pos(0, 0, top, Z_BACK_STOP);
        e.pos(1, 0, top, front);
        e.pos(2, 3/16f, top, front);
        e.pos(3, 3/16f, top, Z_BACK_STOP);
        // Sprite work:
        e.sprite(0, 0, HEAD_UV_V0);
        e.sprite(3, 0, HEAD_UV_V1);
        e.sprite(2, 0, HEAD_UV_V2);
        e.sprite(1, 0, HEAD_UV_V3);
        e.spriteBake(0, s, MutableQuadView.BAKE_ROTATE_NONE);
        e.spriteColor(0, -1, -1, -1, -1); // Enable texture usage
        // Add the quad to the mesh
        e.emit();

        // Next, the rear cover.
        e.pos(0, left, top, Z_BACK_STOP);
        e.pos(1, left, shelf, Z_BACK_STOP);
        e.pos(2, left, shelf, front);
        e.pos(3, left, top, front);
        // Sprite work:
        e.sprite(0, 0, REAR_UV_V0);
        e.sprite(1, 0, REAR_UV_V1);
        e.sprite(2, 0, REAR_UV_V2);
        e.sprite(3, 0, REAR_UV_V3);
        e.spriteBake(0, s, MutableQuadView.BAKE_ROTATE_NONE);
        e.spriteColor(0, -1, -1, -1, -1); // Enable texture usage
        // Add the quad to the mesh
        e.emit();

        // Now, the spine.
        e.pos(0, left, top, front);
        e.pos(1, left, shelf, front);
        e.pos(2, right, shelf, front);
        e.pos(3, right, top, front);
        // Sprite work:
        e.sprite(0, 0, SPINE_UV_V0);
        e.sprite(1, 0, SPINE_UV_V1);
        e.sprite(2, 0, SPINE_UV_V2);
        e.sprite(3, 0, SPINE_UV_V3);
        e.spriteBake(0, s, MutableQuadView.BAKE_ROTATE_NONE);
        e.spriteColor(0, -1, -1, -1, -1); // Enable texture usage
        // Add the quad to the mesh
        e.emit();

        // Finally, the front cover.
        e.pos(0, right, top, front);
        e.pos(1, right, shelf, front);
        e.pos(2, right, shelf, Z_BACK_STOP);
        e.pos(3, right, top, Z_BACK_STOP);
        // Sprite work
        e.sprite(0, 0, FRONT_UV_V0);
        e.sprite(1, 0, FRONT_UV_V1);
        e.sprite(2, 0, FRONT_UV_V2);
        e.sprite(3, 0, FRONT_UV_V3);
        e.spriteBake(0, s, MutableQuadView.BAKE_ROTATE_NONE);
        e.spriteColor(0, -1, -1, -1, -1); // Enable texture usage
        // Add the quad to the mesh
        e.emit();
    }
}
