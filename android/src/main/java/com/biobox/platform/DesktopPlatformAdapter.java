package com.biobox.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Implementação de PlatformAdapter para Desktop
 */
public class DesktopPlatformAdapter implements PlatformAdapter {
    
    private static final String PREFERENCES_NAME = "biobox_preferences";
    private final Preferences preferences;
    
    public DesktopPlatformAdapter() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
    }
    
    @Override
    public void showMessage(String message) {
        Gdx.app.log("BioBox", "Mensagem: " + message);
    }
    
    @Override
    public void showError(String title, String message) {
        Gdx.app.error("BioBox", title + ": " + message);
    }
    
    @Override
    public boolean hasKeyboard() {
        return true;
    }
    
    @Override
    public boolean hasTouch() {
        return false;
    }
    
    @Override
    public float getDisplayDensity() {
        return 1.0f;
    }
    
    @Override
    public float getPhysicalWidth() {
        return Gdx.graphics.getWidth() / 96f; // Assume 96 DPI
    }
    
    @Override
    public float getPhysicalHeight() {
        return Gdx.graphics.getHeight() / 96f; // Assume 96 DPI
    }
    
    @Override
    public boolean isLandscape() {
        return Gdx.graphics.getWidth() > Gdx.graphics.getHeight();
    }
    
    @Override
    public String getAssetsPath() {
        return "";
    }
    
    @Override
    public String getStoragePath() {
        return Gdx.files.getExternalStoragePath();
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
