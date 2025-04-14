package com.biobox;

import com.badlogic.gdx.math.MathUtils;

public class Entity {
    private int x, y;
    private final Map map;

    public Entity(Map map, int startX, int startY) {
        this.map = map;
        this.x = startX;
        this.y = startY;
    }

    public void moveRandomly() {
        int direction = MathUtils.random(0, 3);
        int newX = x, newY = y;

        switch (direction) {
            case 0: newX++; break; // Right
            case 1: newX--; break; // Left
            case 2: newY++; break; // Up
            case 3: newY--; break; // Down
        }

        // Check if new position is walkable
        TileType tile = map.getTile(newX, newY);
        if (tile.isWalkable()) {
            x = newX;
            y = newY;
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}