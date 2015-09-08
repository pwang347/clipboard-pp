package zenas.fxml.main;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import sun.awt.shell.ShellFolder;
import zenas.ClipboardPP;
import zenas.c;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.image.BufferedImage;
import java.beans.EventHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Paul on 8/26/2015.
 */
public class FileItem {
    File file;
    javafx.scene.image.Image icon;
    VBox vb;
    Pane pane;
    Label fileName;
    ImageView iconView;
    MainFXController controller;
    private java.util.Timer animTimer = new java.util.Timer();

    public FileItem(File file, MainFXController controller){
        this.file = file;
        this.controller = controller;
        this.icon = getFileIcon(file);
        pane = new StackPane();
        pane.setPrefSize(128,128);
        vb = new VBox(0);
        vb.setPrefSize(96,96);
        vb.setMaxSize(96,96);
        vb.setMinSize(96,96);
        vb.setAlignment(Pos.CENTER);
        fileName = new Label(file.getName());
        fileName.setPrefSize(69,59);
        fileName.setMinSize(69,59);
        fileName.setEllipsisString("...\n");
        fileName.setWrapText(true);
        iconView = new ImageView(icon);
        iconView.setSmooth(true);
        iconView.setFitHeight(32);
        iconView.setFitWidth(32);
        vb.setStyle(c.FILE_STYLE);
        vb.getChildren().addAll(iconView, fileName);
        vb.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            if(event.getClickCount()==2){
                controller.removeFileItem(this);
            }
        });
        vb.addEventFilter(MouseEvent.MOUSE_PRESSED, event1 ->{
            controller.showFileProperties(this.file);
            event1.consume();
        });
        pane.setOnDragDetected(event -> {
            Dragboard db = pane.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(String.valueOf(this));
            db.setContent(cc);
            SnapshotParameters sp = new SnapshotParameters();
            sp.setFill(Color.TRANSPARENT);
            Image img = vb.snapshot(sp, null);
            db.setDragView(img, 48, 48);
            vb.setOpacity(0.2);
            event.consume();
        });
        pane.setOnDragDone(event -> {
            vb.setOpacity(1);
        });

        pane.setOnDragEntered(event -> {
            if (event.getGestureSource() != pane) {
                if(event.getSceneX()-64>pane.getLayoutX()) {
                    displaceX(-32, vb);
                } else {
                    displaceX(32, vb);
                }
            }
            event.consume();
        });
        pane.setOnDragOver(event -> {
            if (event.getGestureSource() != pane) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            event.consume();
        });
        pane.setOnDragExited(event->{
            if (event.getGestureSource() != pane) {
                displaceX(0, vb);
            }
            event.consume();
        });

        pane.setOnDragDropped(event -> {
            controller.moveFileItem(this);
        });
        pane.setUserData(this);
        pane.getChildren().add(vb);
    }

    public void displaceX(double translateX, Node node) {
            animTimer.scheduleAtFixedRate(new TimerTask() {
                int i = 0;
                double deltaX = (translateX - node.getTranslateX()) / 5;

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        if (i < 7) {
                            if (i > 5) {
                                node.setTranslateX(node.getTranslateX() - (deltaX / 2));
                            } else if (deltaX != 0) {
                                node.setTranslateX(node.getTranslateX() + deltaX);
                            }
                        } else {
                                this.cancel();
                                node.setTranslateX((int) translateX);
                        }

                        i++;
                    });
                }

            }, 0, 15);
        }

    public void setStyle(String style){
        vb.setStyle(style);
    }

    public File file(){
        return file;
    }

    public void setFile(File file){
        this.file = file;
    }

    public javafx.scene.image.Image icon(){
        return icon();
    }

    public Pane getGraphics(){
        return pane;
    }

    private static javafx.scene.image.Image getFileIcon(File file) {
        ShellFolder sf = null;
        try {
            sf = ShellFolder.getShellFolder(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ImageIcon icon = new ImageIcon(sf.getIcon(true), sf.getFolderType());
        BufferedImage image = (BufferedImage) icon.getImage();
        Image img = SwingFXUtils.toFXImage(image, null);
        return img;
    }

}
