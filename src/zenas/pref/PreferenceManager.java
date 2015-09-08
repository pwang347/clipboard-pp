package zenas.pref;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.scene.input.KeyCode;

public class PreferenceManager {
    public static Preferences prefs = Preferences.userRoot().node(PreferenceManager.class.getName());

    //General multi-purpose. Returns castable object based on key with default
    public static Object loadPreference(Class c, String key, Object def) {
        if (c.equals(int.class)||c.equals(Integer.class)) {
            return prefs.getInt(key, (int) def);
        } else if (c.equals(String.class)) {
            return prefs.get(key, (String) def);
        } else if (c.equals(boolean.class)||c.equals(Boolean.class)) {
            return prefs.getBoolean(key, (boolean) def);
        }
        return null;
    }

    public static KeyCode loadKey(int key) {
        KeyCode k = KeyCode.getKeyCode(prefs.get("KEY" + key, Settings.Keys.values()[key].getDefaultKey().getName()));
        return k;
    }

    public static KeyCode[] loadKeyMods(int key) {
        List<KeyCode> keys = new ArrayList<KeyCode>();
        String s;
        for (int i = 0; i < 3; i++) {
            if (i < Settings.Keys.values()[key].getDefaultKeyMods().length) {
                s = prefs.get("M" + i + "KEY" + key, Settings.Keys.values()[key].getDefaultKeyMods()[i].getName());
            } else {
                s = prefs.get("M" + i + "KEY" + key, null);
            }
            if (s == null || s.equals("empty")) {
                break;
            }
            KeyCode code = KeyCode.getKeyCode(s);
            keys.add(code);
        }
        return keys.toArray(new KeyCode[keys.size()]);
    }

    //Puts object with key
    public static void putPreference(Class c, String key, Object o) {
        if (c.equals(int.class)||c.equals(Integer.class)) {
            prefs.putInt(key, (int) o);
        } else if (c.equals(String.class)) {
            prefs.put(key, (String) o);
        } else if (c.equals(boolean.class)||c.equals(Boolean.class)) {
            prefs.putBoolean(key, (boolean) o);
        } else {
            System.out.println(c.toString());
        }
    }

    public static void putKey(int key) {
        prefs.put("KEY" + key, Settings.Keys.values()[key].getKey().getName());
    }

    public static void putKeyMods(int key) {
        if (Settings.Keys.values()[key].getKeyMods().length < 1) {
            prefs.put("M0KEY" + key, "empty");
        } else {
            for (int i = 0; i < 3; i++) {
                if (i < Settings.Keys.values()[key].getKeyMods().length) {
                    prefs.put("M" + i + "KEY" + key, Settings.Keys.values()[key].getKeyMods()[i].getName());
                } else {
                    prefs.remove("M" + i + "KEY" + key);
                }
            }
        }
    }

    //Clears all pref
    public static void clearPreferences() {
        try {
            prefs.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
}
