package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Main class for the RPG World Generator application.
 */
public class WorldEditorMain extends ApplicationAdapter {
    // Core components
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    
    // Game components
    private SquareGrid grid;
    private BiomeGenerator worldGenerator;
    private SquareTileRenderer tileRenderer;
    
    // UI components
    private WorldEditorUI editorUI;
    
    @Override
    public void create() {
        // Initialize rendering components
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        
        // Create font
        font = createFont(16);
        
        // Create world components
        grid = new SquareGrid(100, 70);
        worldGenerator = new BiomeGenerator();
        tileRenderer = new SquareTileRenderer(grid, shapeRenderer, batch);
        
        // Create world editor UI
        editorUI = new WorldEditorUI(
            grid,
            shapeRenderer,
            batch,
            font,
            tileRenderer,
            worldGenerator
        );
        
        // Generate initial world
        worldGenerator.generateWorld(grid, BiomeGenerator.WorldType.CLASSIC);
        tileRenderer.markDirty();
        
        // Set input processor to handle user interactions
        Gdx.input.setInputProcessor(editorUI.getInputProcessor());
        
        System.out.println("RPG World Generator initialized!");
    }
    
    /**
     * Create a bitmap font with the specified size
     */
    private BitmapFont createFont(int size) {
        // Use the default font with scaled size
        BitmapFont defaultFont = new BitmapFont();
        defaultFont.getData().setScale(size / 16f);
        return defaultFont;
    }
    
    @Override
    public void render() {
        // Clear the screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Handle user input
        editorUI.handleInput();
        
        // Render the editor (UI and map)
        editorUI.render();
    }
    
    @Override
    public void resize(int width, int height) {
        // Notify UI about resize
        editorUI.resize(width, height);
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        tileRenderer.dispose();
        editorUI.dispose();
    }
}