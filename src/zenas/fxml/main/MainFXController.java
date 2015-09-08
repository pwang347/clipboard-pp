package zenas.fxml.main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.net.CookieManager;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.controlsfx.control.PlusMinusSlider;

import zenas.*;
import zenas.pref.Settings;
import zenas.util.DialogBuilder;
import zenas.util.InputController;
import zenas.util.TextFormat;

//FXML controller for primary stage
public class MainFXController {

    //root pane and main components
    public BorderPane rootPane;
    public Pagination pagination;
    public ToggleButton editToggleBtn;
    private InputController ic = new InputController();
    public AnchorPane centerPane;
    public VBox dataPreviewBox;
    public MenuBar menu;
    public MenuFXController menuController;
    private ClipboardListener cListen;

    //content pane holding different views depending on clipobject type --- types listed below
    public AnchorPane contentPane;
    public VBox progressBox;
    //text pane and child nodes
    public AnchorPane textPane;
    public TextArea textArea;
    //image pane and child nodes
    public AnchorPane imagePane;
    public ScrollPane imageScroll;
    public ImageView imageView;
    public Slider zoomSlider;
    public TextField zoomField;
    public WritableImage originalImage;
    public PixelReader originalImageReader;
    public WritableImage newImage;
    //web pane and child nodes
    public AnchorPane webPane;
    public WebView webView;
    public WebEngine webEngine;
    private CookieManager manager;
    //file pane and child nodes
    public List<File> fileList = new ArrayList<>();
    public AnchorPane filePane;
    public TextField urlText;
    public Label itemCountLabel;
    public ScrollPane fileScroll;
    public TilePane fileContainer;

    //home page panel and child nodes
    public TabPane homePane;
    public WebView faq_display;
    public WebView release_notes_display;

    //toolbox pane
    public TitledPane toolPane;
    public AnchorPane toolContentPane;
    public AnchorPane toolPaneContainer;

    //text tools
    public VBox textTools;
    public TextField removeField;
    public TextField newLenField;
    public TextField oldTField;
    public TextField newTField;
    public TextField rotationField;
    //image tools
    public VBox imageTools;
    public Pane rotateContainer;
    public TextField widthField;
    public TextField heightField;
    //web tools
    public VBox webTools;
    public TextArea htmlEditor;
    //file tools
    public VBox fileTools;
    public TextField fileNameTF;
    public TextField filepathTF;
    public int fileIndex = -1;
    public TextField namePatternTF;

