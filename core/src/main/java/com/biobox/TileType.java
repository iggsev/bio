package com.biobox;

import com.badlogic.gdx.graphics.Color;

public enum TileType {
    GRASS(true, Color.GREEN),
    WALL(false, Color.GRAY),
    WATER(false, Color.BLUE);

    private final boolean walkable;
    private final Color color;

    TileType(boolean walkable, Color color) {
        this.walkable = walkable;
        this.color = color;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public Color getColor() {
        return color;
    }
}