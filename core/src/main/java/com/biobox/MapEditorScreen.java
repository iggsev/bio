package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class MapEditorScreen extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private int[][] map;
    private int entityX, entityY;
    private static final int MAP_SIZE = 100;
    private static final int TILE_SIZE = 16;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        map = new int[MAP_SIZE][MAP_SIZE];
        
        // Initialize map with grass (0)
        for (int x = 0; x < MAP_SIZE; x++) {
            for (int y = 0; y < MAP_SIZE; y++) {
                map[x][y] = 0;
            }
        }
        
        // Initial entity position
        entityX = MAP_SIZE / 2;
        entityY = MAP_SIZE / 2;
    }

    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Randomly move entity
        if (MathUtils.randomBoolean(0.1f)) {
            moveEntity();
        }

        // Render map
        renderMap();
    }

    private void moveEntity() {
        int direction = MathUtils.random(0, 3);
        int newX = entityX, newY = entityY;

        switch (direction) {
            case 0: newX++; break; // Right
            case 1: newX--; break; // Left
            case 2: newY++; break; // Up
            case 3: newY--; break; // Down
        }

        // Boundary and simple collision check
        if (newX >= 0 && newX < MAP_SIZE && newY >= 0 && newY < MAP_SIZE) {
            entityX = newX;
            entityY = newY;
        }
    }

    private void renderMap() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Render tiles
        for (int x = 0; x < MAP_SIZE; x++) {
            for (int y = 0; y < MAP_SIZE; y++) {
                // Different colors for different tile types
                switch (map[x][y]) {
                    case 0: // Grass
                        shapeRenderer.setColor(Color.GREEN);
                        break;
                    case 1: // Wall
                        shapeRenderer.setColor(Color.GRAY);
                        break;
                    case 2: // Water
                        shapeRenderer.setColor(Color.BLUE);
                        break;
                }
                shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Render entity
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(
            entityX * TILE_SIZE + TILE_SIZE / 2f, 
            entityY * TILE_SIZE + TILE_SIZE / 2f, 
            TILE_SIZE / 2f
        );

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}