package zenas.fxml.setting;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import zenas.ClipboardListener;
import zenas.fxml.main.MainFXController;
import zenas.util.DialogBuilder;
import zenas.util.InputController;
import zenas.pref.Settings;
import zenas.util.TextFormat;

public class SettingFXController {

    private InputController ic;
    private Stage stage;
    public TreeView treeView;
    public Label titleLabel;
    public VBox dialogsContainer;
    public VBox toolboxContainer;
    public VBox advancedContainer;
    public StackPane contentPane;
    public CheckBox hideDialogsCB;
    public CheckBox disableTrayNotifCB;
    public CheckBox animateToolCB;
    public CheckBox autoToolSetCB;
    public CheckBox clearInputCB;
    public TextField maxQueueTF;
    public TextField refreshRateTF;
    public MainFXController controller;
    public ClipboardListener cListen;

    public void init(MainFXController controller, ClipboardListener cListen) {
        this.cListen = cListen;
        this.controller = controller;
        ic = new InputController();
        ContentTreeItem<String> general = new ContentTreeItem<>(null, "General");
        ContentTreeItem<String> general_notifications = new ContentTreeItem<>(dialogsContainer, "Notifications");
        ContentTreeItem<String> general_toolbox = new ContentTreeItem<>(toolboxContainer, "Toolbox");
        general.getChildren().addAll(general_notifications, general_toolbox);
        ContentTreeItem<String> clipboard = new ContentTreeItem<>(null, "Clipboard");
        ContentTreeItem<String> advanced = new ContentTreeItem<>(advancedContainer, "Advanced");
        ContentTreeItem<String> dummyRoot = new ContentTreeItem<>(null,null);
        dummyRoot.getChildren().addAll(general, clipboard, advanced);
        treeView.setRoot(dummyRoot);
        treeView.setShowRoot(false);
        treeView.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue,
                                                                         Object newValue) -> {
                    ContentTreeItem<String> selectedItem = (ContentTreeItem<String>) newValue;
                    if(selectedItem.getGraphics()==null){
                        return;
                    }
                    setContent(selectedItem.getGraphics());
                    titleLabel.setText("Settings: "+selectedItem.getValue());
                }
        );
        contentPane.getChildren().clear();
        treeView.getSelectionModel().select(general_notifications);
        maxQueueTF.textProperty().addListener(TextFormat.numLimit(2));
        refreshRateTF.textProperty().addListener(TextFormat.numLimit(4));
        updateGraphics();
    }

    public void setStage(final Stage stage){
        this.stage = stage;
    }

    public void setContent(VBox box){
        if (contentPane.getChildren().size() > 0) {
            contentPane.getChildren().remove(0);
        }
        contentPane.getChildren().add(box);
    }

    class ContentTreeItem<T> extends TreeItem<T>{
        VBox vb;
        ContentTreeItem(VBox vb, T t){
            super(t);
            this.vb = vb;
        }
        VBox getGraphics(){
            return vb;
        }
    }

    public void updateGraphics(){
        hideDialogsCB.setSelected(Settings.Vars.hideDialogs.getBool());
        disableTrayNotifCB.setSelected(Settings.Vars.hideNotifications.getBool());
        animateToolCB.setSelected(Settings.Vars.animateToolbox.getBool());
        autoToolSetCB.setSelected(Settings.Vars.autoApplyToolbox.getBool());
        clearInputCB.setSelected(Settings.Vars.clearInput.getBool());
        maxQueueTF.setText(Settings.Vars.maximumQueueSize.getInt()+"");
        refreshRateTF.setText(Settings.Vars.refreshRate.getInt()+"");
    }

    public void defaultSettings(){
        for(Settings.Vars v: Settings.Vars.values()){
            v.setDefault();
        }
        updateGraphics();
    }

    public void confirm() {
        saveSettings();
        Settings.save(Settings.Type.VARS);
        stage.close();
    }

    public void saveSettings() {
        Settings.Vars.hideDialogs.setValue(hideDialogsCB.isSelected());
        Settings.Vars.hideNotifications.setValue(disableTrayNotifCB.isSelected());
        Settings.Vars.animateToolbox.setValue(animateToolCB.isSelected());
        Settings.Vars.autoApplyToolbox.setValue(autoToolSetCB.isSelected());
        Settings.Vars.clearInput.setValue(clearInputCB.isSelected());
        int size = Integer.parseInt(maxQueueTF.getText());
        if(size<Settings.Vars.maximumQueueSize.getInt()){
            if(new DialogBuilder().showYesNoDialog("Optional", "Would you like to remove all existing objects beyond the new maximum index?")){
                controller.setPage(0);
                controller.pagination.setPageCount(size);
                cListen.chopQueue(size);
            }
        }
        Settings.Vars.maximumQueueSize.setValue(Integer.parseInt(maxQueueTF.getText()));
        Settings.Vars.refreshRate.setValue(Integer.parseInt(refreshRateTF.getText()));
        cListen.updateMaxQueueSize();
    }

    public void cancel() {
        defaultSettings();
        stage.close();
    }
}
