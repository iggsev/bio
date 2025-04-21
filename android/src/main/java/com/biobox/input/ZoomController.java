package com.biobox.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Controlador de zoom para interfaces touch no Android
 * Fornece botões de zoom e gestos de pinch em uma implementação simples
 */
public class ZoomController {
    private final OrthographicCamera camera;
    private final Viewport uiViewport;
    
    // Estado de zoom
    private float targetZoom;
    private float zoomSpeed = 2.0f;
    private float minZoom = 0.2f;
    private float maxZoom = 5.0f;
    
    // Botões de zoom
    private Rectangle zoomInButton;
    private Rectangle zoomOutButton;
    
    // Estado de gesto de pinch
    private boolean isPinching = false;
    private float initialDistance = 0;
    private float initialZoom = 0;
    
    // Cores e estilo
    private static final Color BUTTON_COLOR = new Color(0.3f, 0.3f, 0.4f, 0.8f);
    private static final Color BUTTON_HOVER_COLOR = new Color(0.4f, 0.4f, 0.5f, 0.8f);
    
    /**
     * Cria um novo controlador de zoom
     * @param camera a câmera a ser controlada
     * @param uiViewport o viewport da UI
     */
    public ZoomController(OrthographicCamera camera, Viewport uiViewport) {
        this.camera = camera;
        this.uiViewport = uiViewport;
        this.targetZoom = camera.zoom;
        
        // Inicializar posição dos botões
        updateButtonPositions();
    }
    
    /**
     * Atualiza a posição dos botões de zoom
     */
    private void updateButtonPositions() {
        float buttonSize = 40;
        float margin = 10;
        
        // Posicionar botões no canto superior direito
        zoomInButton = new Rectangle(
            Gdx.graphics.getWidth() - buttonSize - margin, 
            Gdx.graphics.getHeight() - buttonSize - margin,
            buttonSize,
            buttonSize
        );
        
        zoomOutButton = new Rectangle(
            Gdx.graphics.getWidth() - buttonSize - margin,
            Gdx.graphics.getHeight() - 2*buttonSize - margin - 5,
            buttonSize,
            buttonSize
        );
    }
    
    /**
     * Atualiza o zoom da câmera, suavizando o movimento
     * @param deltaTime tempo desde a última atualização
     */
    public void update(float deltaTime) {
        // Suaviza o movimento do zoom
        if (Math.abs(camera.zoom - targetZoom) > 0.01f) {
            camera.zoom = MathUtils.lerp(camera.zoom, targetZoom, zoomSpeed * deltaTime);
            // Limita o zoom dentro dos limites definidos
            camera.zoom = MathUtils.clamp(camera.zoom, minZoom, maxZoom);
            camera.update();
        }
    }
    
    /**
     * Lida com toques na tela para interação com os botões ou zoom
     * @param screenX coordenada X do toque na tela
     * @param screenY coordenada Y do toque na tela
     * @param isDown verdadeiro se é um toque inicial, falso se é um toque liberado
     * @return verdadeiro se o toque foi consumido
     */
    public boolean handleTouch(float screenX, float screenY, boolean isDown) {
        // Converter coordenadas de tela para coordenadas de UI
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        uiViewport.unproject(touchPos);
        
        if (isDown) {
            // Verificar botões de zoom
            if (zoomInButton.contains(touchPos.x, touchPos.y)) {
                zoomIn();
                return true;
            } else if (zoomOutButton.contains(touchPos.x, touchPos.y)) {
                zoomOut();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Inicia um gesto de pinch para zoom
     * @param distance distância inicial entre os dedos
     */
    public void beginPinch(float distance) {
        isPinching = true;
        initialDistance = distance;
        initialZoom = camera.zoom;
    }
    
    /**
     * Atualiza o zoom baseado no gesto de pinch
     * @param currentDistance distância atual entre os dedos
     */
    public void updatePinch(float currentDistance) {
        if (!isPinching || initialDistance <= 0) return;
        
        float ratio = initialDistance / currentDistance;
        targetZoom = MathUtils.clamp(initialZoom * ratio, minZoom, maxZoom);
    }
    
    /**
     * Finaliza o gesto de pinch
     */
    public void endPinch() {
        isPinching = false;
    }
    
    /**
     * Renderiza os botões de zoom
     * @param renderer renderizador de formas
     * @param batch batch para desenhar texto
     * @param font fonte para os símbolos + e -
     */
    public void render(ShapeRenderer renderer, SpriteBatch batch, BitmapFont font) {
        // Aplicar viewport da UI
        renderer.setProjectionMatrix(uiViewport.getCamera().combined);
        batch.setProjectionMatrix(uiViewport.getCamera().combined);
        
        // Coordenadas do mouse para hover
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        uiViewport.unproject(mousePos);
        
        // Renderizar botões
        renderer.begin(ShapeType.Filled);
        
        // Zoom In
        boolean zoomInHover = zoomInButton.contains(mousePos.x, mousePos.y);
        renderer.setColor(zoomInHover ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
        renderer.rect(zoomInButton.x, zoomInButton.y, zoomInButton.width, zoomInButton.height);
        
        // Zoom Out
        boolean zoomOutHover = zoomOutButton.contains(mousePos.x, mousePos.y);
        renderer.setColor(zoomOutHover ? BUTTON_HOVER_COLOR : BUTTON_COLOR);
        renderer.rect(zoomOutButton.x, zoomOutButton.y, zoomOutButton.width, zoomOutButton.height);
        
        renderer.end();
        
        // Renderizar bordas
        renderer.begin(ShapeType.Line);
        renderer.setColor(Color.WHITE);
        renderer.rect(zoomInButton.x, zoomInButton.y, zoomInButton.width, zoomInButton.height);
        renderer.rect(zoomOutButton.x, zoomOutButton.y, zoomOutButton.width, zoomOutButton.height);
        renderer.end();
        
        // Renderizar símbolos + e -
        batch.begin();
        font.setColor(Color.WHITE);
        
        font.draw(batch, "+", 
            zoomInButton.x + (zoomInButton.width - font.getData().getGlyph('+').width) / 2,
            zoomInButton.y + (zoomInButton.height + font.getCapHeight()) / 2
        );
        
        font.draw(batch, "-", 
            zoomOutButton.x + (zoomOutButton.width - font.getData().getGlyph('-').width) / 2,
            zoomOutButton.y + (zoomOutButton.height + font.getCapHeight()) / 2
        );
        
        batch.end();
    }
    
    /**
     * Redimensiona o controlador quando a tela é redimensionada
     * @param width nova largura
     * @param height nova altura
     */
    public void resize(int width, int height) {
        updateButtonPositions();
    }
    
    /**
     * Aproxima o zoom (diminui o valor de zoom)
     */
    public void zoomIn() {
        targetZoom = Math.max(minZoom, camera.zoom * 0.8f);
    }
    
    /**
     * Afasta o zoom (aumenta o valor de zoom)
     */
    public void zoomOut() {
        targetZoom = Math.min(maxZoom, camera.zoom * 1.2f);
    }
    
    /**
     * Define limites para o zoom
     * @param min zoom mínimo (valor menor = mais aproximado)
     * @param max zoom máximo (valor maior = mais afastado)
     */
    public void setZoomLimits(float min, float max) {
        this.minZoom = min;
        this.maxZoom = max;
    }
}