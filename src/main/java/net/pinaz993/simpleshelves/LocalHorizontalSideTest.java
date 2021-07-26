package net.pinaz993.simpleshelves;

import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static net.pinaz993.simpleshelves.LocalHorizontalSide.getLocalSide;
import static org.junit.jupiter.api.Assertions.*;

class LocalHorizontalSideTest {
    @Test
    @DisplayName("Back is opposite front.")
    void backIsOppositeFront() {
        assertEquals(LocalHorizontalSide.BACK, getLocalSide(Direction.NORTH, Direction.SOUTH));
        assertEquals(LocalHorizontalSide.BACK, getLocalSide(Direction.SOUTH, Direction.NORTH));
        assertEquals(LocalHorizontalSide.BACK, getLocalSide(Direction.EAST, Direction.WEST));
        assertEquals(LocalHorizontalSide.BACK, getLocalSide(Direction.WEST, Direction.EAST));
    }

    @Test
    @DisplayName("Right is 3 o'clock")
    void rightIs3oclock(){
        assertEquals(LocalHorizontalSide.RIGHT, getLocalSide(Direction.EAST, Direction.NORTH));
        assertEquals(LocalHorizontalSide.RIGHT, getLocalSide(Direction.SOUTH, Direction.EAST));
        assertEquals(LocalHorizontalSide.RIGHT, getLocalSide(Direction.WEST, Direction.SOUTH));
        assertEquals(LocalHorizontalSide.RIGHT, getLocalSide(Direction.NORTH, Direction.WEST));
    }

    @Test
    @DisplayName("Left is 9 o'clock")
    void leftIs9oclock(){
        assertEquals(LocalHorizontalSide.LEFT, getLocalSide(Direction.WEST, Direction.NORTH));
        assertEquals(LocalHorizontalSide.LEFT, getLocalSide(Direction.NORTH, Direction.EAST));
        assertEquals(LocalHorizontalSide.LEFT, getLocalSide(Direction.EAST, Direction.SOUTH));
        assertEquals(LocalHorizontalSide.LEFT, getLocalSide(Direction.SOUTH, Direction.WEST));
    }

//    @Test
//    @DisplayName("Front ")
}