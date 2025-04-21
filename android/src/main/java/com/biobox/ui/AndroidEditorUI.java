package com.biobox.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.biobox.core.BiomeGenerator;
import com.biobox.core.BiomeType;
import com.biobox.core.SquareGrid;
import com.biobox.core.SquareTileRenderer;
import com.biobox.input.InputController;

/**
 * Implementação básica da UI para Android
 */
public class AndroidEditorUI implements EditorUI {
    
    // Core components
    private final SquareGrid grid;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final SquareTileRenderer tileRenderer;
    private final BiomeGenerator worldGenerator;
    
    // Camera and viewport
    private final OrthographicCamera camera;
    private final Viewport viewport;
    
    // UI constants
    private static final float SCREEN_WIDTH = 1280;
    private static final float SCREEN_HEIGHT = 720;
    
    // Estado
    private boolean gridVisible = true;
    private int editorMode = 0;
    
    public AndroidEditorUI(SquareGrid grid, ShapeRenderer shapeRenderer, SpriteBatch batch, 
                           BitmapFont font, SquareTileRenderer tileRenderer, 
                           BiomeGenerator worldGenerator) {
        this.grid = grid;
        this.shapeRenderer = shapeRenderer;
        this.batch = batch;
        this.font = font;
        this.tileRenderer = tileRenderer;
        this.worldGenerator = worldGenerator;
        
        // Setup camera and viewport
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(
            grid.getWidth() * SquareTileRenderer.TILE_SIZE,
            grid.getHeight() * SquareTileRenderer.TILE_SIZE,
            camera
        );
        
        // Center camera on grid
        centerCamera();
    }
    
    private void centerCamera() {
        float width = grid.getWidth() * SquareTileRenderer.TILE_SIZE;
        float height = grid.getHeight() * SquareTileRenderer.TILE_SIZE;
        camera.position.set(width / 2f, height / 2f, 0);
        camera.update();
    }
    
    @Override
    public void update(float deltaTime, InputController inputController) {
        // Atualização básica - pode ser expandida conforme necessário
    }
    
    @Override
    public void render() {
        // Renderização básica do mapa
        viewport.apply();
        tileRenderer.render(camera);
        
        // Renderização da UI básica
        batch.begin();
        font.draw(batch, "BioBox - Android", 10, Gdx.graphics.getHeight() - 10);
        font.draw(batch, "Toque na tela para interagir", 10, Gdx.graphics.getHeight() - 30);
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
    
    @Override
    public void dispose() {
        // Nada específico para liberar aqui
    }
    
    @Override
    public OrthographicCamera getCamera() {
        return camera;
    }
    
    @Override
    public void toggleGrid() {
        gridVisible = !gridVisible;
        tileRenderer.toggleGrid();
    }
    
    @Override
    public boolean isGridVisible() {
        return gridVisible;
    }
    
    @Override
    public void setEditorMode(int mode) {
        this.editorMode = mode;
    }
    
    @Override
    public int getEditorMode() {
        return editorMode;
    }
    
    @Override
    public void generateWorld(int worldType) {
        if (worldType >= 0 && worldType < BiomeGenerator.WorldType.values().length) {
            BiomeGenerator.WorldType type = BiomeGenerator.WorldType.values()[worldType];
            worldGenerator.generateWorld(grid, type);
            tileRenderer.markDirty();
        }
    }
    
    @Override
    public boolean isInMapArea(float x, float y) {
        // Uma implementação básica - considerar toda a tela como área de mapa
        return true;
    }
    
    @Override
    public float getUIWidth() {
        return SCREEN_WIDTH;
    }
    
    @Override
    public float getUIHeight() {
        return SCREEN_HEIGHT;
    }
}
