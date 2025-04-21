package com.biobox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * Adaptador para entrada de toque no Android, simulando controles básicos para o WorldEditorMain
 */
public class AndroidTouchAdapter extends InputAdapter {
    private OrthographicCamera camera;
    private float startX, startY;
    private float lastX, lastY;
    private boolean isDragging = false;
    private boolean isPinching = false;
    private float initialDistance = 0;
    private float initialZoom = 0;
    
    // Para simulação de WASD
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    // Constantes
    private static final float DRAG_THRESHOLD = 5f; // Distância em pixels para iniciar arrasto
    private static final float TOUCH_MOVE_SPEED = 15f; // Velocidade de movimento da câmera
    
    public AndroidTouchAdapter(OrthographicCamera camera) {
        this.camera = camera;
    }
    
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            // Primeiro toque
            startX = screenX;
            startY = screenY;
            lastX = screenX;
            lastY = screenY;
            isDragging = false;
        } else if (pointer == 1 && !isPinching) {
            // Segundo toque (pinch para zoom)
            isPinching = true;
            
            // Calcular distância inicial entre os dois toques
            float x0 = startX;
            float y0 = startY;
            float x1 = screenX;
            float y1 = screenY;
            initialDistance = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
            initialZoom = camera.zoom;
        }
        
        return true;
    }
    
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            isDragging = false;
            
            // Verificar se foi um toque simples (sem arrastar)
            if (Math.abs(screenX - startX) < DRAG_THRESHOLD && 
                Math.abs(screenY - startY) < DRAG_THRESHOLD) {
                handleTap(screenX, screenY);
            }
        }
        
        if (pointer == 0 || pointer == 1) {
            isPinching = false;
        }
        
        // Resetar estados dos botões simulados
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        
        return true;
    }
    
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == 0) {
            float deltaX = (lastX - screenX) * camera.zoom;
            float deltaY = (screenY - lastY) * camera.zoom;
            
            if (!isDragging) {
                isDragging = Math.abs(screenX - startX) > DRAG_THRESHOLD || 
                            Math.abs(screenY - startY) > DRAG_THRESHOLD;
            }
            
            if (isDragging && !isPinching) {
                // Mover a câmera
                camera.position.x += deltaX;
                camera.position.y += deltaY;
                camera.update();
            }
            
            lastX = screenX;
            lastY = screenY;
        } else if (pointer == 1 && isPinching) {
            // Calcular nova distância para pinch zoom
            float x0 = lastX;
            float y0 = lastY;
            float x1 = screenX;
            float y1 = screenY;
            float currentDistance = (float) Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
            
            // Ajustar zoom
            if (initialDistance > 0) {
                float zoomFactor = initialDistance / currentDistance;
                camera.zoom = initialZoom * zoomFactor;
                camera.zoom = Math.max(0.2f, Math.min(camera.zoom, 10f)); // Limitar zoom
                camera.update();
            }
        }
        
        return true;
    }
    
    /**
     * Tratar toque simples na tela
     */
    private void handleTap(int screenX, int screenY) {
        // Converter coordenadas da tela para coordenadas de mundo
        Vector3 worldCoords = new Vector3(screenX, screenY, 0);
        camera.unproject(worldCoords);
    }
    
    /**
     * Simular pressionamento de tecla WASD com botões na tela
     */
    public void simulateKeyPress(int keyCode, boolean pressed) {
        switch (keyCode) {
            case Input.Keys.W:
                upPressed = pressed;
                break;
            case Input.Keys.S:
                downPressed = pressed;
                break;
            case Input.Keys.A:
                leftPressed = pressed;
                break;
            case Input.Keys.D:
                rightPressed = pressed;
                break;
        }
    }
    
    /**
     * Atualizar a câmera com base nos toques na tela
     * Chamar este método no método render() para movimentos suaves
     */
    public void update(float deltaTime) {
        float moveSpeed = TOUCH_MOVE_SPEED * deltaTime * camera.zoom;
        
        if (upPressed) camera.position.y += moveSpeed;
        if (downPressed) camera.position.y -= moveSpeed;
        if (leftPressed) camera.position.x -= moveSpeed;
        if (rightPressed) camera.position.x += moveSpeed;
        
        if (upPressed || downPressed || leftPressed || rightPressed) {
            camera.update();
        }
    }
    
    /**
     * Verificar se uma tecla simulada está pressionada (para compatibilidade com Gdx.input.isKeyPressed)
     */
    public boolean isKeyPressed(int keyCode) {
        switch (keyCode) {
            case Input.Keys.W:
                return upPressed;
            case Input.Keys.S:
                return downPressed;
            case Input.Keys.A:
                return leftPressed;
            case Input.Keys.D:
                return rightPressed;
            default:
                return false;
        }
    }
}