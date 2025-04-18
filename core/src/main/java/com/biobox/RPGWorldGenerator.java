package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Main application for the RPG World Generator with square tiles
 */
public class RPGWorldGenerator extends ApplicationAdapter {
    // World size and screen dimensions
    private static final int GRID_WIDTH = 100;
    private static final int GRID_HEIGHT = 70;
    private static final int SCREEN_WIDTH = 1280;
    private static final int SCREEN_HEIGHT = 720;
    
    // Components
    private SquareGrid grid;
    private BiomeGenerator worldGenerator;
    private SquareTileRenderer tileRenderer;
    private WorldEditor worldEditor;
    private BiomeGenerator.WorldType currentWorldType;
    
    // Rendering
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    
    // UI
    private boolean showHelp = true;
    private float lastGenerationTime = 0;
    
    // Input handling
    private Vector3 lastMousePos = new Vector3();
    private boolean isDragging = false;
    
    @Override
    public void create() {
        // Initialize rendering components
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        
        // Setup camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        camera.position.set(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 0);
        camera.update();
        
        // Create square grid
        grid = new SquareGrid(GRID_WIDTH, GRID_HEIGHT);
        
        // Create world generator
        worldGenerator = new BiomeGenerator();
        
        // Create renderer
        tileRenderer = new SquareTileRenderer(grid, shapeRenderer, batch);
        
        // Create world editor
        worldEditor = new WorldEditor(grid, shapeRenderer, batch, font, tileRenderer);
        
        // Generate initial world
        generateNewWorld();
        
        // Setup input processor
        Gdx.input.setInputProcessor(new WorldInputProcessor());
        
        // Center camera on the grid
        centerCamera();
    }
    
    /**
     * Generate a new world with a random type
     */
    private void generateNewWorld() {
        long startTime = System.currentTimeMillis();
        
        currentWorldType = worldGenerator.getRandomWorldType();
        worldGenerator.generateWorld(grid, currentWorldType);
        
        // Mark the renderer dirty to update the texture
        tileRenderer.markDirty();
        
        // Calculate generation time
        lastGenerationTime = (System.currentTimeMillis() - startTime) / 1000f;
    }
    
    /**
     * Generate a specific world type
     */
    private void generateWorld(BiomeGenerator.WorldType type) {
        long startTime = System.currentTimeMillis();
        
        currentWorldType = type;
        worldGenerator.generateWorld(grid, type);
        
        // Mark the renderer dirty to update the texture
        tileRenderer.markDirty();
        
        // Calculate generation time
        lastGenerationTime = (System.currentTimeMillis() - startTime) / 1000f;
    }
    
    /**
     * Center the camera on the grid
     */
    private void centerCamera() {
        int gridWidthInPixels = GRID_WIDTH * SquareTileRenderer.TILE_SIZE;
        int gridHeightInPixels = GRID_HEIGHT * SquareTileRenderer.TILE_SIZE;
        
        camera.position.set(gridWidthInPixels / 2f, gridHeightInPixels / 2f, 0);
        camera.zoom = 2f; // Initial zoom level
        camera.update();
    }
    
    @Override
    public void render() {
        // Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Handle input
        handleInput();
        
        // Update camera
        camera.update();
        
        // Render the world
        tileRenderer.render(camera);
        
        // Render world editor if active
        if (worldEditor.isActive()) {
            worldEditor.render(camera);
        }
        
        // Render UI
        renderUI();
    }
    
