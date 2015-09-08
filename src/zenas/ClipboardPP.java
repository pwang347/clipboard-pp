/**
 * The Clipboard++ program handles clipboard activities
 * and supports text, image, hyperlink and file formats.
 *
 * @author Paul Wang
 * @version 1.0
 * @since 2015-09-7
 */

package zenas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import zenas.fxml.main.MainFXController;
import zenas.pref.Settings;
import zenas.util.TrayHandler;

public class ClipboardPP extends Application {

    private static Stage mainStage;
    private ClipboardListener cListen = new ClipboardListener();
    private MainFXController controller;
    private static TrayHandler trayHandler;

    /**
     *  loads main FXML file and controller, initializes main stage
     *  creates a handler for tray functions and begins new update thread
     */

    @Override
    public void start(Stage mainStage) throws Exception {
        Settings.load();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/main/main.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.init(cListen);
        mainStage.setTitle(c.TITLE_TEXT);
        mainStage.setScene(new Scene(root, c.WIDTH_COLLAPSED, c.HEIGHT));
        mainStage.setMinWidth(c.WIDTH_COLLAPSED);
        mainStage.setWidth(c.WIDTH_COLLAPSED);
        mainStage.getIcons().add(new javafx.scene.image.Image(String.valueOf(getClass().getResource("res/icon.png"))));
        mainStage.getIcons().add(new javafx.scene.image.Image(String.valueOf(getClass().getResource("res/icon64.png"))));
        mainStage.setAlwaysOnTop(true);
        mainStage.setResizable(false);
        mainStage.setOnCloseRequest((WindowEvent t) -> {
            //closes window visually before attempting to stop all threads
            mainStage.close();
            System.exit(0);
        });
        this.mainStage = mainStage;
        TrayHandler trayHandler = new TrayHandler();
        trayHandler.createTrayIcon(mainStage);
        this.trayHandler = trayHandler;
        controller.contentPane.requestFocus();
        mainStage.show();
        startBackgroundThread();
    }

    public void startBackgroundThread() {
        Thread updateThread = new Thread(new ClipboardLoop(cListen, controller, mainStage));
        updateThread.start();
    }

    public static Stage getStage() {
        return mainStage;
    }

    public static TrayHandler getTrayHandler() {
        return trayHandler;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
