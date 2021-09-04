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
     * A block that can store and display items and books.
     *
     * Abstract, because there are shelves for each type of wood, and possibly other materials.
     * As this item needs to store an inventory, a block entity has been implemented for this block. (ShelfBlockEntity).
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
        ShelfEntity blockEntity;
        // Let's be clear about any block entity shenanigans.
        try{
            blockEntity = (ShelfEntity) world.getBlockEntity(pos);
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
        // Is the player's main hand empty?
        if (activeStack.isEmpty()) {
            // If so, try to extract an item from the shelf.
            // Does the current quadrant have a stack?
            if(blockEntity.quadrantHasGenericItem(quadrant)){
                // If so, try to extract the stack from the generic slot.
                // TODO: Implement manual item extraction for generic slots.
                return ActionResult.SUCCESS;
            }
            // Which book slot?
            BookPosition bookPosition = BookPosition.getBookPos(hit, state.get(FACING));
            // Does that book position have a stack in it?
            if (!blockEntity.getStack(bookPosition.SLOT).isEmpty()){
                // If so, try to extract the stack.
                // TODO: Implement manual item extraction for book slots.
                return ActionResult.SUCCESS;
            }
        }
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
            if(be instanceof ShelfEntity){
                ItemScatterer.spawn(world, pos, (ShelfEntity)be);
                // Update all adjacent blocks, like comparators.
                world.updateNeighbors(pos, this);
            }
            super.onStateReplaced(state, world, pos,newState, moved);
        }
    }

}




