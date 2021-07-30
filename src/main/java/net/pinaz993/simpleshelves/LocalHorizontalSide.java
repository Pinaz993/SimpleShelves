package net.pinaz993.simpleshelves;

import net.minecraft.util.math.Direction;

/**
 * A helper enum to tell right from left from back from front based on what direction a HorizontalFacingBlock is facing.
 * This could have been a private enum, but I want to try out tests. Thus, I have no choice but to put it in its own file.
 */
enum LocalHorizontalSide {
    BACK, BOTTOM, FRONT, LEFT, RIGHT, TOP;

    /**
     * See above. Will throw a state exception if fed garbage.
     * @param cardinalSide The side in question.
     * @param blockIsFacing The direction the block is facing. Must be NORTH, EAST, SOUTH, or WEST.
     * @return The local side equivalent to cardinalSide.
     */
    public static LocalHorizontalSide getLocalSide(Direction cardinalSide, Direction blockIsFacing) {
        // Ordered by probability of use, except for UP and DOWN, as they don't always work if the front test is run first.
        // This is only designed for children of HorizontalFacingBlock, so it can't face UP or DOWN.
        if (blockIsFacing == Direction.UP || blockIsFacing == Direction.DOWN)
            throw new IllegalStateException(String.format(
                    "Block cannot face %s", blockIsFacing));
        // The side facing the direction the block is facing is the front,
        else if (cardinalSide == blockIsFacing) return FRONT;
            // 3 o'clock is right,
        else if (cardinalSide == blockIsFacing.rotateYClockwise()) return RIGHT;
            // 9 o'clock is left,
        else if (cardinalSide == blockIsFacing.rotateYCounterclockwise()) return LEFT;
            // UP is always the top,
        else if (cardinalSide == Direction.UP) return TOP;
            // DOWN is always the bottom,
        else if (cardinalSide == Direction.DOWN) return BOTTOM;
            // and the opposite side is back.
        else if (cardinalSide == blockIsFacing.getOpposite()) return BACK;
            // Finally, throw a verbose exception if you don't know what else to do. I think I've covered everything, but I could be wrong.
        else throw new IllegalStateException(String.format(
                    "Cannot determine local side from %s side of %s facing block.", cardinalSide, blockIsFacing));
    }
}
