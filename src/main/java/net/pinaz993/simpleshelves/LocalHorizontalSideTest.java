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
    @DisplayName("Right is 3 o'clock.")
    void rightIs3oclock(){
        assertEquals(LocalHorizontalSide.RIGHT, getLocalSide(Direction.WEST, Direction.NORTH));
        assertEquals(LocalHorizontalSide.RIGHT, getLocalSide(Direction.NORTH, Direction.EAST));
        assertEquals(LocalHorizontalSide.RIGHT, getLocalSide(Direction.EAST, Direction.SOUTH));
        assertEquals(LocalHorizontalSide.RIGHT, getLocalSide(Direction.SOUTH, Direction.WEST));
    }

    @Test
    @DisplayName("Left is 9 o'clock.")
    void leftIs9oclock(){
        assertEquals(LocalHorizontalSide.LEFT, getLocalSide(Direction.EAST, Direction.NORTH));
        assertEquals(LocalHorizontalSide.LEFT, getLocalSide(Direction.SOUTH, Direction.EAST));
        assertEquals(LocalHorizontalSide.LEFT, getLocalSide(Direction.WEST, Direction.SOUTH));
        assertEquals(LocalHorizontalSide.LEFT, getLocalSide(Direction.NORTH, Direction.WEST));
    }

    @Test
    @DisplayName("Front is the side the block is facing.")
    void frontIsFront(){
        assertEquals(LocalHorizontalSide.FRONT, getLocalSide(Direction.NORTH, Direction.NORTH));
        assertEquals(LocalHorizontalSide.FRONT, getLocalSide(Direction.EAST, Direction.EAST));
        assertEquals(LocalHorizontalSide.FRONT, getLocalSide(Direction.SOUTH, Direction.SOUTH));
        assertEquals(LocalHorizontalSide.FRONT, getLocalSide(Direction.WEST, Direction.WEST));
    }

    @Test
    @DisplayName("Up is top regardless of facing.")
    void upIsTop(){
        assertEquals(LocalHorizontalSide.TOP, getLocalSide(Direction.UP, Direction.NORTH));
        assertEquals(LocalHorizontalSide.TOP, getLocalSide(Direction.UP, Direction.EAST));
        assertEquals(LocalHorizontalSide.TOP, getLocalSide(Direction.UP, Direction.SOUTH));
        assertEquals(LocalHorizontalSide.TOP, getLocalSide(Direction.UP, Direction.WEST));
    }

    @Test
    @DisplayName("Down is bottom regardless of facing.")
    void downIsBottom(){
        assertEquals(LocalHorizontalSide.BOTTOM, getLocalSide(Direction.DOWN, Direction.NORTH));
        assertEquals(LocalHorizontalSide.BOTTOM, getLocalSide(Direction.DOWN, Direction.EAST));
        assertEquals(LocalHorizontalSide.BOTTOM, getLocalSide(Direction.DOWN, Direction.SOUTH));
        assertEquals(LocalHorizontalSide.BOTTOM, getLocalSide(Direction.DOWN, Direction.WEST));
    }

    @Test
    @DisplayName("Throws exception if block is facing up.")
    void cannotFaceUp(){
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.DOWN, Direction.UP));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.UP, Direction.UP));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.NORTH, Direction.UP));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.EAST, Direction.UP));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.SOUTH, Direction.UP));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.WEST, Direction.UP));
    }

    @Test
    @DisplayName("Throws Exception if block is facing down.")
    void cannotFaceDown(){
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.DOWN, Direction.DOWN));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.UP, Direction.DOWN));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.NORTH, Direction.DOWN));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.EAST, Direction.DOWN));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.SOUTH, Direction.DOWN));
        assertThrows(IllegalStateException.class, () -> getLocalSide(Direction.WEST, Direction.DOWN));
    }
}