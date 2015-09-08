package zenas.fxml.main;

import java.awt.*;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

import zenas.ClipboardListener;
import zenas.ClipboardPP;
import zenas.c;
import zenas.fxml.key.KeyFXController;
import zenas.fxml.setting.SettingFXController;
import zenas.util.DialogBuilder;


public class MenuFXController {

    private CheckMenuItem show_faq_menu;
    private CheckMenuItem disable_menu;
    private ClipboardListener l;
    private MainFXController controller;

    public void init(ClipboardListener l, MainFXController controller){
        this.l = l;
        this.controller = controller;
    }

    public void setHomePane(boolean state) {
        if (show_faq_menu.isSelected() != state) {
            show_faq_menu.setSelected(state);
        }
        if (state == true || l.getQueue().size() > 0) {
            controller.toolPane.setExpanded(false);
            controller.dataPreviewBox.setVisible(!state);
            controller.homePane.setVisible(state);
            controller.faq_display.requestFocus();
        } else {
            controller.contentPane.requestFocus();
        }
    }

    public void showHome() {
        if(controller.toolPane.isExpanded()) {
            controller.toolPane.setExpanded(false);
        }
        setHomePane(show_faq_menu.isSelected());

    }

    public void newItem() {
        String text = new DialogBuilder(null).showTextDialog("Create a new clipboard object", "Text:");
        if (text != null && text != "") {
            l.setClipObject(text);
        }
    }

    public void saveFile() {
       controller.saveFile();
    }

    public void exit() {
        ClipboardPP.getStage().close();
        System.exit(0);
    }

    public void editToggle() {
       controller.editToggle();
    }

    public void delPageDialog() {
        controller.delPageDialog();
    }

    public void aboutPage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(ClipboardPP.getStage());
        alert.setContentText("Copyright 2015 Zenas Apps\nAll Rights Reserved.");
        alert.showAndWait();
    }

    public void settingsPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ClipboardPP.class.getResource("fxml/setting/setting.fxml"));
            Parent root2 = fxmlLoader.load();
            SettingFXController settingControl = fxmlLoader.getController();
            settingControl.init(controller, l);
            Stage setStage = new Stage();
            setStage.setScene(new Scene(root2));
            setStage.initOwner(ClipboardPP.getStage());
            setStage.setTitle("Settings");
            setStage.getIcons().add(new javafx.scene.image.Image(String.valueOf(ClipboardPP.class.getResource("res/icon.png"))));
            setStage.getIcons().add(new javafx.scene.image.Image(String.valueOf(ClipboardPP.class.getResource("res/icon64.png"))));
            setStage.show();
            settingControl.setStage(setStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void keyBindingsPage() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ClipboardPP.class.getResource("fxml/key/key.fxml"));
            Parent root1 = fxmlLoader.load();
            KeyFXController keyControl = fxmlLoader.getController();
            Stage keyStage = new Stage();
            keyStage.setScene(new Scene(root1));
            keyStage.initOwner(ClipboardPP.getStage());
            keyStage.setTitle("Key Bindings");
            keyStage.getIcons().add(new javafx.scene.image.Image(String.valueOf(ClipboardPP.class.getResource("res/icon.png"))));
            keyStage.getIcons().add(new javafx.scene.image.Image(String.valueOf(ClipboardPP.class.getResource("res/icon64.png"))));
            keyStage.show();
            keyControl.setStage(keyStage);
            keyControl.updateTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleDisable() {
        setDisable(disable_menu.isSelected());
    }

    public void setDisable(boolean state) {
        if(state) {
            ClipboardPP.getStage().setTitle(c.TITLE_TEXT+" - DISABLED");
            ClipboardPP.getTrayHandler().showTrayMessage(TrayIcon.MessageType.INFO, "Clipboard++ disabled", "Clipboard++ will no longer detect changes to clipboard data until re-enabled.");
        } else {
            ClipboardPP.getStage().setTitle(c.TITLE_TEXT);
            ClipboardPP.getTrayHandler().showTrayMessage(TrayIcon.MessageType.INFO, "Clipboard++ enabled", "Clipboard++ will now detect changes to clipboard data.");
        }
        l.setDisabled(state);
    }
}
