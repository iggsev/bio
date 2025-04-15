package com.biobox;

import com.badlogic.gdx.graphics.Color;

/**
 * Simplified TileType enum to work with LibGDX rendering
 */
public enum TileType {
    GRASS(true, new Color(0.2f, 0.7f, 0.2f, 1f)),
    WALL(false, new Color(0.5f, 0.5f, 0.5f, 1f)),
    WATER(false, new Color(0.2f, 0.3f, 0.8f, 1f)),
    SAND(true, new Color(0.9f, 0.8f, 0.5f, 1f)),
    DIRT(true, new Color(0.6f, 0.4f, 0.2f, 1f)),
    STONE(true, new Color(0.4f, 0.4f, 0.4f, 1f));

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