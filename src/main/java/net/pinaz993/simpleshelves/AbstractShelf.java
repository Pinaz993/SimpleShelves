package net.pinaz993.simpleshelves;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public abstract class AbstractShelf extends HorizontalFacingBlock {
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
     * colors inset one sub-voxel into the block. Models for books will be based on the location within the shelf.
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
     * Non crouching 'use' behavior will be determined by the following flowchart:
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
     * all sides except for the 'front.' (That hopper isn't touching the shelf. Fight me.) Generic item slots will not
     * be available to automation, allowing for vanilla-style filtering of non-stackable book-like items.
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

    //<editor-fold desc="Horizontal Orientation, Placement, and Collision Region">
    // This block can be set down in four horizontal orientations, depending on where the player is facing when it is
    // placed. Same arrangement as furnaces. That is accomplished by the following two methods.
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    // A shelf only takes up half a block. If the block is facing south, it takes up the north half of the block, and
    // visa versa. Same for east and west. Method is deprecated for reasons other than "DON'T USE".
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
        if(player.getMainHandStack() == ItemStack.EMPTY) {
            player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP,1, 1);
        } else {
            player.playSound(SoundEvents.ENTITY_PLAYER_HURT, 1, 1);
        }
        return ActionResult.SUCCESS;
    }
}
