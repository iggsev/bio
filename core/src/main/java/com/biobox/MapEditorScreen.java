package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class MapEditorScreen extends ApplicationAdapter {
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Map map;
    private Entity entity;
    private int[][] tileData;
    private static final int MAP_SIZE = 100;
    private static final int TILE_SIZE = 16;
    private float timeSinceLastMove = 0;
    private Vector3 touchPos = new Vector3();
    private int selectedTileType = 0;
    private String[] tileTypeNames = {"GRASS", "WALL", "WATER"};

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(MAP_SIZE * TILE_SIZE / 2f, MAP_SIZE * TILE_SIZE / 2f, 0);
        
        // Generate a new map with the WorldGenerator
        WorldGenerator worldGen = new WorldGenerator();
        map = worldGen.generateWorld(MAP_SIZE, MAP_SIZE);
        
        // Convert Map to simple tile data for rendering
        tileData = new int[MAP_SIZE][MAP_SIZE];
        for (int x = 0; x < MAP_SIZE; x++) {
            for (int y = 0; y < MAP_SIZE; y++) {
                TileType type = map.getTile(x, y);
                if (type == TileType.GRASS) tileData[x][y] = 0;
                else if (type == TileType.WALL) tileData[x][y] = 1;
                else if (type == TileType.WATER) tileData[x][y] = 2;
            }
        }
        
        // Place entity on a walkable tile
        int startX = MAP_SIZE / 2;
        int startY = MAP_SIZE / 2;
        
        // Make sure the entity starts on walkable terrain
        while (!map.getTile(startX, startY).isWalkable()) {
            startX = MathUtils.random(0, MAP_SIZE - 1);
            startY = MathUtils.random(0, MAP_SIZE - 1);
        }
        
        entity = new Entity(map, startX, startY);
    }

    @Override
    public void render() {
        // Handle input
        handleInput();
        
        // Update entity
        timeSinceLastMove += Gdx.graphics.getDeltaTime();
        if (timeSinceLastMove > 0.5f) {
            entity.moveRandomly();
            timeSinceLastMove = 0;
        }
        
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update camera
        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // Render map
        renderMap();
        
        // Render UI
        renderUI();
    }
    
    private void handleInput() {
        // Camera movement
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += 5;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= 5;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= 5;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += 5;
        
        // Zoom
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom += 0.05f;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) camera.zoom -= 0.05f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 3f);
        
        // Cycle through tile types
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            selectedTileType = (selectedTileType + 1) % 3;
        }
        
        // Place tiles with mouse
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            
            int tileX = (int)(touchPos.x / TILE_SIZE);
            int tileY = (int)(touchPos.y / TILE_SIZE);
            
            if (tileX >= 0 && tileX < MAP_SIZE && tileY >= 0 && tileY < MAP_SIZE) {
                tileData[tileX][tileY] = selectedTileType;
                
                // Update the actual Map data
                TileType type = TileType.GRASS;
                if (selectedTileType == 1) type = TileType.WALL;
                if (selectedTileType == 2) type = TileType.WATER;
                map.setTile(tileX, tileY, type);
            }
        }
        
        // Generate new map with R key
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            WorldGenerator worldGen = new WorldGenerator();
            map = worldGen.generateWorld(MAP_SIZE, MAP_SIZE);
            
            // Update tile data
            for (int x = 0; x < MAP_SIZE; x++) {
                for (int y = 0; y < MAP_SIZE; y++) {
                    TileType type = map.getTile(x, y);
                    if (type == TileType.GRASS) tileData[x][y] = 0;
                    else if (type == TileType.WALL) tileData[x][y] = 1;
                    else if (type == TileType.WATER) tileData[x][y] = 2;
                }
            }
            
            // Reposition entity
            int startX = MAP_SIZE / 2;
            int startY = MAP_SIZE / 2;
            while (!map.getTile(startX, startY).isWalkable()) {
                startX = MathUtils.random(0, MAP_SIZE - 1);
                startY = MathUtils.random(0, MAP_SIZE - 1);
            }
            entity = new Entity(map, startX, startY);
        }
    }

    private void renderMap() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Calculate visible region for better performance
        int startX = Math.max(0, (int)((camera.position.x - camera.viewportWidth/2 * camera.zoom) / TILE_SIZE) - 1);
        int endX = Math.min(MAP_SIZE - 1, (int)((camera.position.x + camera.viewportWidth/2 * camera.zoom) / TILE_SIZE) + 1);
        int startY = Math.max(0, (int)((camera.position.y - camera.viewportHeight/2 * camera.zoom) / TILE_SIZE) - 1);
        int endY = Math.min(MAP_SIZE - 1, (int)((camera.position.y + camera.viewportHeight/2 * camera.zoom) / TILE_SIZE) + 1);
        
        // Render tiles
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                // Different colors for different tile types
                switch (tileData[x][y]) {
                    case 0: // Grass
                        shapeRenderer.setColor(0.2f, 0.7f, 0.2f, 1f);
                        break;
                    case 1: // Wall
                        shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
                        break;
                    case 2: // Water
                        shapeRenderer.setColor(0.2f, 0.2f, 0.8f, 1f);
                        break;
                }
                shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Render entity
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(
            entity.getX() * TILE_SIZE + TILE_SIZE / 2f, 
            entity.getY() * TILE_SIZE + TILE_SIZE / 2f, 
            TILE_SIZE / 2f
        );

        // Render grid lines
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.5f);
        for (int x = startX; x <= endX; x++) {
            shapeRenderer.line(x * TILE_SIZE, startY * TILE_SIZE, x * TILE_SIZE, (endY + 1) * TILE_SIZE);
        }
        for (int y = startY; y <= endY; y++) {
            shapeRenderer.line(startX * TILE_SIZE, y * TILE_SIZE, (endX + 1) * TILE_SIZE, y * TILE_SIZE);
        }

        shapeRenderer.end();
    }
    
    private void renderUI() {
        // Set up for UI rendering (in screen coordinates)
        shapeRenderer.setProjectionMatrix(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()).combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw selected tile type indicator
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapeRenderer.rect(10, 10, 200, 50);
        
        // Show the color of the selected tile
        switch (selectedTileType) {
            case 0: // Grass
                shapeRenderer.setColor(0.2f, 0.7f, 0.2f, 1f);
                break;
            case 1: // Wall
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
                break;
            case 2: // Water
                shapeRenderer.setColor(0.2f, 0.2f, 0.8f, 1f);
                break;
        }
        shapeRenderer.rect(20, 20, 30, 30);
        
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}