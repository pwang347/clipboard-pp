package zenas;

import javafx.application.Platform;
import javafx.stage.Stage;
import zenas.fxml.main.FileItem;
import zenas.fxml.main.MainFXController;
import zenas.pref.Settings;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ClipboardLoop implements Runnable {
    private ClipboardListener cListen;
    private MainFXController controller;
    private Stage mainStage;

    enum DataType {
        String, Hyperlink, Image, FileList;
    }

    public ClipboardLoop(ClipboardListener cListen, MainFXController controller, Stage mainStage) {
        this.cListen = cListen;
        this.controller = controller;
        this.mainStage = mainStage;
    }

    public void run() {
        cListen.start();
        try {
            while (true) {
                Thread.sleep(Settings.Vars.refreshRate.getInt());
                //make sure that listener is not disabled
                if (!cListen.isDisabled()) {
                    //if changes have been made to the clipboard
                    if (cListen.isChanged()) {
                        cListen.setChanged(false);

                        if (cListen.getQueue().size() > 0) {
                            Platform.runLater(() -> {
                                //if the home page is open, close it and show the data preview page instead
                                if (!controller.dataPreviewBox.isVisible()) {
                                    controller.menuController.setHomePane(false);
                                }
                                controller.reset();
                            });
                            //display UI based on the data type of the clip object in the listener
                            cListen.setDataType();
                            try {
                                updateUI(DataType.valueOf(cListen.getDataType()));
                            } catch (IllegalArgumentException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            //otherwise show the default home page
                            Platform.runLater(() -> {
                                controller.menuController.setHomePane(true);
                            });
                        }
                    }
                    //if the clipobject is a new unique object
                    if (cListen.isNew()) {
                        cListen.setNew(false);
                        Platform.runLater(() -> {
                            controller.addPage();
                            if (Settings.Vars.autoApplyToolbox.getBool()) {
                                if (cListen.getDataType().equals("String")) {
                                    controller.applyTextSettings();
                                } else if (cListen.getDataType().equals("Image")) {
                                    controller.applyImage();
                                }
                            }
                        });
                    }
                    //if a clip object is in queue for display show a progress indicator, or hide a visible indicator otherwise
                    if (cListen.isQueued()) {
                        cListen.setQueued(false);
                        Platform.runLater(() -> {
                            controller.progressBox.setVisible(true);
                        });
                    } else if (controller.progressBox.isVisible()) {
                        Platform.runLater(() -> {
                            controller.progressBox.setVisible(false);
                        });
                    }

                    //if page number doesn't match with the clipboard queue number, do so
                    if (cListen.isPageUpdate()) {
                        Platform.runLater(() -> {
                            controller.setPage(cListen.getQueueIndex());
                            cListen.setPageUpdate(false);
                            controller.progressBox.setVisible(false);
                        });
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateUI(DataType type) {
        //bring the window to the front
        Platform.runLater(() -> {
            mainStage.toFront();
            controller.rootPane.requestFocus();
        });

        switch (type) {
            case String:
                String text = cListen.getClipboardText(cListen.getClipObject());
                //remove text, if text is empty and it is the only item in the clipboard queue
                if (text.equals("") && cListen.getQueue().size() == 1) {
                    cListen.removeFromQueue(cListen.getQueueIndex());
                    Platform.runLater(() -> {
                        controller.delCurrentPage();
                    });
                    cListen.setNew(false);
                    break;
                }
                //if the text is unique
                if (!controller.textArea.getText().equals(text)) {
                    Platform.runLater(() -> {
                        controller.textArea.setText(text);
                        controller.setPane(controller.textPane);
                        controller.setToolPane(controller.textTools);
                    });
                    //sleep to prevent error from instantaneously accessing clipboard
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //set clipboard contents to the text
                    StringSelection s = new StringSelection(controller.textArea.getText());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);
                } else {
                    Platform.runLater(() -> {
                        controller.setPane(controller.textPane);
                        controller.setToolPane(controller.textTools);
                    });
                }
                break;
            case Hyperlink:
                Platform.runLater(() -> {
                    String link = cListen.getClipboardText(cListen.getClipObject());
                    if (link.startsWith("www.")) {
                        link = "http://" + link;
                    }
                    controller.setUrl(link);
                    controller.setPane(controller.webPane);
                    controller.setToolPane(controller.webTools);
                });
                break;
            case Image:
                controller.setImage();
                Platform.runLater(() -> {
                    controller.textArea.setText("");
                    controller.setPane(controller.imagePane);
                    controller.setToolPane(controller.imageTools);
                    controller.imageView.requestFocus();
                });
                break;
            case FileList:
                Platform.runLater(() -> {
                    controller.fileList = cListen.getClipboardFileList(cListen.getClipObject());
                    //populate grid container with file items (icon + label)
                    for (int i = 0; i < controller.fileList.size(); i++) {
                        if ((controller.fileList.get(i).exists() && !controller.fileList.get(i).isDirectory())) {
                            FileItem fi = new FileItem(controller.fileList.get(i), controller);
                            controller.fileContainer.getChildren().addAll(fi.getGraphics());
                        } else {
                            controller.fileList.remove(controller.fileList.get(i));
                        }
                    }
                    if (controller.fileIndex > -1) {
                        controller.showFileProperties(controller.fileList.get(controller.fileIndex));
                    }
                    controller.itemCountLabel.setText(controller.fileContainer.getChildren().size() + " Item(s)");
                    controller.setPane(controller.filePane);
                    controller.setToolPane(controller.fileTools);
                });
                break;
            default:
                //if none of the above types, remove the item from queue
                cListen.removeFromQueue(cListen.getQueueIndex());
                Platform.runLater(() -> {
                    controller.delCurrentPage();
                });
                cListen.setNew(false);
                break;
        }
    }
}
