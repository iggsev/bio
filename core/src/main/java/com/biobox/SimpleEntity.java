package com.biobox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Representa uma entidade no mapa com movimentação suave e wraparound nas bordas
 */
public class SimpleEntity {
    private static final float DEFAULT_SPEED = 2.0f;
    
    public float x, y;
    public float direction;
    public Color color;
    public float moveTimer;
    public Vector2 velocity;
    private GameMap map;
    
    public SimpleEntity(GameMap map, float x, float y, Color color) {
        this.map = map;
        this.x = x;
        this.y = y;
        this.color = color;
        this.direction = MathUtils.random(0f, MathUtils.PI2);
        this.moveTimer = MathUtils.random(0f, 2f);
        this.velocity = new Vector2(0, 0);
    }
    
    public void update(float deltaTime) {
        // Change direction occasionally
        moveTimer -= deltaTime;
        if (moveTimer <= 0) {
            // Choose one of 32 directions
            int dirIndex = MathUtils.random(0, 31);
            direction = (dirIndex / 32f) * MathUtils.PI2;
            moveTimer = MathUtils.random(1f, 3f);
            
            // Set a new random velocity magnitude
            float speed = MathUtils.random(0.5f, 1.5f) * DEFAULT_SPEED;
            velocity.set(MathUtils.cos(direction) * speed, MathUtils.sin(direction) * speed);
        }
        
        // Calculate new position with velocity
        float newX = x + velocity.x * deltaTime;
        float newY = y + velocity.y * deltaTime;
        
        // Apply wraparound (toroidal mapping) for x-coordinate
        if (newX < 0) {
            newX = map.getWidth() - 0.01f; // Slight offset to prevent issues at exact boundary
        } else if (newX >= map.getWidth()) {
            newX = 0;
        }
        
        // Apply wraparound (toroidal mapping) for y-coordinate
        if (newY < 0) {
            newY = map.getHeight() - 0.01f; // Slight offset to prevent issues at exact boundary
        } else if (newY >= map.getHeight()) {
            newY = 0;
        }
        
        // Check if new position is valid (walkable)
        if (isValidPosition(newX, newY)) {
            x = newX;
            y = newY;
        } else {
            // Hit something, pick a new direction
            direction = (direction + MathUtils.PI) % MathUtils.PI2; // Reverse direction
            velocity.set(MathUtils.cos(direction) * DEFAULT_SPEED, MathUtils.sin(direction) * DEFAULT_SPEED);
        }
    }
    
    private boolean isValidPosition(float x, float y) {
        // Handle wraparound for checking position validity
        int tileX = MathUtils.floor(x);
        int tileY = MathUtils.floor(y);
        
        // Apply toroidal (wraparound) transformations for coordinate checks
        tileX = (tileX + map.getWidth()) % map.getWidth();
        tileY = (tileY + map.getHeight()) % map.getHeight();
        
        // Check tile type at the wrapped coordinate
        return map.getTile(tileX, tileY).isWalkable();
    }
    
    /**
     * Gets the visual X coordinate, handling wraparound rendering if needed
     * @param cameraX the camera's center X position
     * @param viewWidth the width of the visible area
     * @return the effective X coordinate for rendering
     */
    public float getVisualX(float cameraX, float viewWidth) {
        float halfWidth = viewWidth / 2;
        
        // Position in normal map space
        float visualX = x;
        
        // Check if we should draw a wrapped copy on the opposite side
        if (x * GameMap.TILE_SIZE < cameraX - halfWidth) {
            // Entity is off the left side, might need to draw on right
            float wrappedX = x + map.getWidth();
            if (wrappedX * GameMap.TILE_SIZE > cameraX - halfWidth) {
                visualX = wrappedX;
            }
        } else if (x * GameMap.TILE_SIZE > cameraX + halfWidth) {
            // Entity is off the right side, might need to draw on left
            float wrappedX = x - map.getWidth();
            if (wrappedX * GameMap.TILE_SIZE < cameraX + halfWidth) {
                visualX = wrappedX;
            }
        }
        
        return visualX;
    }
    
    /**
     * Gets the visual Y coordinate, handling wraparound rendering if needed
     * @param cameraY the camera's center Y position
     * @param viewHeight the height of the visible area
     * @return the effective Y coordinate for rendering
     */
    public float getVisualY(float cameraY, float viewHeight) {
        float halfHeight = viewHeight / 2;
        
        // Position in normal map space
        float visualY = y;
        
        // Check if we should draw a wrapped copy on the opposite side
        if (y * GameMap.TILE_SIZE < cameraY - halfHeight) {
            // Entity is off the bottom side, might need to draw on top
            float wrappedY = y + map.getHeight();
            if (wrappedY * GameMap.TILE_SIZE > cameraY - halfHeight) {
                visualY = wrappedY;
            }
        } else if (y * GameMap.TILE_SIZE > cameraY + halfHeight) {
            // Entity is off the top side, might need to draw on bottom
            float wrappedY = y - map.getHeight();
            if (wrappedY * GameMap.TILE_SIZE < cameraY + halfHeight) {
                visualY = wrappedY;
            }
        }
        
        return visualY;
    }
}