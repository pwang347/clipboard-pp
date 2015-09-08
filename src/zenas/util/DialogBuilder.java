package zenas.util;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import zenas.ClipboardPP;

/**
 * Created by Paul on 8/31/2015.
 */
public class DialogBuilder {
    Alert.AlertType alertType;

    public DialogBuilder(Alert.AlertType alertType){
        this.alertType = alertType;
    }
    public DialogBuilder(){};

    public Alert showDialog(String title, String text) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(ClipboardPP.getStage());
        alert.setGraphic(null);
        alert.setContentText(text);
        alert.showAndWait();
        return alert;
    }

    public boolean showYesNoDialog(String title, String text) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonYes, buttonNo);
        alert.setHeaderText(null);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(ClipboardPP.getStage());
        alert.setGraphic(null);
        alert.setContentText(text);
        alert.showAndWait();
        return alert.getResult().equals(buttonYes);
    }

    public Alert showDialog(String title, String text, String header, Node graphic) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.initOwner(ClipboardPP.getStage());
        alert.setGraphic(graphic);
        alert.setContentText(text);
        alert.showAndWait();
        return alert;
    }

    public String showTextDialog(String title, String content){
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(title);
        dialog.setHeaderText("");
        dialog.setContentText(content);
        dialog.initOwner(ClipboardPP.getStage());
        dialog.showAndWait();
        return dialog.getResult();
    }

    public String showTextDialog(String title, String content, String header, String field){
        TextInputDialog dialog = new TextInputDialog(field);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        dialog.initOwner(ClipboardPP.getStage());
        dialog.showAndWait();
        return dialog.getResult();
    }
}
