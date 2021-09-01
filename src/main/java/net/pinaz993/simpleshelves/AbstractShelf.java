package net.pinaz993.simpleshelves;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@SuppressWarnings("deprecation") //Because Mojang abuses @Deprecated. Not the smartest practice, I'll say.
public abstract class AbstractShelf extends HorizontalFacingBlock implements BlockEntityProvider {
    /**
     * A block that can store and display items and books. Each block has four quadrants. Each quadrant can contain one
     * generic item stack, or three book-like (any tagged as lectern_books, :book, :enchanted_book, or
     * simple_shelves:RedstoneBook) item stacks. These items are placed in the shelf by 'using' (right click by default)
     * the item stack on the desired quadrant. If the quadrant is occupied by one or more books, an item cannot be
     * inserted into the quadrant. If the quadrant is occupied by a generic item stack, book-like item stacks cannot be
     * inserted. Only book-like item stacks can be inserted or withdrawn automatically (with hoppers or pipes).
     *
     * Item stacks contained within a quadrant are rendered using the item frames renderer, with positions adjusted to
     * make the item or block look as if it is sitting on the shelf. Book-like items contained within a quadrant are
     * rendered much the same way as in BiblioCraft bookshelves: as rectangular prisms of various widths, heights, and
     * colors.
     *
     * In order to implement the manual item stack/book-like insertion/withdraw behaviors, first the face and location
     * on face of the right click event will have to be ascertained. If the face is bottom or 'back' (as determined from
     * the horizontal orientation), nothing will happen. If the face is the top or one of the 'sides' (as determined
     * from the horizontal orientation), the event will interact (or fail to interact) with only the quadrants
     * accessible from those faces. If the face is the 'front' (as determined from the horizontal orientation), the
     * event can interact (or fail to interact) with any of the quadrants.
     *
     * In order for the shelf to be able to store most items as items but treat books differently, each quadrant will
     * need 4 storage slots: 1 normal item slot that can take in any item but is not exposed to iSided-ness, and three
     * book-like slots that are exposed to all sides of the block except the 'front' (as determi... how'd you guess?).
     *
     * Non crouching 'use' behavior will follow the following flowchart:
     *
     *          Is the use item book-like?
     *             │                │
     *             │                │
     *             │                │
     *             │                │
     *            yes               no
     *             │                 │
     *             │                 │
     *             │                 │
     *             │                 │
     *             │         Is the use item stack empty?
     *             │           │                   │
     *             │           │                   │
     *             │          yes                  │
     *             │           │                   │
     *             │           │                   no
     *             │       Do nothing.              │
     *             │                     ┌──────────┘
     *             │                     │
     *             │          Is the quadrant occupied
     *             │          by >= 1 book-like stack?
     *             │             │               │
     *             │             │               │
     *             │            yes              no
     *             │             │               │
     *             │             │               │
     *             │         Do nothing.         │
     *             │                     ┌───────┘
     *             │                     │
     *             │             Is the quadrant occupied by a
     *             │             generic item stack?     │
     *             │              │                      │
     *             │              │                     no
     *             │              │              ┌───────
     *             │              │              │
     *             │             yes    Transfer the use item stack
     *             │              │     to the quadrant. Update block.
     *             │              │
     *             │              │
     *             │       Can the use item stack
     *             │       partially or completely
     *             │       stack with the quadrant
     *             │       item stack?       │
     *             │         │               │
     *             │         │               │
     *             │         │              no
     *             │        yes              │
     *             │         │               │
     *             │         │           Do nothing.
     *             │         └──────┐
     *             │                │
     *             │    Transfer as many items
     *             │    as possible to the quadrant,
     *             │    leaving any remainder in the
     *             │    player's inventory. Update block.
     *             │
     *             │
     *             │
     * Is the quadrant occupied by a
     * generic item stack?     │
     *    │                    │
     *    │                    │
     *   yes               ┌───┘
     *    │                │
     *    │               no
     * Do nothing.        │
     *                    │
     *                    │
     *             Is the sub-quadrant occupied
     *             by a book-like stack?│
     *               │                  │
     *               │                  │
     *               │                 no
     *               │                 │
     *               │               ┌─┘
     *              yes              │
     *               │     Transfer the item stack into
     *               │     the slot corresponding to the
     *               │     sub-quadrant. Update block.
     *               │
     *               │
     *     Can the use item stack
     *     partially or completely
     *     stack with the quadrant
     *     item stack?       │
     *       │               │
     *       │               │
     *       │              no
     *      yes              │
     *       │               │
     *       │           Do nothing.
     *       └─────────┐
     *                 │
     *      Transfer as many items
     *      as possible to the quadrant,
     *      leaving any remainder in the
     *      player's inventory. Update block.
     *
     * Crouching 'use' behavior (shift + right click by default) will do nothing if the slot is empty, place the
     * contained item stack in the player's inventory if there's room, or spawn it as an entity at the center of the
     * face that was clicked on if the player's inventory is full.
     *
     * iSidedInventory interaction will simply be accomplished by exposing all book-like slots to be manipulated from
     * all sides. Generic item slots will not be available to automation, allowing for vanilla-style filtering of
     * non-stackable book-like items.
     *
     * Shelves will have the same 'fullness indicator' behavior one expects from a chest, with the provision that any
     * slot that is unusable (book-like slots for a quadrant that is filled with an item and visa versa) will not be
     * counted. (That'll be fun to implement.)
     *
     * Shelves will have an additional redstone behavior. In addition to triggering a block update every time their
     * inventory is visibly changed, every time they receive a block update, they will query all book-like slots for
     * simpleshelves.RedstoneBook item stacks. If one or more stacks are found, the shelf will emit redstone equal in
     * value to the amount of RedstoneBook items in the most numerous stack. (These items stack to 15 for this reason.)
     *
     * Shelves will function as enchantment boosters, with a boosting effect equal to the amount of quadrants occupied
     * by at least one book-like item stack, resulting in a maximum boosting effect of 4, and an minimum of 1.
     * (Vanilla bookshelves have a boosting effect of 2, for reference.)
     *
     * If for some reason a quadrant is occupied by both a generic item stack and one or more book-like item stacks, the
     * generic item stack will be ejected in entity form to the top side upon the next block update, and an error will
     * be thrown in the console.
     *
     * @param settings: used for super HorizontalFacingBlock
     */

