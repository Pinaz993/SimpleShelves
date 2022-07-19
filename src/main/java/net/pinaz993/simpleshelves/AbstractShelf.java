package net.pinaz993.simpleshelves;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
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
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation") //Because Mojang abuses @Deprecated. Not the smartest practice, I'll say.
public abstract class AbstractShelf extends HorizontalFacingBlock implements BlockEntityProvider {
    /**
     * A block that can store and display items and books.
     *
     * Abstract, because there are shelves for each type of wood, and possibly other materials.
     * As this item needs to store an inventory, a block entity has been implemented for this block. (ShelfBlockEntity).
     *
     * One goal was to allow this shelf to boost enchantment levels. That is not happening without mixins, and I don't
     * want to tangle with those at the moment. Maybe later, or if an API comes out.
     *
     * Done:
     * Models
     * Book Textures and Model
     * Books render when they're on the shelves.
     * book_like item tag
     * Quadrants can hold books, generic items, or nothing.
     * Use Book Insertion
     * Use Book Extraction
     * Use Item Insertion
     * Use Item Extraction
     * SidedInventory Insertion and Extraction
     *      Only works with books
     *      Will not insert into quadrants that have generic items
     * Generic item rendering
     * Inventory Syncing
     * All 8 woods accounted for.
     * Comparator Behavior
     * Localization in English
     * Analog Redstone Emission Behavior
     *
     *
     * @param settings: used for super HorizontalFacingBlock
     */

    public AbstractShelf(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }


    //<editor-fold desc="Horizontal Orientation, Placement, and Collision Region">
    // This block can be set down in four horizontal orientations, depending on where the player is facing when it is
    // placed. Same arrangement as furnaces. That is accomplished by the following two methods. It also has state for
    // every single book slot, because OpenGL is scary, and block states are nice and safe.
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Have to admit, being able to insert a line break before a dot call is pretty nice in this case.
        return this.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
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
        ShelfQuadrant quadrant = ShelfQuadrant.getQuadrant(hit, state.get(FACING)); // Which quadrant?
        ItemStack activeStack = player.getMainHandStack(); // What's in the player's main hand?
        if (activeStack.isEmpty()) { // Is the player's main hand empty?
            // If so, try to extract an item from the shelf.
            if(blockEntity.quadrantHasGenericItem(quadrant)){ // Does the current quadrant have a generic stack?
                // Spawn that item in the world as an item entity in the location of the player that right clicked.
                ItemScatterer.spawn(world,
                        player.getX(), player.getY(), player.getZ(),
                        blockEntity.removeStack(quadrant.GENERIC_ITEM_SLOT));
                return ActionResult.SUCCESS; // Something was done.
            } // No generic? Look at book slot.
            BookPosition bookPosition = BookPosition.getBookPos(hit, state.get(FACING)); // Which book slot?
            if (!blockEntity.getStack(bookPosition.SLOT).isEmpty()){ // Does that book slot have a stack in it?
                // Spawn that item in the world as an item entity in the location of the player that right clicked.
                ItemScatterer.spawn(world,
                        player.getX(), player.getY(), player.getZ(),
                        blockEntity.removeStack(bookPosition.SLOT));
                return ActionResult.SUCCESS; // Something was done.
            } else return ActionResult.FAIL; // If no book or generic item is in the slot, nothing was done.
        }
        if(ShelfInventory.isBookLike(activeStack)){ // Is the player holding a book-like item?
            // If so, try to insert it into the appropriate book slot.
            // If the quadrant the player clicked on has a generic item in it, the action fails, full stop.
            if (blockEntity.quadrantHasGenericItem(quadrant)) return ActionResult.FAIL;
            BookPosition bookPosition = BookPosition.getBookPos(hit, state.get(FACING)); // Which book slot?
            // Defer to the logic in ShelfInventory.attemptInsertion.
            // Place the result of that method in the player's selected slot.
            player.getInventory().setStack(player.getInventory().selectedSlot,
                    blockEntity.attemptInsertion(bookPosition.SLOT, activeStack));
            return ActionResult.SUCCESS; // Something was done.
        }
        else { // The player's hand isn't empty, and it doesn't have a book-like object in it.
            // Treat the stack as generic.
            // If the quadrant the player clicked on has books, the action fails, full stop.
            if (blockEntity.quadrantHasBook(quadrant)) return ActionResult.FAIL;
            // Defer to the logic in ShelfInventory.attemptInsertion.
            // Place the result of that method in the player's selected slot.
            player.getInventory().setStack(player.getInventory().selectedSlot,
                    blockEntity.attemptInsertion(quadrant.GENERIC_ITEM_SLOT, activeStack));
            // markDirty() won't trigger observers unless world.setBlockState() is called with NOTIFY_LISTENERS set.
            // Since that doesnt happen with generic items, manually do that here, so that we trigger any observers.
            return ActionResult.SUCCESS; // Something was done.
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) { // If the block type has changed
            BlockEntity be = world.getBlockEntity(pos); // Get the block entity.
            if(be instanceof ShelfEntity){ // If the entity is what we expect it to be...
                // Don't know why it wouldn't be, TBH. Monkey See, Monkey Do.
                ItemScatterer.spawn(world, pos, (ShelfEntity)be); // Drop the inventory as entities in the world.
                world.updateNeighbors(pos, this); // Update all adjacent blocks...
                world.updateComparators(pos, this); // and comparators (which might not be adjacent).
            }
            super.onStateReplaced(state, world, pos,newState, moved);
        }
    }

    //<editor-fold desc="Redstone Stuff">

    @Override
    public boolean hasComparatorOutput(BlockState state) {return true;}

    /**
     * Tells any comparator reading this block how much signal to put out.
     * Basically just defers to the logic in ShelfInventory.
     */
    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        // For some reason, either IDEA or Java won't just let use the following one-liner:
        //return world.getBlockEntity(pos) instanceof ShelfEntity ? (ShelfEntity)world.getBlockEntity(pos).getComparatorOutput(): 0;
        // So, I split it into two lines and it works. Don't ask me what the problem was.
        // Get the entity, and ensure it is a ShelfEntity.
        ShelfEntity entity = world.getBlockEntity(pos) instanceof ShelfEntity ? (ShelfEntity) world.getBlockEntity(pos): null;
        // If it is a ShelfEntity, defer to the comparator output logic within. Otherwise, return 0.
        return entity != null ? entity.getComparatorOutput() : 0;
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) { return true; } // Yes, it does emit redstone power.
    // As much as I'd like to make it so that redstone doesn't connect to the other two sides, I can't do that without
    // a mixin, so I'm not going to bother.

    @Override
    public int getStrongRedstonePower(@NotNull BlockState state, BlockView world, BlockPos pos, Direction direction) {
        // Power only comes from the back of the block. If the direction is the back...
        // Yes, this says FRONT. It's counterintuitive, I know. I don't know what's going on behind the scenes, I just
        // know that this works.
        if(LocalHorizontalSide.getLocalSide(direction, state.get(FACING)) == LocalHorizontalSide.FRONT) {
            ShelfEntity entity = (ShelfEntity) world.getBlockEntity(pos);
            assert (entity != null);
            return entity.getRedstoneValue();
        }
        else return 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getStrongRedstonePower(state, world, pos, direction); // Weak power is the same as strong power.
    }

    //</editor-fold>


}