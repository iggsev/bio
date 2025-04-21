package com.biobox.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Controlador de entrada baseado em toque para Android
 */
public class TouchInputController implements InputController, GestureListener {
    
    // Câmera e estado
    private final OrthographicCamera camera;
    private final Vector2 cursorPosition = new Vector2();
    private final Vector2 worldPosition = new Vector2();
    private final Vector3 touchPos = new Vector3();
    private boolean isTouching = false;
    private float zoomDelta = 0;
    
    // Simulação de teclas (para compatibilidade com código existente)
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    
    // Processadores de entrada
    private final InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private final GestureDetector gestureDetector;
    private final TouchProcessor touchProcessor;
    
    // Constantes
    private static final float ZOOM_SPEED = 0.1f;
    private static final float PAN_SPEED = 10.0f;
    private static final float INITIAL_ZOOM = 1.0f;
    private static final float MIN_ZOOM = 0.2f;
    private static final float MAX_ZOOM = 5.0f;
    
    /**
     * Construtor
     * @param camera câmera a ser controlada
     */
    public TouchInputController(OrthographicCamera camera) {
        this.camera = camera;
        this.camera.zoom = INITIAL_ZOOM;
        
        // Inicializar processadores
        this.gestureDetector = new GestureDetector(this);
        this.touchProcessor = new TouchProcessor();
        
        // Configurar multiplexer
        inputMultiplexer.addProcessor(gestureDetector);
        inputMultiplexer.addProcessor(touchProcessor);
    }
    
    @Override
    public void update(float deltaTime) {
        // Atualizar posição do cursor
        updateCursorPosition();
        
        // Atualizar câmera com base no estado das teclas virtuais
        float speed = PAN_SPEED * deltaTime * camera.zoom;
        if (upPressed) camera.position.y += speed;
        if (downPressed) camera.position.y -= speed;
        if (leftPressed) camera.position.x -= speed;
        if (rightPressed) camera.position.x += speed;
        
        // Aplicar zoom
        if (zoomDelta != 0) {
            camera.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, camera.zoom + zoomDelta * ZOOM_SPEED));
            zoomDelta = 0; // Resetar após aplicar
        }
        
        // Atualizar câmera
        camera.update();
    }
    
    @Override
    public InputProcessor getInputProcessor() {
        return inputMultiplexer;
    }
    
    @Override
    public OrthographicCamera getCamera() {
        return camera;
    }
    
    @Override
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
        return isTouching;
    }
    
    @Override
    public float getZoomDelta() {
        return zoomDelta;
    }
    
    @Override
    public void reset() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
        isTouching = false;
        zoomDelta = 0;
    }
    
    /**
     * Simula o pressionamento de uma tecla
     * @param keyCode código da tecla
     * @param pressed true se pressionada, false se liberada
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
     * Atualiza a posição do cursor com base no toque
     */
    private void updateCursorPosition() {
        if (Gdx.input.isTouched()) {
            cursorPosition.set(Gdx.input.getX(), Gdx.input.getY());
            isTouching = true;
        } else {
            isTouching = false;
        }
    }
    
    // Implementação de GestureListener
    
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        // Registrar toque para cálculos futuros
        isTouching = true;
        cursorPosition.set(x, y);
        return false;
    }
    
    @Override
    public boolean tap(float x, float y, int count, int button) {
        // Simular clique quando o usuário toca rapidamente
        Gdx.app.log("TouchInput", "Tap em " + x + ", " + y + ", contagem: " + count);
        return false;
    }
    
    @Override
    public boolean longPress(float x, float y) {
        // Simular clique longo
        Gdx.app.log("TouchInput", "Long press em " + x + ", " + y);
        return false;
    }
    
    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        // Poderia implementar momentum para a câmera
        return false;
    }
    
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        // Mover a câmera quando o usuário arrasta
        camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        return true;
    }
    
    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }
    
    @Override
    public boolean zoom(float initialDistance, float distance) {
        // Zoom quando o usuário faz pinch
        float ratio = initialDistance / distance;
        zoomDelta = (ratio - 1) * 0.5f;
        return true;
    }
    
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        // Geralmente não é usado porque zoom() já lida com o pinch
        return false;
    }
    
    @Override
    public void pinchStop() {
        // Resetar delta de zoom quando o pinch termina
        zoomDelta = 0;
    }
    
    /**
     * Processador de toque para entrada adicional
     */
    private class TouchProcessor implements InputProcessor {
        @Override
        public boolean keyDown(int keycode) {
            return false;
        }
        
        @Override
        public boolean keyUp(int keycode) {
            return false;
        }
        
        @Override
        public boolean keyTyped(char character) {
            return false;
        }
        
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            isTouching = true;
            cursorPosition.set(screenX, screenY);
            return false;
        }
        
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            isTouching = false;
            return false;
        }
        
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            cursorPosition.set(screenX, screenY);
            return false;
        }
        
        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }
        
        @Override
        public boolean scrolled(float amountX, float amountY) {
            zoomDelta = amountY * 0.1f;
            return true;
        }
        
        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            isTouching = false;
            return false;
        }
    }
}