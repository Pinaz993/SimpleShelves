package net.pinaz993.simpleshelves.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.pinaz993.simpleshelves.BookPosition;

@Environment(EnvType.CLIENT)
public enum BookModelKey {
    /**
     * Keeps track of all the data needed to load the models for each book position, and the masks needed to decide if
     * they need to be rendered given data from the block entity.
     */
    ALPHA_1(BookPosition.ALPHA_1, "book_alpha_1", 0b000_000_000_001),

    ALPHA_2(BookPosition.ALPHA_2, "book_alpha_2", 0b000_000_000_010),

    ALPHA_3(BookPosition.ALPHA_3, "book_alpha_3", 0b000_000_000_100),

    BETA_1 (BookPosition.BETA_1,  "book_beta_1",  0b000_000_001_000),

    BETA_2 (BookPosition.BETA_2,  "book_beta_2",  0b000_000_010_000),

    BETA_3 (BookPosition.BETA_3,  "book_beta_3",  0b000_000_100_000),

    GAMMA_1(BookPosition.GAMMA_1, "book_gamma_1", 0b000_001_000_000),

    GAMMA_2(BookPosition.GAMMA_2, "book_gamma_2", 0b000_010_000_000),

    GAMMA_3(BookPosition.GAMMA_3, "book_gamma_3", 0b000_100_000_000),

    DELTA_1(BookPosition.DELTA_1, "book_delta_1", 0b001_000_000_000),

    DELTA_2(BookPosition.DELTA_2, "book_delta_2", 0b010_000_000_000),

    DELTA_3(BookPosition.DELTA_3, "book_delta_3", 0b100_000_000_000);

    // The Z coordinate of the front surface of the rear plate of the shelf. This is as low in Z as a vertex can be.
    private static final float Z_BACK_STOP = 1/16f;

    public final  BookPosition POSITION; // Which book position is this for?
    public final String MODEL_ID; // What model are we loading? (needs to have "simple_shelves:block/" in front.
    public final int BIT_MASK; // A bit mask for use in quickly determining which books to render client-side.

    BookModelKey(BookPosition bookPosition, String modelId, int bitFlag){
        this.POSITION = bookPosition;
        this.MODEL_ID = modelId;
        this.BIT_MASK = ~bitFlag;
    }

}
