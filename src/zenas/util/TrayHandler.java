package zenas.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;

import javafx.application.Platform;
import javafx.stage.Stage;

import zenas.ClipboardPP;

public class TrayHandler {
    private TrayIcon trayIcon;

    public Image getOptimalIcon(SystemTray tray) {
        try {
            Dimension trayIconSize = tray.getTrayIconSize();
            if (trayIconSize.getWidth() > 16.0d) {
                return ImageIO.read(ClipboardPP.class.getResource("res/icon32.png"));
            } else {
                return ImageIO.read(ClipboardPP.class.getResource("res/icon16.png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createTrayIcon(final Stage stage) {
        Platform.setImplicitExit(false);
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = getOptimalIcon(tray);
            final ActionListener closeListener = e -> System.exit(0);
            ActionListener showHideListener = e -> Platform.runLater(() -> {
                if (stage.isShowing()) {
                    hide(stage);
                } else {
                    stage.show();
                }
            });
            ActionListener showListener = e -> Platform.runLater(() -> {
                stage.show();
            });
            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("Show/Hide Window");
            showItem.addActionListener(showHideListener);
            popup.add(showItem);

            MenuItem closeItem = new MenuItem("Exit");
            closeItem.addActionListener(closeListener);
            popup.add(closeItem);
            trayIcon = new TrayIcon(image, "Clipboard++", popup);
            trayIcon.addActionListener(showListener);
            try {
                tray.add(trayIcon);
                System.out.println("added");
            } catch (AWTException e) {
                System.err.println(e);
            }
        }
    }

    public void showProgramIsMinimizedMsg() {
        trayIcon.displayMessage("Alert",
                "Clipboard++ has been minimized to the system tray.",
                TrayIcon.MessageType.INFO);
    }

    public void showTrayMessage(TrayIcon.MessageType mt, String title, String message) {
        trayIcon.displayMessage(title, message, mt);
    }

    public void hide(final Stage stage) {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                showProgramIsMinimizedMsg();
                stage.hide();
            } else {
                Platform.exit();
                System.exit(0);
            }
        });
    }

}