    public void init(ClipboardListener cListen) {
        this.cListen = cListen;
        cListen.updateMaxQueueSize();
        menuController.init(cListen, this);
        loadHome();
        //show tool view when expanded
        toolPane.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    showTools(newValue);
                }
        );
        initImageView();
        newLenField.textProperty().addListener(TextFormat.numLimit(4));
        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            reset();
            cListen.setClipObject((Integer) newValue);
        });
        zoomSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    int value = newValue.intValue();
                    zoomField.setText(value + "");
                    zoomImage(value);
                }
        );
        webView.getEngine().locationProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    urlText.setText(newValue);
                }
        );
        webView.addEventFilter(ScrollEvent.ANY, event -> {
            if (event.isControlDown()) {
                double x = event.getDeltaY() / 500;
                webView.setZoom(webView.getZoom() + x);
                event.consume();
            }

        });
        manager = new java.net.CookieManager();
        java.net.CookieHandler.setDefault(manager);
        fileContainer.setOnMousePressed(event -> {
            deselectFileItem();
        });
        contentPane.getChildren().clear();
        toolContentPane.getChildren().clear();
        contentPane.requestFocus();
    }

    public void initImageView(){
        PlusMinusSlider rotateSlider = new PlusMinusSlider();
        rotateSlider.setOnValueChanged(event -> {
            setRotation(imageView.getRotate() + 5 * event.getValue());
        });
        rotateContainer.getChildren().add(rotateSlider);

        imageView.rotateProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    rotationField.setText(newValue.intValue() + "");
                }
        );
        imageScroll.addEventFilter(ScrollEvent.ANY, scrollEvent -> {
            if (scrollEvent.isControlDown()) {
                double zoom = zoomSlider.getValue();
                double delta = scrollEvent.getDeltaY();
                if (zoom < 50) {
                    delta /= 10;
                } else if (zoom < 100) {
                    delta /= 5;
                }
                zoomSlider.setValue(zoomSlider.getValue() + delta);
                scrollEvent.consume();
            }
        });
        rotationField.textProperty().addListener(TextFormat.numLimit(3));
        zoomField.textProperty().addListener(TextFormat.numLimit(4));
        widthField.textProperty().addListener(TextFormat.numLimit(4));
        heightField.textProperty().addListener(TextFormat.numLimit(4));
    }

    public void setPane(AnchorPane pane) {
        if (contentPane.getChildren().size() > 0) {
            contentPane.getChildren().remove(0);
        }
        contentPane.getChildren().add(pane);
    }

    public void loadHome() {
        faq_display.getEngine().loadContent(c.FAQ_TEXT);
        release_notes_display.getEngine().loadContent(c.DEV_TEXT);
    }

    public void editToggle() {
        if (cListen.getQueue().size() > 0) {
            if (contentPane.getChildren().get(0).equals(textPane)) {
                if (textArea.isEditable()) {
                    textArea.setEditable(false);
                    updateTextArea(textArea.getText());
                    rootPane.requestFocus();
                } else {
                    textArea.setEditable(true);
                    textArea.requestFocus();
                }
                if (editToggleBtn.isSelected() != textArea.isEditable()) {
                    editToggleBtn.setSelected(textArea.isEditable());
                }
            } else if (contentPane.getChildren().get(0).equals(imagePane) || contentPane.getChildren().get(0).equals(webPane)) {
                toggleToolView();
                if (editToggleBtn.isSelected() != toolPane.isExpanded()) {
                    editToggleBtn.setSelected(toolPane.isExpanded());
                }
            }
        }
    }

    public void saveFile() {
        if (cListen.getQueue().size() > 0) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            setFilters(fileChooser);
            File file = fileChooser.showSaveDialog(ClipboardPP.getStage());
            if (file != null) {
                if (cListen.getDataType().equals("String")) {
                    try {
                        PrintWriter out = new PrintWriter(file.toString());
                        out.println(textArea.getText());
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (cListen.getDataType().equals("Image")) {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(imageView.snapshot(null, null), null), "png", file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (cListen.getDataType().equals("Hyperlink")) {
                    try {
                        PrintWriter out = new PrintWriter(file.toString());
                        out.println((String) webView.getEngine().executeScript("document.documentElement.outerHTML"));
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setFilters(FileChooser chooser) {
        String type = cListen.getDataType();
        if (type.equals("String")) {
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Text", "*.*"),
                    new FileChooser.ExtensionFilter("TXT", "*.txt")
            );
        } else if (type.equals("Image")) {
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Images", "*.*"),
                    new FileChooser.ExtensionFilter("PNG", "*.png")
            );
        } else if (type.equals("Hyperlink")) {
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Text", "*.*"),
                    new FileChooser.ExtensionFilter("TXT", "*.txt"),
                    new FileChooser.ExtensionFilter("HTML", "*.html")
            );
        } else if (type.equals("FileList")) {
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("All Text", "*.*"),
                    new FileChooser.ExtensionFilter("TXT", "*.txt"),
                    new FileChooser.ExtensionFilter("HTML", "*.html")
            );
        }

    }

    public void setPage(int index) {
        if (index >= cListen.getQueue().size()) {
            index = cListen.getQueue().size() - 1;
        }
        pagination.setCurrentPageIndex(index);
        cListen.setClipObject(index);
    }

    public void addPage() {
        if (!pagination.isVisible()) {
            pagination.setVisible(true);
        } else {
            if (pagination.getPageCount() < Settings.Vars.maximumQueueSize.getInt())
                pagination.setPageCount(pagination.getPageCount() + 1);
        }
    }

    public void delPageDialog() {
        if (cListen.getQueue().size() > 0) {
            if (!Settings.Vars.hideDialogs.getBool()) {
                if (new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Warning!", "Are you sure you want to permanently delete this object?")
                        .getResult().getText().equals("OK")) {
                    delCurrentPage();
                }
            } else {
                delCurrentPage();
            }
        }
    }

    public void delCurrentPage() {
        if (pagination.getPageCount() == 1) {
            pagination.setVisible(false);
            if (textArea.isEditable()) {
                textArea.setText("");
                textArea.setEditable(false);
                editToggleBtn.setSelected(false);
            }
        }
        boolean move = false;
        cListen.removeFromQueue(pagination.getCurrentPageIndex());
        if (pagination.getCurrentPageIndex() == pagination.getPageCount() - 1) {
            move = true;
        }
        pagination.setPageCount(pagination.getPageCount() - 1);
        if (move) {
            pagination.setCurrentPageIndex(pagination.getPageCount() - 1);
        }
    }

    //Toolbox functions
    public void toggleToolView() {
        if (cListen.getQueue().size() > 0) {
            if ((int) (ClipboardPP.getStage().getWidth()) == c.WIDTH_EXPANDED) {
                toolPane.setExpanded(false);
            } else if ((int) (ClipboardPP.getStage().getWidth()) == c.WIDTH_COLLAPSED) {
                toolPane.setExpanded(true);
            }
        }
    }

    public void resizeStageWidth(double width, int numFrames, int delay) {
        Timer animTimer = new Timer();
        synchronized (this) {
            animTimer.scheduleAtFixedRate(new TimerTask() {
                int counter = 0;
                double deltaX = (width - ClipboardPP.getStage().getWidth()) * 1 / numFrames;

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (counter <= numFrames + 2) {
                            //two frames for a bouncing-esque animation
                            if (counter > numFrames) {
                                ClipboardPP.getStage().setWidth(ClipboardPP.getStage().getWidth() - deltaX / 2);
                            } else if (deltaX != 0) {
                                ClipboardPP.getStage().setWidth(ClipboardPP.getStage().getWidth() + deltaX);
                            }
                        } else {
                            this.cancel();
                            ClipboardPP.getStage().setWidth((int) (width));
                        }
                        counter++;
                    });
                }
            }, 0, delay);
        }
    }

    public void showTools(boolean isToolView) {
        if (isToolView) {
            if (Settings.Vars.animateToolbox.getBool()) {
                resizeStageWidth(c.WIDTH_EXPANDED, 10, 15);
            } else {
                ClipboardPP.getStage().setWidth(c.WIDTH_EXPANDED);
            }
            toolPaneContainer.setMaxHeight(Control.USE_COMPUTED_SIZE);
        } else {
            if (Settings.Vars.animateToolbox.getBool()) {
                resizeStageWidth(c.WIDTH_COLLAPSED, 10, 15);
            } else {
                ClipboardPP.getStage().setWidth(c.WIDTH_COLLAPSED);
            }
            toolPaneContainer.setMaxHeight(Control.USE_PREF_SIZE);
        }
    }

    public void setToolPane(VBox box) {
        if (toolContentPane.getChildren().size() > 0) {
            toolContentPane.getChildren().remove(0);
        }
        toolContentPane.getChildren().add(box);
    }

    public void zoomImage(int val) {
        if (val > 1000) {
            zoomField.setText("1000");
            val = 1000;
        } else if (val < 1) {
            zoomField.setText("1");
            val = 1;
        }
        imageView.setFitHeight(val / 100.0f * originalImage.getHeight());
        imageView.setFitWidth(val / 100.0f * originalImage.getWidth());
    }

    public void setImage() {
        if (!cListen.isDisabled()) {
            originalImage = (WritableImage) cListen.getClipboardImage(cListen.getClipObject());
            originalImageReader = originalImage.getPixelReader();
            newImage = new WritableImage(originalImage.getPixelReader(), (int) (originalImage.getWidth()), (int) (originalImage.getHeight()));
            Platform.runLater(() -> {
                imageView.setImage(originalImage);
                scaleImageToFit();
                widthField.setText(String.valueOf((int) originalImage.getWidth()));
                heightField.setText(String.valueOf((int) originalImage.getHeight()));
            });
        }
    }

    public void scaleImageToFit() {
        int percent = (int) (Math.round(Math.min(320.0 / originalImage.getHeight(), c.WIDTH_COLLAPSED / originalImage.getWidth()) * 100) - 1);
        if (percent < 1) {
            percent = 1;
        } else if (percent > 1000) {
            percent = 1000;
        }
        zoomSlider.setValue(percent);
    }

    public void rotateCW() {
        setRotation(imageView.getRotate() + 90.0d);
    }

    public void rotateCCW() {
        setRotation(imageView.getRotate() - 90.0d);
    }

    public void setRotation(double angle) {
        angle = angle % 360;
        imageView.setRotate(negAngleToPos(angle));
    }

    double parseAngle(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Input error", "Invalid input.");
            return imageView.getRotate();
        }
    }

    public double negAngleToPos(double angle) {
        if (angle < 0) {
            return angle + 360;
        } else {
            return angle;
        }
    }

    public void flipHorizontal() {
        imageView.setScaleX(imageView.getScaleX() * -1);
    }

    public void flipVertical() {
        imageView.setScaleY(imageView.getScaleY() * -1);
    }

    public void toggleImageEffect(Effect effect) {
        if (imageView.getEffect() != null) {
            if (!imageView.getEffect().getClass().equals(effect.getClass())) {
                imageView.setEffect(effect);
            } else {
                imageView.setEffect(null);
            }
        } else {
            imageView.setEffect(effect);
        }
    }

    public void toBlackWhite() {
        toggleImageEffect(new ColorAdjust(0, -1, 0, 0));
    }

    public void toSepia() {
        toggleImageEffect(new SepiaTone());
    }

    public void applyImage() {
        zoomSlider.setValue(100.0d);
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(javafx.scene.paint.Color.TRANSPARENT);
        cListen.setEdit(true);
        cListen.setClipObject(imageView.snapshot(sp, null));
        imageView.setEffect(null);
        imageView.setRotate(0);
    }

    public void applyImageDialog() {
        if (!Settings.Vars.hideDialogs.getBool()) {
            if (new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Warning!", "Performing this action will override and delete the previous data.")
                    .getResult().getText().equals("OK")) {
                applyImage();
            }
        } else {
            applyImage();
        }
    }

    public void reset() {
        manager.getCookieStore().removeAll();
        webView.getEngine().load(null);
        imageView.setImage(null);
        fileContainer.getChildren().clear();
        if (editToggleBtn.isSelected()) {
            editToggle();
        }
    }

    public void setUrl(String url) {
        new Thread(() -> {
            urlText.setText(url);
            webView.getEngine().load(url);
        }).run();
    }

    public void removeSequenceAction() {
        TextFormat tf = new TextFormat(textArea.getText());
        updateTextArea(tf.removeInstances(removeField.getText()));
        if (!Settings.Vars.hideDialogs.getBool()) {
            new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Remove text", "Removed " + tf.getCount() + " instance(s) of '" + removeField.getText() + "'.");
        }
        if (Settings.Vars.clearInput.getBool()) {
            removeField.setText("");
            removeField.requestFocus();
        }
    }

    public void replaceSequenceActionOld() {
        TextFormat tf = new TextFormat(textArea.getText());
        updateTextArea(tf.replaceSequence(oldTField.getText(), newTField.getText()));
        if (!Settings.Vars.hideDialogs.getBool()) {
            new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Replace text", "Replaced " + tf.getCount() + " instance(s) of '" + oldTField.getText() + "' with '" + newTField.getText() + "'.");
        }
        if (Settings.Vars.clearInput.getBool()) {
            oldTField.setText("");
            newTField.setText("");
            oldTField.requestFocus();
        }
    }

    public void replaceSequenceActionNew() {
        TextFormat tf = new TextFormat(textArea.getText());
        updateTextArea(tf.replaceSequence(oldTField.getText(), newTField.getText()));
        if (!Settings.Vars.hideDialogs.getBool()) {
            new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Replace text", "Replaced " + tf.getCount() + " instance(s) of '" + oldTField.getText() + "' with '" + newTField.getText() + "'.");
        }
        if (Settings.Vars.clearInput.getBool()) {
            oldTField.setText("");
            newTField.setText("");
            newTField.requestFocus();
        }
    }

    public void chopString() {
        int i;
        try {
            i = Integer.parseInt(newLenField.getText());
        } catch (NumberFormatException e) {
            i = textArea.getText().length();
        }
        TextFormat tf = new TextFormat(textArea.getText());
        updateTextArea(tf.chopString(i));
        if (!Settings.Vars.hideDialogs.getBool()) {
            new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Chop text", "Removed " + tf.getCount() + " character(s).");
        }
        if (Settings.Vars.clearInput.getBool()) {
            newLenField.setText("");
            newLenField.requestFocus();
        }
    }

    public void toUpper() {
        updateTextArea(textArea.getText().toUpperCase());
    }

    public void toLower() {
        updateTextArea(textArea.getText().toLowerCase());
    }

    public void addQuotes() {
        TextFormat tf = new TextFormat(textArea.getText());
        updateTextArea(tf.addDblQuotes());
    }

    public void applyTextSettings() {
        boolean temp = Settings.Vars.hideDialogs.getBool();
        boolean temp2 = Settings.Vars.clearInput.getBool();
        Settings.Vars.hideDialogs.setValue(true);
        Settings.Vars.clearInput.setValue(false);
        removeSequenceAction();
        replaceSequenceActionNew();
        chopString();
        Settings.Vars.hideDialogs.setValue(temp);
        Settings.Vars.clearInput.setValue(temp2);
    }

    void updateTextArea(String text) {
        if (textArea.getText().length() > 0) {
            textArea.setText(text);
            cListen.setEdit(true);
            cListen.setClipObject(textArea.getText());
        }
    }

    public void mainKeyReleased(KeyEvent keyEvent) {
        ic.removeKey(keyEvent.getCode());
    }

    public void mainKeyPressed(KeyEvent keyEvent) {
        ic.addKey(keyEvent.getCode());
        if (ic.isPressed(Settings.Keys.KEY_SAVE.getKey(), Settings.Keys.KEY_SAVE.getKeyMods())) {
            ic.releaseAll();
            saveFile();
        }
        if (ic.isPressed(Settings.Keys.KEY_NEW.getKey(), Settings.Keys.KEY_NEW.getKeyMods())) {
            menuController.newItem();
            ic.releaseAll();
        }
        if (ic.isPressed(Settings.Keys.KEY_TOOL.getKey(), Settings.Keys.KEY_TOOL.getKeyMods())) {
            toggleToolView();
        }
        if (ic.isPressed(Settings.Keys.KEY_HIDE.getKey(), Settings.Keys.KEY_HIDE.getKeyMods())) {
            ic.releaseAll();
            if (ClipboardPP.getStage().isShowing()) {
                ClipboardPP.getTrayHandler().hide(ClipboardPP.getStage());
            }
        }
        if (ic.isPressed(Settings.Keys.KEY_EDIT.getKey(), Settings.Keys.KEY_EDIT.getKeyMods())) {
            editToggle();
        }
        if (ic.isPressed(Settings.Keys.KEY_DELETE.getKey(), Settings.Keys.KEY_DELETE.getKeyMods())) {
            delPageDialog();
            ic.releaseAll();
        }
        KeyCode numPriority = ic.getHighestPriority(
                Settings.Keys.KEY_OBJ1, Settings.Keys.KEY_OBJ2,
                Settings.Keys.KEY_OBJ3, Settings.Keys.KEY_OBJ4,
                Settings.Keys.KEY_OBJ5, Settings.Keys.KEY_OBJ6,
                Settings.Keys.KEY_OBJ7, Settings.Keys.KEY_OBJ8,
                Settings.Keys.KEY_OBJ9, Settings.Keys.KEY_OBJ10,
                Settings.Keys.KEY_OBJL);
        if (numPriority != null) {
            if (numPriority.equals(Settings.Keys.KEY_OBJL.getKey())) {
                if (cListen.getQueue().size() - 1 != cListen.getQueueIndex())
                    setPage(cListen.getQueue().size() - 1);
            } else {
                int index = ic.keycodeToInt(numPriority);
                if (index == 0) {
                    index = 9;
                } else {
                    index--;
                }
                if (index != cListen.getQueueIndex())
                    setPage(index);
            }
        }
        KeyCode dirPriority = ic.getHighestPriority(Settings.Keys.KEY_OBJN, Settings.Keys.KEY_OBJP);
        if (dirPriority != null) {
            if (dirPriority.equals(Settings.Keys.KEY_OBJN.getKey())) {
                setPage(cListen.getQueueIndex() + 1);
            } else if (dirPriority.equals(Settings.Keys.KEY_OBJP.getKey())) {
                setPage(cListen.getQueueIndex() - 1);
            }
        }
        if (ic.isPressed(Settings.Keys.KEY_EXIT.getKey(), Settings.Keys.KEY_EXIT.getKeyMods())) {
            menuController.exit();
        }
    }

    public void toolKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            TextField t = (TextField) keyEvent.getSource();
            switch (t.getId()) {
                case "removeField":
                    removeSequenceAction();
                    break;
                case "newTField":
                    replaceSequenceActionNew();
                    break;
                case "oldTField":
                    replaceSequenceActionOld();
                    break;
                case "newLenField":
                    chopString();
                    break;
                case "rotationField":
                    setRotation(parseAngle(rotationField.getText()));
                    break;
                case "zoomField":
                    zoomImage(Integer.parseInt(zoomField.getText()));
                    break;
                case "widthField":
                case "heightField":
                    resizeImageAction();
                    break;
                case "fileNameTF":
                    renameFile();
                    break;
                case "filepathTF":
                    moveFileDialog();
                    break;
                case "namePatternTF":
                    applyNamingPattern();
                    break;
                default:
                    break;
            }
        }
    }

    public void resizeImageAction() {
        resizeImage(Double.parseDouble(widthField.getText()), Double.parseDouble(heightField.getText()));
    }

    public void cropImage(double x, double y) {
        newImage = new WritableImage(originalImage.getPixelReader(), (int) x, (int) y);
        imageView.setImage(newImage);
    }

    public void resizeImage(double x, double y) {
        imageView.setPreserveRatio(false);
        zoomImage(100);
        imageView.setFitHeight(y);
        imageView.setFitWidth(x);
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(javafx.scene.paint.Color.TRANSPARENT);
        cListen.setClipObject(imageView.snapshot(sp, null));
        imageView.setPreserveRatio(true);
    }

    public void openURL(ActionEvent event) {
        String link = "";
        Button b = (Button) event.getSource();
        if (b.getText().equals("Google URL Shortener")) {
            link = "https://goo.gl/";
        } else if (b.getText().equals("Google Translator")) {
            link = "https://translate.google.com/";
        } else if (b.getText().equals("Whois Lookup")) {
            link = "https://www.whois.net/";
        } else if (b.getText().equals("Down For Everyone")) {
            link = "http://downforeveryoneorjustme.com/";
        } else if (b.getText().equals("Scam Adviser")) {
            link = "http://www.scamadviser.com/";
        }
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public void applyFileList() {
        cListen.setEdit(true);
        cListen.setClipObject(fileList);
    }

    public void showFileProperties(File file) {
        deselectFileItem();
        fileIndex = fileList.indexOf(file);
        selectFileItem();
        fileNameTF.setText(file.getName());
        filepathTF.setText(file.getParent());
    }

    public void selectFileItem() {
        FileItem newFi = null;
        if (fileIndex > -1) {
            newFi = (FileItem) fileContainer.getChildren().get(fileIndex).getUserData();
        }
        if (newFi != null) {
            newFi.setStyle(c.FILE_SELECTED_STYLE);
        }
    }

    public void deselectFileItem() {
        FileItem oldFi = null;
        if (fileIndex > -1) {
            oldFi = (FileItem) fileContainer.getChildren().get(fileIndex).getUserData();
        }
        if (oldFi != null) {
            oldFi.setStyle(c.FILE_STYLE);
        }
        fileNameTF.setText("");
        filepathTF.setText("");
    }

    public void moveFileItem(FileItem target) {
        File file = fileList.get(fileIndex);
        int index = fileContainer.getChildren().indexOf(target.getGraphics());
        int delta = Math.abs(fileIndex - index);
        if (delta > 1) {
            if (target.vb.getTranslateX() < 0 && index < fileList.size() - 1) {
                index++;
                fileList.remove(file);
                fileList.add(index, file);
                fileIndex = index;
                addFirstToLast();
            } else {
                fileList.remove(file);
                fileList.add(index, file);
                fileIndex = index;
            }
        } else if (index > fileIndex) {
            if (isRight(target)) {
                fileList.remove(file);
                fileList.add(index, file);
                fileIndex = index;
            } else if (Settings.Vars.shiftAllFiles.getBool()) {
                addLastToFirst();
            }
        } else if (index < fileIndex) {
            if (isLeft(target)) {
                fileList.remove(file);
                fileList.add(index, file);
                fileIndex = index;
            } else if (Settings.Vars.shiftAllFiles.getBool()) {
                addLastToFirst();
            }
        }
        applyFileList();
    }

    public void addFirstToLast() {
        File file = fileList.get(0);
        fileList.remove(file);
        fileList.add(file);
        fileIndex--;
    }

    public void addLastToFirst() {
        File file = fileList.get(fileList.size() - 1);
        fileList.remove(file);
        fileList.add(0, file);
        fileIndex++;
    }

    public boolean isRight(FileItem target) {
        return target.vb.getTranslateX() <= 0;
    }

    public boolean isLeft(FileItem target) {
        return target.vb.getTranslateX() > 0;
    }

    public void removeFileItem(FileItem fileItem) {
        if (new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Confirm delete", "Remove this file from the clipboard?")
                .getResult().getText().equals("OK")) {
            fileList.remove(fileItem.file());
            fileContainer.getChildren().remove(fileItem.getGraphics());
            fileNameTF.setText("");
            filepathTF.setText("");
            itemCountLabel.setText(fileList.size() + " Item(s)");
            if (fileList.size() < 1) {
                delCurrentPage();
            } else {
                System.out.println("ITEM 1: " + fileList.get(0).toPath());
            }
            applyFileList();
        }
    }

    public void moveFileDialog() {
        if (fileIndex > -1) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select destination folder");
            if (filepathTF.getText() != "") {
                dirChooser.setInitialDirectory(new File(filepathTF.getText()));
            }
            File file = dirChooser.showDialog(ClipboardPP.getStage());
            if (file != null) {
                filepathTF.setText(file.getAbsolutePath());
            }
            applyFileList();
        }
    }

    public void applyNamingPattern() {
        String pattern = namePatternTF.getText();
        Matcher m = Pattern.compile("\\[([^]]+)\\]").matcher(pattern);
        String base = null;
        String incSys = null;
        if (m.find()) {
            base = m.group(1);
            if (m.find()) {
                incSys = m.group(1);
            }
        }
        if (base == null || incSys == null) {
            new DialogBuilder(Alert.AlertType.ERROR).showDialog("Error", "Incorrect input. The pattern should be in the format of [base text][start-end].\nFor instance, [frame][0-9] would result in the first item being frame0, the second being frame1 and so on.");
            return;
        } else if (!validateBase(base)) {
            new DialogBuilder(Alert.AlertType.ERROR).showDialog("Error", "Incorrect input for the base. The text in the brackets shouldn't contain any special characters");
            return;
        } else if (!validateIncSys(incSys)) {
            new DialogBuilder(Alert.AlertType.ERROR).showDialog("Error", "Incorrect input for the range. The text in the brackets must be in the format x-y, where y is a character that follows the character x. Examples may be 0-9, a-z, a-c, etc");
            return;
        }
        String[] c = incSys.split("-");
        char start = c[0].charAt(0);
        char end = c[1].charAt(0);
        for (int i = 0; i < fileList.size(); i++) {
            String name = fileList.get(i).getName();
            String ext = name.substring(name.lastIndexOf('.'));
            renameFile(base + indexToInc(i, start, end) + ext, i);
        }
        new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Alert", "Done");
    }

    public String indexToInc(int index, char start, char end) {
        String s = "";
        int delta = (int) end - (int) start + 1;
        while (index >= 0) {
            int remainder = index % delta;
            s = (char) (remainder + (int) start) + s;
            index = (index - remainder) / delta - 1;
        }
        return s;
    }

    //@@still bugged - will fix soon!
    public boolean validateBase(String base) {
        for (int i = 0; i < base.length(); i++) {
            if (Arrays.asList(c.ILLEGAL_CHARACTERS).contains(base.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean validateIncSys(String incSys) {
        if (!incSys.contains("-") || incSys.length() != 3 || incSys.charAt(2) <= incSys.charAt(0)) {
            return false;
        }
        return true;
    }

    public void renameFile() {
        if (fileIndex >= 0) {
            File file = fileList.get(fileIndex);
            FileItem fi = (FileItem) fileContainer.getChildren().get(fileIndex).getUserData();
            File newFile = new File(file.getParent() + "/" + fileNameTF.getText());
            fi.setFile(newFile);
            fileList.set(fileIndex, newFile);
            fi.fileName.setText(fileNameTF.getText());
            try {
                Files.move(file.toPath(), file.toPath().resolveSibling(fileNameTF.getText()));
                new DialogBuilder(Alert.AlertType.CONFIRMATION).showDialog("Alert", "Done");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void renameFile(String name, int index) {
        if (index >= 0) {
            File file = fileList.get(index);
            FileItem fi = (FileItem) fileContainer.getChildren().get(index).getUserData();
            File newFile = new File(file.getParent() + "/" + name);
            fi.setFile(newFile);
            fileList.set(index, newFile);
            fi.fileName.setText(name);
            try {
                Files.move(file.toPath(), file.toPath().resolveSibling(name));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
