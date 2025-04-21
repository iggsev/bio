package com.biobox.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Controlador de entrada baseado em teclado e mouse para Desktop
 */
public class KeyboardInputController implements InputController {
    
    private final OrthographicCamera camera;
    private final Vector2 cursorPosition = new Vector2();
    private final Vector2 worldPosition = new Vector2();
    private final Vector3 touchPos = new Vector3();
    private final InputProcessor inputProcessor;
    private float zoomDelta = 0;
    
    private static final float ZOOM_SPEED = 0.1f;
    private static final float PAN_SPEED = 200.0f;
    
    public KeyboardInputController(OrthographicCamera camera) {
        this.camera = camera;
        this.inputProcessor = new KeyboardProcessor();
    }
    
    @Override
    public void update(float deltaTime) {
        // Atualizar posição do cursor
        cursorPosition.set(Gdx.input.getX(), Gdx.input.getY());
        
        // Movimento da câmera com WASD
        float speed = PAN_SPEED * deltaTime * camera.zoom;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.y += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.y -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.x -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.x += speed;
        
        // Zoom com QE
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom += deltaTime * 2f;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) camera.zoom -= deltaTime * 2f;
        
        // Aplicar delta de zoom do scroll
        if (zoomDelta != 0) {
            camera.zoom = Math.max(0.2f, Math.min(10f, camera.zoom + zoomDelta * ZOOM_SPEED));
            zoomDelta = 0;
        }
        
        // Limitar zoom
        camera.zoom = Math.max(0.2f, Math.min(10f, camera.zoom));
        
        // Atualizar câmera
        camera.update();
    }
    
    @Override
    public InputProcessor getInputProcessor() {
        return inputProcessor;
    }
    
    @Override
    public OrthographicCamera getCamera() {
        return camera;
    }
    
    @Override
    public boolean isKeyPressed(int keyCode) {
        return Gdx.input.isKeyPressed(keyCode);
    }
    
    @Override
    public Vector2 getCursorPosition() {
        return cursorPosition;
    }
    
    @Override
    public Vector2 screenToWorld(float screenX, float screenY) {
        touchPos.set(screenX, screenY, 0);
        camera.unproject(touchPos);
        worldPosition.set(touchPos.x, touchPos.y);
        return worldPosition;
    }
    
    @Override
    public boolean isTouched() {
        return Gdx.input.isTouched();
    }
    
    @Override
    public float getZoomDelta() {
        return zoomDelta;
    }
    
    @Override
    public void reset() {
        zoomDelta = 0;
    }
    
    /**
     * Processador de entrada de teclado e mouse
     */
    private class KeyboardProcessor extends InputAdapter {
        @Override
        public boolean scrolled(float amountX, float amountY) {
            zoomDelta = amountY * 0.1f;
            return true;
        }
        
        @Override
        public boolean keyDown(int keycode) {
            // Teclas especiais podem ser processadas aqui
            return false;
        }
        
        @Override
        public boolean keyUp(int keycode) {
            return false;
        }
        
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return false;
        }
    }
}
