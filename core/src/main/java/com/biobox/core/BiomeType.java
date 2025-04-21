package com.biobox.core;

import com.badlogic.gdx.graphics.Color;

/**
 * Enum representing different biome types for an RPG world map
 */
public enum BiomeType {
    DEEP_WATER(false, new Color(0.1f, 0.2f, 0.6f, 1f)),
    SHALLOW_WATER(false, new Color(0.2f, 0.4f, 0.8f, 1f)),
    SAND(true, new Color(0.9f, 0.8f, 0.5f, 1f)),
    GRASS(true, new Color(0.2f, 0.7f, 0.2f, 1f)),
    FOREST(true, new Color(0.0f, 0.5f, 0.2f, 1f)),
    JUNGLE(true, new Color(0.0f, 0.6f, 0.1f, 1f)),
    HILLS(true, new Color(0.5f, 0.5f, 0.3f, 1f)),
    MOUNTAINS(false, new Color(0.6f, 0.6f, 0.6f, 1f)), // Added mountain
    WALL(false, new Color(0.3f, 0.3f, 0.3f, 1f)),      // Added wall
    SNOW(true, new Color(0.9f, 0.9f, 0.9f, 1f)),
    DESERT(true, new Color(0.8f, 0.7f, 0.2f, 1f)),
    SAVANNA(true, new Color(0.8f, 0.7f, 0.3f, 1f)),
    SWAMP(true, new Color(0.3f, 0.4f, 0.2f, 1f)),
    LAVA(false, new Color(0.9f, 0.3f, 0.0f, 1f));

    private final boolean walkable;
    private final Color baseColor;

    BiomeType(boolean walkable, Color baseColor) {
        this.walkable = walkable;
        this.baseColor = baseColor;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public Color getBaseColor() {
        return baseColor;
    }
}