    /**
     * Handle keyboard and mouse input
     */
    private void handleInput() {
        // Let editor handle input if active
        if (worldEditor.isActive()) {
            worldEditor.handleInput(camera);
            return;
        }
        
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Camera movement
        float speed = 400f * deltaTime * camera.zoom;
        
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += speed;
        
        // Zoom control
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom += deltaTime * 2f;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) camera.zoom -= deltaTime * 2f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 10f);
        
        // Generate new random world with R
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            generateNewWorld();
        }
        
        // Toggle grid with G
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            tileRenderer.toggleGrid();
        }
        
        // Toggle editor with TAB
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            worldEditor.toggle();
        }
        
        // Toggle help with H
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            showHelp = !showHelp;
        }
        
        // Generate specific world types with number keys
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            generateWorld(BiomeGenerator.WorldType.CLASSIC);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            generateWorld(BiomeGenerator.WorldType.ARCHIPELAGO);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            generateWorld(BiomeGenerator.WorldType.CONTINENTS);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            generateWorld(BiomeGenerator.WorldType.PANGEA);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
            generateWorld(BiomeGenerator.WorldType.ISLANDS);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
            generateWorld(BiomeGenerator.WorldType.VOLCANIC);
        }
    }
    
    /**
     * Render UI elements
     */
    private void renderUI() {
        batch.begin();
        
        // Show world type and editor status
        String worldTypeText = "World Type: " + currentWorldType;
        String editorStatus = worldEditor.isActive() ? " | EDITOR MODE" : " | VIEW MODE";
        font.draw(batch, worldTypeText + editorStatus, 10, Gdx.graphics.getHeight() - 10);
        
        // Show generation time and FPS
        String genTimeText = String.format("Generation Time: %.2fs", lastGenerationTime);
        String fpsText = "FPS: " + Gdx.graphics.getFramesPerSecond();
        font.draw(batch, genTimeText, 10, Gdx.graphics.getHeight() - 30);
        font.draw(batch, fpsText, 10, Gdx.graphics.getHeight() - 50);
        
        // Show grid status
        String gridText = "Grid: " + (tileRenderer.isShowingGrid() ? "ON" : "OFF");
        font.draw(batch, gridText, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 10);
        
        // Show editor toggle hint
        if (!worldEditor.isActive()) {
            font.draw(batch, "Press TAB to enter editor mode", 10, 30);
        }
        
        // Show help if enabled
        if (showHelp) {
            drawHelpText();
        } else {
            font.draw(batch, "Press H for help", 10, 60);
        }
        
        batch.end();
    }
    
    /**
     * Draw help text on screen
     */
    private void drawHelpText() {
        int y = 200;
        int leftX = 10;
        int rightX = Gdx.graphics.getWidth() / 2 + 50;
        
        // Title
        font.draw(batch, "CONTROLS:", leftX, y);
        y -= 20;
        
        // Movement controls
        font.draw(batch, "WASD - Move camera", leftX, y);
        y -= 20;
        font.draw(batch, "QE - Zoom in/out", leftX, y);
        y -= 20;
        font.draw(batch, "Mouse drag - Pan camera", leftX, y);
        y -= 20;
        font.draw(batch, "Mouse wheel - Zoom", leftX, y);
        y -= 20;
        
        // Display options
        font.draw(batch, "G - Toggle grid", leftX, y);
        y -= 20;
        font.draw(batch, "TAB - Toggle editor", leftX, y);
        y -= 20;
        font.draw(batch, "H - Toggle help", leftX, y);
        y -= 20;
        
        // World generation
        y = 200;
        font.draw(batch, "WORLD TYPES:", rightX, y);
        y -= 20;
        font.draw(batch, "R - Random world", rightX, y);
        y -= 20;
        font.draw(batch, "1 - Classic RPG", rightX, y);
        y -= 20;
        font.draw(batch, "2 - Archipelago", rightX, y);
        y -= 20;
        font.draw(batch, "3 - Continents", rightX, y);
        y -= 20;
        font.draw(batch, "4 - Pangea", rightX, y);
        y -= 20;
        font.draw(batch, "5 - Islands", rightX, y);
        y -= 20;
        font.draw(batch, "6 - Volcanic", rightX, y);
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        camera.update();
    }
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        tileRenderer.dispose();
    }
    
    /**
     * Input processor for handling mouse/touch input
     */
    private class WorldInputProcessor extends InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                // Start dragging
                isDragging = true;
                lastMousePos.set(screenX, screenY, 0);
                return true;
            }
            return false;
        }
        
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            // Stop dragging
            isDragging = false;
            return true;
        }
        
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (isDragging && !worldEditor.isActive()) {
                // Calculate how much the mouse has moved since the last frame
                float deltaX = (screenX - lastMousePos.x) * camera.zoom;
                float deltaY = (lastMousePos.y - screenY) * camera.zoom;
                
                // Move the camera
                camera.position.x -= deltaX;
                camera.position.y -= deltaY;
                
                // Update last mouse position
                lastMousePos.set(screenX, screenY, 0);
                return true;
            }
            return false;
        }
        
        @Override
        public boolean scrolled(float amountX, float amountY) {
            // Zoom with mouse wheel
            camera.zoom += amountY * camera.zoom * 0.1f;
            camera.zoom = MathUtils.clamp(camera.zoom, 0.2f, 10f);
            return true;
        }
    }
}