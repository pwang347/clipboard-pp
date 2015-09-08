package zenas.util;

import javafx.scene.input.KeyCode;
import zenas.c;
import zenas.pref.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Paul on 7/11/2015.
 */
public class InputController {
    //-- VARIABLES
    private boolean keyDisabled = false;
    public java.util.List<KeyCode> pressedKeys = new ArrayList<KeyCode>();

    //--GENERAL FUNCTIONS
    public void setKeyDisabled(boolean state) {
        keyDisabled = state;
    }

    public void addKey(KeyCode code) {
        if (!pressedKeys.contains(code) && !keyDisabled)
            pressedKeys.add(code);
    }

    public void removeKey(KeyCode code) {
        while (pressedKeys.contains(code)) {
            pressedKeys.remove(pressedKeys.indexOf(code));
        }
    }

    public void releaseAll() {
        pressedKeys.clear();
    }

    public KeyCode getHighestPriority(KeyCode... codes) {
        int highestIndex = -1;
        for (int i = 0; i < codes.length; i++) {
            if (pressedKeys.indexOf(codes[i]) > highestIndex) {
                highestIndex = pressedKeys.indexOf(codes[i]);
            }
        }
        if (highestIndex > -1)
            return pressedKeys.get(highestIndex);
        else
            return null;
    }

    public KeyCode getHighestPriority(Settings.Keys... keys) {
        int highestIndex = -1;
        for (int i = 0; i < keys.length; i++) {
            if(isPressed(keys[i].getKey(), keys[i].getKeyMods())&&pressedKeys.indexOf(keys[i].getKey()) > highestIndex) {
                    highestIndex = pressedKeys.indexOf(keys[i].getKey());
            }
        }
        if (highestIndex > -1)
            return pressedKeys.get(highestIndex);
        else
            return null;
    }


    public boolean isPressed(KeyCode code, KeyCode... mods) {
        if (!pressedKeys.contains(code)) {
            return false;
        }
        KeyCode[] missingMod = getMissingMods(mods);
        for (int i = 0; i < mods.length; i++) {
            if (!pressedKeys.contains(mods[i])) {
                return false;
            }
        }
        for (int i = 0; i < missingMod.length; i++) {
            if (pressedKeys.contains(missingMod[i])) {
                return false;
            }
        }
        return true;
    }

    public KeyCode[] getMissingMods(KeyCode[] mods) {
        List<KeyCode> missingMods = new ArrayList<KeyCode>();
        missingMods.add(KeyCode.CONTROL);
        missingMods.add(KeyCode.ALT);
        missingMods.add(KeyCode.SHIFT);
        for (int i = 0; i < mods.length; i++) {
            missingMods.remove(mods[i]);
        }
        return missingMods.toArray(new KeyCode[missingMods.size()]);
    }

    public boolean isKeyModifier(KeyCode key) {
        return key.equals(KeyCode.CONTROL) || key.equals(KeyCode.SHIFT) || key.equals(KeyCode.ALT);
    }

    public int keycodeToInt(KeyCode key) {
        int start = Settings.Keys.valueOf("KEY_OBJ1").ordinal();
        for (int i = 0; i < 10; i++) {
            if (key.equals(Settings.Keys.values()[start + i].getKey())) {
                return i + 1;
            }
        }
        return -1;
    }

    public KeyCode[] getMods() {
        List<KeyCode> mods = new ArrayList<KeyCode>();
        for (KeyCode key : pressedKeys) {
            if (isKeyModifier(key)) {
                mods.add(key);
            }
        }
        return mods.toArray(new KeyCode[mods.size()]);
    }

    public KeyCode getPriorityKey() {
        for (int i = pressedKeys.size() - 1; i >= 0; i--) {
            if (!isKeyModifier(pressedKeys.get(i))) {
                return pressedKeys.get(i);
            }
        }
        return null;
    }

    public boolean combinationExists(KeyCode key, KeyCode[] mods) {
        for (int i = 0; i < Settings.Keys.values().length; i++) {
            if (key.equals(Settings.Keys.values()[i].getKey())) {
                if (Arrays.equals(mods, Settings.Keys.values()[i].getKeyMods())) {
                    return true;
                }
            }
        }
        return false;
    }
}
