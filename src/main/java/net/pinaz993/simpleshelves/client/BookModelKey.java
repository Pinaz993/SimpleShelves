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
    ALPHA_1(BookPosition.ALPHA_1, "book_alpha_1"), // Top Left Most
    ALPHA_2(BookPosition.ALPHA_2, "book_alpha_2"),
    ALPHA_3(BookPosition.ALPHA_3, "book_alpha_3"),
    BETA_1 (BookPosition.BETA_1 ,  "book_beta_1"),
    BETA_2 (BookPosition.BETA_2 ,  "book_beta_2"),
    BETA_3 (BookPosition.BETA_3 ,  "book_beta_3"), // Top Right Most
    GAMMA_1(BookPosition.GAMMA_1, "book_gamma_1"), // Bottom Left Most
    GAMMA_2(BookPosition.GAMMA_2, "book_gamma_2"),
    GAMMA_3(BookPosition.GAMMA_3, "book_gamma_3"),
    DELTA_1(BookPosition.DELTA_1, "book_delta_1"),
    DELTA_2(BookPosition.DELTA_2, "book_delta_2"),
    DELTA_3(BookPosition.DELTA_3, "book_delta_3"); // Bottom Right Most

    public final  BookPosition POSITION; // Which book position is this for?
    // The inverse of POSITION.BIT_FLAG, used to quickly determine if a book needs to be rendered.
    public final int BIT_MASK;
    public final String MODEL_ID; // What model are we loading? (needs to have "simple_shelves:block/" in front.

    BookModelKey(BookPosition bookPosition, String modelId){
        this.POSITION = bookPosition;
        this.BIT_MASK = ~POSITION.BIT_FLAG;
        this.MODEL_ID = modelId;
    }

}
