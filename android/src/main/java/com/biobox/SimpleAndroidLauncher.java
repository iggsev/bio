package com.biobox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Color;

/**
 * Versão simplificada do RPG World Generator para testes no Android.
 */
public class SimpleAndroidLauncher extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    private float time = 0;
    
    // Screen dimensions
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 480;
    
    @Override
    public void create() {
        try {
            // Log initialization
            Gdx.app.log("BioBox", "Iniciando versão simplificada...");
            
            // Create basic components
            batch = new SpriteBatch();
            shapeRenderer = new ShapeRenderer();
            font = new BitmapFont();
            font.getData().setScale(2);
            
            // Setup camera
            camera = new OrthographicCamera();
            viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
            viewport.apply();
            
            camera.position.set(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, 0);
            camera.update();
            
            Gdx.app.log("BioBox", "Inicialização concluída com sucesso!");
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro na inicialização", e);
        }
    }
    
    @Override
    public void render() {
        try {
            // Clear screen
            Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            
            // Update time
            time += Gdx.graphics.getDeltaTime();
            
            // Update camera
            camera.update();
            
            // Draw shapes
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeType.Filled);
            
            // Draw some colorful shapes that animate
            float size = 100 + (float)Math.sin(time) * 20;
            
            // Red square
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(SCREEN_WIDTH/2 - size/2, SCREEN_HEIGHT/2 - size/2, size, size);
            
            // Green circle
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.circle(200, 240, 50 + (float)Math.cos(time*2) * 10);
            
            // Blue circle
            shapeRenderer.setColor(Color.BLUE);
            shapeRenderer.circle(600, 240, 50 + (float)Math.sin(time*2) * 10);
            
            shapeRenderer.end();
            
            // Draw text
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, "BioBox Android Test", 100, SCREEN_HEIGHT - 50);
            font.draw(batch, "Toque na tela para testar", 100, SCREEN_HEIGHT - 100);
            
            // Display touch position if touched
            if (Gdx.input.isTouched()) {
                float x = Gdx.input.getX();
                float y = Gdx.input.getY();
                
                // Convert to world coordinates
                float worldX = x * SCREEN_WIDTH / Gdx.graphics.getWidth();
                float worldY = SCREEN_HEIGHT - (y * SCREEN_HEIGHT / Gdx.graphics.getHeight());
                
                font.draw(batch, "Touch: " + (int)worldX + ", " + (int)worldY, 100, 100);
            }
            
            // Display frame rate
            font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), SCREEN_WIDTH - 150, 50);
            
            batch.end();
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro na renderização", e);
        }
    }
    
    @Override
    public void resize(int width, int height) {
        try {
            Gdx.app.log("BioBox", "Resize: " + width + "x" + height);
            viewport.update(width, height);
            camera.position.set(SCREEN_WIDTH/2, SCREEN_HEIGHT/2, 0);
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro no resize", e);
        }
    }
    
    @Override
    public void dispose() {
        try {
            Gdx.app.log("BioBox", "Disposing resources");
            if (batch != null) batch.dispose();
            if (shapeRenderer != null) shapeRenderer.dispose();
            if (font != null) font.dispose();
        } catch (Exception e) {
            Gdx.app.error("BioBox", "Erro no dispose", e);
        }
    }
}