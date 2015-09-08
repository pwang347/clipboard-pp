package zenas.fxml.key;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import zenas.pref.Settings;
import zenas.util.InputController;

import java.util.Arrays;

public class KeyFXController {

    Stage stage;
    public TableView tableView;
    public TableColumn funcCol;
    public TableColumn keyCol;
    InputController ic = new InputController();
    ObservableList<KeyItem> data;
    public Label keyWarning;

    public void setStage(final Stage stage){
        this.stage = stage;
    }

    public void mainKeyReleased(KeyEvent keyEvent) {
        ic.removeKey(keyEvent.getCode());
    }

    public void mainKeyPressed(KeyEvent keyEvent) {
        if (keyWarning.isVisible()) {
            keyWarning.setVisible(false);
        }
        ic.addKey(keyEvent.getCode());
        KeyCode key = ic.getPriorityKey();
        KeyCode[] mods = ic.getMods();
        int index = tableView.getSelectionModel().getSelectedIndex();
        if (index != -1 && key != null) {
            System.out.println(ic.combinationExists(key, mods));
            if (!ic.combinationExists(key, mods)) {
                data.set(index, new KeyItem(Settings.Keys.values()[index].getDescription(), key, mods));
                saveHotkeys(index);
                tableView.getSelectionModel().select(index);
            } else {
                keyWarning.setVisible(true);
            }
        }
    }

    public class KeyItem {
        private StringProperty funcName;
        private StringProperty keyCombination;
        private KeyCode key;
        private KeyCode[] mods;

        private KeyItem(String funcName, KeyCode key, KeyCode... mods) {
            this.funcName = new SimpleStringProperty(funcName);
            this.key = key;
            this.mods = mods;
            String keyCombination = "";
            for (int i = 0; i < mods.length; i++) {
                keyCombination += mods[i].getName() + " + ";
            }
            if (key != null)
                keyCombination += key.getName();
            this.keyCombination = new SimpleStringProperty(keyCombination);
        }

        public StringProperty funcNameProperty() {
            return funcName;
        }

        public StringProperty keyCombinationProperty() {
            return keyCombination;
        }
    }

    public void updateTable() {
        data = FXCollections.observableArrayList();
        for (int i = 0; i < Settings.Keys.values().length; i++) {
            KeyItem item = new KeyItem(Settings.Keys.values()[i].getDescription(), Settings.Keys.values()[i].getKey(), Settings.Keys.values()[i].getKeyMods());
            data.add(item);
        }
        funcCol.setCellValueFactory(new PropertyValueFactory("funcName"));
        keyCol.setCellValueFactory(new PropertyValueFactory("keyCombination"));
        tableView.setItems(data);
    }

    public void saveHotkeys() {
        for (int i = 0; i < data.size(); i++) {
            if (!Settings.Keys.values()[i].getKey().equals(data.get(i).key))
                Settings.Keys.values()[i].setKey(data.get(i).key);
            if (!Arrays.equals(Settings.Keys.values()[i].getKeyMods(), data.get(i).mods))
                Settings.Keys.values()[i].setKeyMods(data.get(i).mods.clone());
        }
        System.out.println(Settings.Keys.KEY_DELETE.getKeyMods().length);
    }

    public void saveHotkeys(int index) {
        Settings.Keys.values()[index].setKey(data.get(index).key);
        Settings.Keys.values()[index].setKeyMods(data.get(index).mods.clone());
    }

    public void confirm() {
        saveHotkeys();
        Settings.save(Settings.Type.KEYS);
        stage.close();
    }

    public void defaultKeys() {
        if (keyWarning.isVisible()) {
            keyWarning.setVisible(false);
        }
        for(Settings.Keys k : Settings.Keys.values()){
            k.setDefault();
        }
        updateTable();
    }

    public void cancel() {
        defaultKeys();
        stage.close();
    }
}