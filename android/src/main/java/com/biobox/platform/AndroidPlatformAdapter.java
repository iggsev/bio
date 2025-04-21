package com.biobox.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Implementação de PlatformAdapter para Android
 */
public class AndroidPlatformAdapter implements PlatformAdapter {
    
    private static final String PREFERENCES_NAME = "biobox_preferences";
    private final Preferences preferences;
    
    public AndroidPlatformAdapter() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
    }
    
    @Override
    public void showMessage(String message) {
        // No Android, usamos Toast através do handler do LibGDX
        Gdx.app.log("BioBox", "Mensagem: " + message);
        Gdx.app.getApplicationListener().resume();
    }
    
    @Override
    public void showError(String title, String message) {
        // Registrar erro e mostrar Toast
        Gdx.app.error("BioBox", title + ": " + message);
        Gdx.app.getApplicationListener().resume();
    }
    
    @Override
    public boolean hasKeyboard() {
        // Consideramos que Android não tem teclado físico por padrão
        return false;
    }
    
    @Override
    public boolean hasTouch() {
        return true;
    }
    
    @Override
    public float getDisplayDensity() {
        return Gdx.graphics.getDensity();
    }
    
    @Override
    public float getPhysicalWidth() {
        return Gdx.graphics.getWidth() / Gdx.graphics.getPpiX();
    }
    
    @Override
    public float getPhysicalHeight() {
        return Gdx.graphics.getHeight() / Gdx.graphics.getPpiY();
    }
    
    @Override
    public boolean isLandscape() {
        return Gdx.graphics.getWidth() > Gdx.graphics.getHeight();
    }
    
    @Override
    public String getAssetsPath() {
        return ""; // No LibGDX, os assets são acessados através do Gdx.files
    }
    
    @Override
    public String getStoragePath() {
        return Gdx.files.getLocalStoragePath();
    }
    
    @Override
    public void savePreference(String key, String value) {
        preferences.putString(key, value);
        preferences.flush();
    }
    
    @Override
    public String loadPreference(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }
}
