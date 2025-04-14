package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class MapEditorScreen extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private Map map;
    private Entity mainEntity;
    private Array<Entity> entities = new Array<>();
    private static final int MAP_SIZE = 100;
    private static final int TILE_SIZE = 32;
    private float timeSinceLastMove = 0;
    private Vector3 touchPos = new Vector3();
    private TileType selectedTileType = TileType.GRASS;
    
    // Map generation types
    public enum MapType {
        DEFAULT,
        ISLAND,
        CONTINENT,
        LAKES
    }
    private MapType currentMapType = MapType.DEFAULT;
    
    // UI elements
    private boolean isDragging = false;
    private boolean editorMode = true;
    private boolean entityPlacementMode = false;
    
    // UI Buttons
    private Rectangle grassButton;
    private Rectangle wallButton;
    private Rectangle waterButton;
    private Rectangle entityButton;
    private Rectangle modeButton;
    
    // Map generation buttons
    private Rectangle defaultMapButton;
    private Rectangle islandMapButton;
    private Rectangle continentMapButton;
    private Rectangle lakesMapButton;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // Configurar câmera para o mundo
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(MAP_SIZE * TILE_SIZE / 2f, MAP_SIZE * TILE_SIZE / 2f, 0);
        
        // Configurar câmera para UI
        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Initialize UI rectangles here
        int buttonHeight = 30;
        int buttonWidth = 120;
        int margin = 10;
        int startY = Gdx.graphics.getHeight() - buttonHeight - margin;
        
        // Tile buttons
        grassButton = new Rectangle(margin, startY, buttonWidth, buttonHeight);
        wallButton = new Rectangle(margin, startY - buttonHeight - margin, buttonWidth, buttonHeight);
        waterButton = new Rectangle(margin, startY - 2 * (buttonHeight + margin), buttonWidth, buttonHeight);
        entityButton = new Rectangle(margin, startY - 3 * (buttonHeight + margin), buttonWidth, buttonHeight);
        modeButton = new Rectangle(margin, startY - 4 * (buttonHeight + margin), buttonWidth, buttonHeight);
        
        // Map generation buttons
        int mapButtonWidth = 100;
        int mapButtonX = Gdx.graphics.getWidth() - mapButtonWidth - margin;
        defaultMapButton = new Rectangle(mapButtonX, startY, mapButtonWidth, buttonHeight);
        islandMapButton = new Rectangle(mapButtonX, startY - buttonHeight - margin, mapButtonWidth, buttonHeight);
        continentMapButton = new Rectangle(mapButtonX, startY - 2 * (buttonHeight + margin), mapButtonWidth, buttonHeight);
        lakesMapButton = new Rectangle(mapButtonX, startY - 3 * (buttonHeight + margin), mapButtonWidth, buttonHeight);
        
        System.out.println("Usando renderização básica sem texturas");
        
        // Generate initial map
        generateMap(currentMapType);
        
        // Configurar input handler
        Gdx.input.setInputProcessor(new CustomInputProcessor(this));
    }
    
    private void generateMap(MapType type) {
        WorldGenerator worldGen = new WorldGenerator();
        map = worldGen.generateWorld(MAP_SIZE, MAP_SIZE, type);
        
        // Clear existing entities
        entities.clear();
        
        // Create main entity
        int startX = MAP_SIZE / 2;
        int startY = MAP_SIZE / 2;
        
        // Make sure entity starts on walkable terrain
        while (!map.getTile(startX, startY).isWalkable()) {
            startX = MathUtils.random(0, MAP_SIZE - 1);
            startY = MathUtils.random(0, MAP_SIZE - 1);
        }
        
        mainEntity = new Entity(map, startX, startY);
        entities.add(mainEntity);
    }
    
    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        if (!editorMode) {
            timeSinceLastMove += Gdx.graphics.getDeltaTime();
            if (timeSinceLastMove > 0.5f) {
                // Move all entities, not just the main one
                for (Entity entity : entities) {
                    entity.moveRandomly();
                }
                timeSinceLastMove = 0;
            }
        }
        
        // Update camera
        handleCameraControls();
        camera.update();
        
        // Render map and entities
        renderMap();
        
        // Render UI
        renderUI();
    }
    
    private void renderUI() {
        // Configure UI batch and shape renderer
        uiCamera.update();
        shapeRenderer.setProjectionMatrix(uiCamera.combined);
        
        // Draw UI backgrounds with ShapeRenderer
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Tile buttons
        shapeRenderer.setColor(selectedTileType == TileType.GRASS && !entityPlacementMode ? Color.LIME : Color.FOREST);
        shapeRenderer.rect(grassButton.x, grassButton.y, grassButton.width, grassButton.height);
        
        shapeRenderer.setColor(selectedTileType == TileType.WALL && !entityPlacementMode ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        shapeRenderer.rect(wallButton.x, wallButton.y, wallButton.width, wallButton.height);
        
        shapeRenderer.setColor(selectedTileType == TileType.WATER && !entityPlacementMode ? Color.SKY : Color.NAVY);
        shapeRenderer.rect(waterButton.x, waterButton.y, waterButton.width, waterButton.height);
        
        shapeRenderer.setColor(entityPlacementMode ? Color.RED : Color.MAROON);
        shapeRenderer.rect(entityButton.x, entityButton.y, entityButton.width, entityButton.height);
        
        shapeRenderer.setColor(editorMode ? Color.GOLD : Color.ORANGE);
        shapeRenderer.rect(modeButton.x, modeButton.y, modeButton.width, modeButton.height);
        
        // Map generation buttons
        shapeRenderer.setColor(currentMapType == MapType.DEFAULT ? Color.CYAN : Color.TEAL);
        shapeRenderer.rect(defaultMapButton.x, defaultMapButton.y, defaultMapButton.width, defaultMapButton.height);
        
        shapeRenderer.setColor(currentMapType == MapType.ISLAND ? Color.CYAN : Color.TEAL);
        shapeRenderer.rect(islandMapButton.x, islandMapButton.y, islandMapButton.width, islandMapButton.height);
        
        shapeRenderer.setColor(currentMapType == MapType.CONTINENT ? Color.CYAN : Color.TEAL);
        shapeRenderer.rect(continentMapButton.x, continentMapButton.y, continentMapButton.width, continentMapButton.height);
        
        shapeRenderer.setColor(currentMapType == MapType.LAKES ? Color.CYAN : Color.TEAL);
        shapeRenderer.rect(lakesMapButton.x, lakesMapButton.y, lakesMapButton.width, lakesMapButton.height);
        
        shapeRenderer.end();
        
        // Draw button outlines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        
        // Tile selection buttons
        shapeRenderer.rect(grassButton.x, grassButton.y, grassButton.width, grassButton.height);
        shapeRenderer.rect(wallButton.x, wallButton.y, wallButton.width, wallButton.height);
        shapeRenderer.rect(waterButton.x, waterButton.y, waterButton.width, waterButton.height);
        shapeRenderer.rect(entityButton.x, entityButton.y, entityButton.width, entityButton.height);
        shapeRenderer.rect(modeButton.x, modeButton.y, modeButton.width, modeButton.height);
        
        // Map generation buttons
        shapeRenderer.rect(defaultMapButton.x, defaultMapButton.y, defaultMapButton.width, defaultMapButton.height);
        shapeRenderer.rect(islandMapButton.x, islandMapButton.y, islandMapButton.width, islandMapButton.height);
        shapeRenderer.rect(continentMapButton.x, continentMapButton.y, continentMapButton.width, continentMapButton.height);
        shapeRenderer.rect(lakesMapButton.x, lakesMapButton.y, lakesMapButton.width, lakesMapButton.height);
        
        shapeRenderer.end();
        
        // Draw button text (without using fonts for now)
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
    
    private void handleCameraControls() {
        float speed = 10f;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            speed = 20f;
        }
        
        // Camera movement
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += speed;
        
        // Zoom
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom += 0.02f;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) camera.zoom -= 0.02f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 3f);
    }
    
    public void handleMapClick(int screenX, int screenY) {
        // First check if click was on UI
        if (checkUIClick(screenX, screenY)) {
            return;
        }
        
        if (!editorMode) return;
        
        // Otherwise handle as map click
        touchPos.set(screenX, screenY, 0);
        camera.unproject(touchPos); // This correctly handles zoom
        
        int tileX = (int)(touchPos.x / TILE_SIZE);
        int tileY = (int)(touchPos.y / TILE_SIZE);
        
        if (tileX >= 0 && tileX < MAP_SIZE && tileY >= 0 && tileY < MAP_SIZE) {
            if (entityPlacementMode) {
                // Add a new entity if the tile is walkable
                if (map.getTile(tileX, tileY).isWalkable()) {
                    Entity newEntity = new Entity(map, tileX, tileY);
                    entities.add(newEntity);
                    System.out.println("Added entity at " + tileX + ", " + tileY);
                }
            } else {
                // Set tile type
                map.setTile(tileX, tileY, selectedTileType);
            }
        }
    }
    
    private boolean checkUIClick(int screenX, int screenY) {
        // Convert to UI coordinates (invert Y since libGDX has origin at bottom-left)
        float uiY = Gdx.graphics.getHeight() - screenY;
        
        // Tile selection buttons
        if (grassButton.contains(screenX, uiY)) {
            selectedTileType = TileType.GRASS;
            entityPlacementMode = false;
            System.out.println("Selected: GRASS");
            return true;
        } else if (wallButton.contains(screenX, uiY)) {
            selectedTileType = TileType.WALL;
            entityPlacementMode = false;
            System.out.println("Selected: WALL");
            return true;
        } else if (waterButton.contains(screenX, uiY)) {
            selectedTileType = TileType.WATER;
            entityPlacementMode = false;
            System.out.println("Selected: WATER");
            return true;
        } else if (entityButton.contains(screenX, uiY)) {
            entityPlacementMode = true;
            System.out.println("Entity placement mode");
            return true;
        } else if (modeButton.contains(screenX, uiY)) {
            editorMode = !editorMode;
            System.out.println("Mode: " + (editorMode ? "EDITOR" : "GAME"));
            return true;
        }
        
        // Map generation buttons
        if (defaultMapButton.contains(screenX, uiY)) {
            currentMapType = MapType.DEFAULT;
            generateMap(currentMapType);
            System.out.println("Generating DEFAULT map");
            return true;
        } else if (islandMapButton.contains(screenX, uiY)) {
            currentMapType = MapType.ISLAND;
            generateMap(currentMapType);
            System.out.println("Generating ISLAND map");
            return true;
        } else if (continentMapButton.contains(screenX, uiY)) {
            currentMapType = MapType.CONTINENT;
            generateMap(currentMapType);
            System.out.println("Generating CONTINENT map");
            return true;
        } else if (lakesMapButton.contains(screenX, uiY)) {
            currentMapType = MapType.LAKES;
            generateMap(currentMapType);
            System.out.println("Generating LAKES map");
            return true;
        }
        
        return false;
    }
    
    public void handleMapDrag(int screenX, int screenY) {
        if (editorMode && isDragging) {
            // Don't handle drags that start on UI
            float uiY = Gdx.graphics.getHeight() - screenY;
            if (grassButton.contains(screenX, uiY) || 
                wallButton.contains(screenX, uiY) || 
                waterButton.contains(screenX, uiY) ||
                entityButton.contains(screenX, uiY) ||
                modeButton.contains(screenX, uiY) ||
                defaultMapButton.contains(screenX, uiY) ||
                islandMapButton.contains(screenX, uiY) ||
                continentMapButton.contains(screenX, uiY) ||
                lakesMapButton.contains(screenX, uiY)) {
                return;
            }
            
            handleMapClick(screenX, screenY);
        }
    }
    
    public void startDragging() {
        isDragging = true;
    }
    
    public void stopDragging() {
        isDragging = false;
    }

    private void renderMap() {
        // Calculate visible region for better performance
        int startX = Math.max(0, (int)((camera.position.x - camera.viewportWidth/2 * camera.zoom) / TILE_SIZE) - 1);
        int endX = Math.min(MAP_SIZE - 1, (int)((camera.position.x + camera.viewportWidth/2 * camera.zoom) / TILE_SIZE) + 1);
        int startY = Math.max(0, (int)((camera.position.y - camera.viewportHeight/2 * camera.zoom) / TILE_SIZE) - 1);
        int endY = Math.min(MAP_SIZE - 1, (int)((camera.position.y + camera.viewportHeight/2 * camera.zoom) / TILE_SIZE) + 1);
        
        // Shape-based rendering
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                TileType tileType = map.getTile(x, y);
                
                // Set color based on tile type
                shapeRenderer.setColor(tileType.getColor());
                shapeRenderer.rect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
        
        // Draw all entities
        for (Entity entity : entities) {
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.circle(
                entity.getX() * TILE_SIZE + TILE_SIZE / 2f, 
                entity.getY() * TILE_SIZE + TILE_SIZE / 2f, 
                TILE_SIZE / 2f
            );
        }
        
        shapeRenderer.end();
        
        // Draw grid
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 0.5f);
        
        for (int x = startX; x <= endX + 1; x++) {
            shapeRenderer.line(x * TILE_SIZE, startY * TILE_SIZE, x * TILE_SIZE, (endY + 1) * TILE_SIZE);
        }
        
        for (int y = startY; y <= endY + 1; y++) {
            shapeRenderer.line(startX * TILE_SIZE, y * TILE_SIZE, (endX + 1) * TILE_SIZE, y * TILE_SIZE);
        }
        
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        
        uiCamera.viewportWidth = width;
        uiCamera.viewportHeight = height;
        uiCamera.update();
        
        // Update button positions after resize
        int buttonHeight = 30;
        int buttonWidth = 120;
        int margin = 10;
        int startY = height - buttonHeight - margin;
        
        // Tile buttons
        grassButton.set(margin, startY, buttonWidth, buttonHeight);
        wallButton.set(margin, startY - buttonHeight - margin, buttonWidth, buttonHeight);
        waterButton.set(margin, startY - 2 * (buttonHeight + margin), buttonWidth, buttonHeight);
        entityButton.set(margin, startY - 3 * (buttonHeight + margin), buttonWidth, buttonHeight);
        modeButton.set(margin, startY - 4 * (buttonHeight + margin), buttonWidth, buttonHeight);
        
        // Map generation buttons
        int mapButtonWidth = 100;
        int mapButtonX = width - mapButtonWidth - margin;
        defaultMapButton.set(mapButtonX, startY, mapButtonWidth, buttonHeight);
        islandMapButton.set(mapButtonX, startY - buttonHeight - margin, mapButtonWidth, buttonHeight);
        continentMapButton.set(mapButtonX, startY - 2 * (buttonHeight + margin), mapButtonWidth, buttonHeight);
        lakesMapButton.set(mapButtonX, startY - 3 * (buttonHeight + margin), mapButtonWidth, buttonHeight);
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (batch != null) batch.dispose();
    }
    
    // Input processor for both UI and map interaction
    private class CustomInputProcessor extends com.badlogic.gdx.InputAdapter {
        private MapEditorScreen screen;
        
        public CustomInputProcessor(MapEditorScreen screen) {
            this.screen = screen;
        }
        
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                screen.handleMapClick(screenX, screenY);
                screen.startDragging();
                return true;
            }
            return false;
        }
        
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            screen.handleMapDrag(screenX, screenY);
            return true;
        }
        
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                screen.stopDragging();
                return true;
            }
            return false;
        }
    }
}