package zenas.pref;

import javafx.scene.input.KeyCode;

public class Settings {
    public enum Type {
        KEYS, VARS
    }

    public enum Keys{
        KEY_SAVE("Export clipboard data", KeyCode.S, KeyCode.CONTROL),
        KEY_HIDE("Minimize window to tray", KeyCode.H, KeyCode.CONTROL),
        KEY_EDIT("Edit current data", KeyCode.E, KeyCode.CONTROL),
        KEY_DELETE("Delete current data", KeyCode.D, KeyCode.CONTROL),
        KEY_TOOL("Expand the toolbox window", KeyCode.T, KeyCode.CONTROL),
        KEY_NEW("Create new clipboard object", KeyCode.N, KeyCode.CONTROL),
        KEY_OBJ1("Select data object #1", KeyCode.DIGIT1),
        KEY_OBJ2("Select data object #2", KeyCode.DIGIT2),
        KEY_OBJ3("Select data object #3", KeyCode.DIGIT3),
        KEY_OBJ4("Select data object #4", KeyCode.DIGIT4),
        KEY_OBJ5("Select data object #5", KeyCode.DIGIT5),
        KEY_OBJ6("Select data object #6", KeyCode.DIGIT6),
        KEY_OBJ7("Select data object #7", KeyCode.DIGIT7),
        KEY_OBJ8("Select data object #8", KeyCode.DIGIT8),
        KEY_OBJ9("Select data object #9", KeyCode.DIGIT9),
        KEY_OBJ10("Select data object #10", KeyCode.DIGIT0),
        KEY_OBJN("Select the next data object", KeyCode.RIGHT, KeyCode.CONTROL),
        KEY_OBJP("Select the previous data object", KeyCode.LEFT, KeyCode.CONTROL),
        KEY_OBJL("Select the last data object", KeyCode.BACK_QUOTE),
        KEY_EXIT("Exits the application", KeyCode.W, KeyCode.CONTROL);

        private String description;
        private KeyCode defaultKey;
        private KeyCode key;
        private KeyCode[] defaultKeyMods;
        private KeyCode[] keyMods;

        Keys(String description, KeyCode key, KeyCode... keyMods){
            this.description = description;
            this.key = key;
            this.defaultKey = key;
            this.keyMods = keyMods;
            this.defaultKeyMods = keyMods;
        }

        public void setDefault(){
            key = defaultKey;
            keyMods = defaultKeyMods;
        }

        public String getDescription(){
            return description;
        }

        public KeyCode getKey(){
            return key;
        }

        public void setKey(KeyCode key){
            this.key = key;
        }

        public KeyCode getDefaultKey(){
            return defaultKey;
        }

        public KeyCode[] getKeyMods(){
            return keyMods;
        }

        public void setKeyMods(KeyCode[] keyMods){
            this.keyMods = keyMods;
        }

        public KeyCode[] getDefaultKeyMods(){
            return defaultKeyMods;
        }
    }

    public enum Vars {
        hideDialogs(false), hideNotifications(false), animateToolbox(true), autoApplyToolbox(false), clearInput(true), maximumQueueSize(10), refreshRate(10), shiftAllFiles(false);
        private Object value;
        private Object defaultValue;

        Vars(Object value) {
            this.defaultValue = value;
            this.value = value;
        }

        public Object getValue() {
            return value;
        }

        public boolean getBool() {
            return (boolean) value;
        }

        public int getInt() {
            return (int) value;
        }

        public Object getDefault() {
            return defaultValue;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public void setDefault(){
            System.out.println(value.toString() + " became " + defaultValue.toString());
            value = defaultValue;
        }
    }

    public static void load() {
        for (int i = 0; i < Keys.values().length; i++) {
            try {
                Keys.values()[i].setKey(PreferenceManager.loadKey(i));
                Keys.values()[i].setKeyMods(PreferenceManager.loadKeyMods(i));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < Vars.values().length; i++) {
            Vars.values()[i].setValue(PreferenceManager.loadPreference(Vars.values()[i].getDefault().getClass(), "VAR" + i, Vars.values()[i].getDefault()));
        }
    }

    public static void save(Type type) {
        if (type == Type.KEYS) {
            for (int i = 0; i < Keys.values().length; i++) {
                PreferenceManager.putKey(i);
                PreferenceManager.putKeyMods(i);
            }
        } else if (type == Type.VARS) {
            for (int i = 0; i < Vars.values().length; i++) {
                PreferenceManager.putPreference(Vars.values()[i].getDefault().getClass(), "VAR" + i, Vars.values()[i].getValue());
            }
        }
    }

    public static void setExportDialogPath(String path) {
        PreferenceManager.putPreference(String.class, "EXPORT_PATH", path);
    }

    public static String getExportDialogPath() {
        return (String) PreferenceManager.loadPreference(String.class, "EXPORT_PATH", "/");
    }

    public static void setMaxQueueSize(int size) {
        PreferenceManager.putPreference(int.class, "MAX_QUEUE_SIZE", size);
    }

    public static int getMaxQueueSize() {
        return (int) PreferenceManager.loadPreference(Integer.class, "MAX_QUEUE_SIZE", 10);
    }
}