    public AbstractShelf(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    // Boolean properties for each book slot. Short of learning OpenGL, this is as elegant a solution as I could come up with.
    public static final BooleanProperty BOOK_ALPHA_1 = BooleanProperty.of("book_alpha_1");
    public static final BooleanProperty BOOK_ALPHA_2 = BooleanProperty.of("book_alpha_2");
    public static final BooleanProperty BOOK_ALPHA_3 = BooleanProperty.of("book_alpha_3");
    public static final BooleanProperty BOOK_BETA_1 = BooleanProperty.of("book_beta_1");
    public static final BooleanProperty BOOK_BETA_2 = BooleanProperty.of("book_beta_2");
    public static final BooleanProperty BOOK_BETA_3 = BooleanProperty.of("book_beta_3");
    public static final BooleanProperty BOOK_GAMMA_1 = BooleanProperty.of("book_gamma_1");
    public static final BooleanProperty BOOK_GAMMA_2 = BooleanProperty.of("book_gamma_2");
    public static final BooleanProperty BOOK_GAMMA_3 = BooleanProperty.of("book_gamma_3");
    public static final BooleanProperty BOOK_DELTA_1 = BooleanProperty.of("book_delta_1");
    public static final BooleanProperty BOOK_DELTA_2 = BooleanProperty.of("book_delta_2");
    public static final BooleanProperty BOOK_DELTA_3 = BooleanProperty.of("book_delta_3");


    //<editor-fold desc="Horizontal Orientation, Placement, and Collision Region">
    // This block can be set down in four horizontal orientations, depending on where the player is facing when it is
    // placed. Same arrangement as furnaces. That is accomplished by the following two methods. It also has state for
    // every single book slot, because OpenGL is scary, and block states are nice and safe.
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
        stateManager.add(BOOK_ALPHA_1);
        stateManager.add(BOOK_ALPHA_2);
        stateManager.add(BOOK_ALPHA_3);
        stateManager.add(BOOK_BETA_1);
        stateManager.add(BOOK_BETA_2);
        stateManager.add(BOOK_BETA_3);
        stateManager.add(BOOK_GAMMA_1);
        stateManager.add(BOOK_GAMMA_2);
        stateManager.add(BOOK_GAMMA_3);
        stateManager.add(BOOK_DELTA_1);
        stateManager.add(BOOK_DELTA_2);
        stateManager.add(BOOK_DELTA_3);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Have to admit, being able to insert a line break before a dot call is pretty nice in this case.
        return this.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite())
                .with(BOOK_ALPHA_1, false)
                .with(BOOK_ALPHA_2, false)
                .with(BOOK_ALPHA_3, false)
                .with(BOOK_BETA_1, false)
                .with(BOOK_BETA_2, false)
                .with(BOOK_BETA_3, false)
                .with(BOOK_GAMMA_1, false)
                .with(BOOK_GAMMA_2, false)
                .with(BOOK_GAMMA_3, false)
                .with(BOOK_DELTA_1, false)
                .with(BOOK_DELTA_2, false)
                .with(BOOK_DELTA_3, false);
    }

    // A shelf only takes up half a block. If the block is facing south, it takes up the north half of the block, and
    // visa versa. Same for east and west. Books do not effect this.
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);
        return switch (dir) {
            case SOUTH -> VoxelShapes.cuboid(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
            case NORTH -> VoxelShapes.cuboid(0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f);
            case WEST -> VoxelShapes.cuboid(0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            case EAST -> VoxelShapes.cuboid(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f);
            default -> VoxelShapes.fullCube();
        };
    }
    //</editor-fold>


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ShelfBlockEntity blockEntity;
        // Let's be clear about any block entity shenanigans.
        try{
            blockEntity = (ShelfBlockEntity) world.getBlockEntity(pos);
            assert blockEntity != null;
        } catch(Exception e) {
            throw new IllegalStateException(String.format("""
                            Wrong Block Entity for shelf.
                            Block Entity: %s
                            Coordinates: %s""", world.getBlockEntity(pos), pos
            ));
        }
        // What side was hit?
        LocalHorizontalSide localSide = LocalHorizontalSide.getLocalSide(hit.getSide(), state.get(FACING));
        // If it was the bottom or back, no dice.
        if (localSide == LocalHorizontalSide.BACK || localSide == LocalHorizontalSide.BOTTOM) return ActionResult.PASS;
        // Which quadrant?
        ShelfQuadrant quadrant = ShelfQuadrant.getQuadrant(hit, state.get(FACING));
        // What's in the player's main hand?
        ItemStack activeStack = player.getMainHandStack();
        // Is the player's main hand empty? If so, the action fails, full stop.
        if (activeStack.isEmpty()) return ActionResult.FAIL;
        // Is the player holding a book-like item?
        if(ShelfInventory.isBookLike(activeStack)){
            // If so, try to insert it into the appropriate book slot.
            // If the quadrant the player clicked on has a generic item in it, the action fails, full stop.
            if (blockEntity.quadrantHasGenericItem(quadrant)) return ActionResult.FAIL;
            // Which book slot?
            BookPosition bookPosition = BookPosition.getBookPos(hit, state.get(FACING));
            // Defer to the logic in ShelfInventory.attemptInsertion. Place the result of that method in the player's selected slot.
            player.getInventory().setStack(player.getInventory().selectedSlot,
                    blockEntity.attemptInsertion(bookPosition.SLOT, activeStack));
            // An action was carried out.
        }
        // The player's hand isn't empty, and it doesn't have a book-like object in it. Treat the stack as generic.
        else {
            // If the quadrant the player clicked on has books, the action fails, full stop.
            if (blockEntity.quadrantHasBook(quadrant)) return ActionResult.FAIL;
            // Defer to the logic in ShelfInventory.attemptInsertion. Place the result of that method in the player's selected slot.
            player.getInventory().setStack(player.getInventory().selectedSlot,
                    blockEntity.attemptInsertion(quadrant.GENERIC_ITEM_SLOT, activeStack));
            // An action was carried out.
        }
        return ActionResult.SUCCESS;

    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = world.getBlockEntity(pos);
            if(be instanceof ShelfBlockEntity){
                ItemScatterer.spawn(world, pos, (ShelfBlockEntity)be);
                // Update all adjacent blocks, like comparators.
                world.updateNeighbors(pos, this);
            }
            super.onStateReplaced(state, world, pos,newState, moved);
        }
    }

}




