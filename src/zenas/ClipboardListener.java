package zenas;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import zenas.pref.Settings;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ClipboardListener extends Thread implements ClipboardOwner {

    private Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
    private Transferable clipObject;
    private LinkedList<Transferable> queue = new LinkedList<>();
    private int queueIndex = 0;
    private String dataType = "";

    private boolean notifyChanged = false;
    private boolean isEdit = false;
    private boolean notifyNew = false;
    private boolean notifyQueued = false;
    private boolean notifyPageUpdate = false;
    private boolean changingIndex = false;

    private int MAX_QUEUE_SIZE = Settings.Vars.maximumQueueSize.getInt();
    private boolean disabled = false;

    public void run() {
        try {
            //while queued, program shows progress indicator
            notifyQueued = true;
            //try grabbing clipboard data
            Transferable trans = sysClip.getContents(this);
            regainOwnership(trans);
        } catch (Exception e) {
            //if the data creates an error, remove it and retry
            e.printStackTrace();
            removeFromQueue(0);
            notifyQueued = false;
            sysClip.setContents(null, null);
            run();
        }
        try {
            //join the listener to the main thread
            this.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * When ownership of the clipboard object is lost by external means,
     * try to regain ownership unless disabled
     */

    public void lostOwnership(Clipboard c, Transferable t) {
        notifyQueued = false;
        do {
            try {
                this.sleep(10);
            } catch (Exception e) {
            }
        } while (disabled);
        notifyQueued = true;
        try {
            Transferable contents = sysClip.getContents(this);
            regainOwnership(contents);
        } catch (Exception e) {
            lostOwnership(c, t);
        }
    }

    void regainOwnership(Transferable t) {
        sysClip.setContents(t, this);
        clipObject = t;
        addToQueue(clipObject);
        notifyChanged = true;
        notifyQueued = false;
    }

    /**
     *  adds a transferable object to the clipboard queue
     */

    void addToQueue(Transferable t) {
        if (!isEdit || queue.size() < 1) {
            int index = isContained(queue, t);
            if (index == -1 && !changingIndex) {
                queue.addFirst(t);
                if (queue.size() > MAX_QUEUE_SIZE) {
                    queue.removeLast();
                } else {
                    setNew(true);
                }
            } else {
                changingIndex = false;
                if (queueIndex != index && getType(t).equals("String")) {
                    queueIndex = index;
                    setPageUpdate(true);
                }
            }
        } else {
            setEdit(false);
            replaceInQueue(t);
        }
    }

    void replaceInQueue(Transferable t) {
        queue.remove(queueIndex);
        queue.add(queueIndex, t);
    }

    public void removeFromQueue(int index) {
        if (!disabled) {
            if (index < queue.size() && index > -1) {
                if (queueIndex == index) {
                    if (index > 0) {
                        setClipObject(index - 1);
                    } else {
                        if (queue.size() == 1) {
                            clearClipboard();
                        } else {
                            setClipObject(index + 1);
                        }
                    }
                }
                queue.remove(index);
            }
            notifyChanged = true;
        }
    }

    void clearClipboard() {
        Transferable t = new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[0];
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return false;
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                return null;
            }
        };
        sysClip.setContents(t, this);
    }

    int isContained(LinkedList<Transferable> list, Transferable t) {
        //loops through each queue index to find the same object... may be memory taxing when queues are large and of the filelist type
        for (int i = 0; i < list.size(); i++) {
            if (isEqual(list.get(i), t)) {
                return i;
            }
        }
        return -1;
    }

    /**
     *  loads main FXML file and controller, initializes main stage
     *  creates a handler for tray functions and begins new update thread
     */

    boolean isEqual(Transferable t1, Transferable t2) {
        String type = getType(t1);
        if (type.equals(getType(t2))) {
            if (type.equals("String") || type.equals("Hyperlink")) {
                if (getClipboardText(t1).equals(getClipboardText(t2))) {
                    return true;
                }
            } else if(type.equals("FileList")){
                //filelists may be considered equal when they contain the same files and are the same size
                if(getClipboardFileList(t1).size()==getClipboardFileList(t2).size()&&
                        getClipboardFileList(t1).containsAll(getClipboardFileList(t2))){
                        return true;
                }
            }
        }
        return false;
    }

    //sets clip contents to object stored at a certain index in the clipboard queue
    public boolean setClipObject(int index) {
        if (index < queue.size() && index > -1 && !disabled) {
            try {
                notifyQueued = true;
                queueIndex = index;
                Transferable trans = queue.get(index);
                sysClip.setContents(trans, null);
                changingIndex = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }
    }

    //... set to new string
    public void setClipObject(String s) {
        System.out.println("2");
        notifyQueued = true;
        StringSelection ss = new StringSelection(s);
        try {
            sysClip.setContents(ss, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //... set to new image
    public void setClipObject(Image image) {
        java.awt.Image img = SwingFXUtils.fromFXImage(image, null);
        notifyQueued = true;
        ImageTransferable transferable = new ImageTransferable(img);
        try {
            sysClip.setContents(transferable, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //... set to new file list
    public void setClipObject(List<File> list) {
        FileListTransferable flt = new FileListTransferable(list);
        try {
            sysClip.setContents(flt, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //remove all elements after the given index in the clipboard queue
    public void chopQueue(int index) {
        if (index < queue.size() && index > -1) {
            queue.subList(index, queue.size()).clear();
        }
    }

    public LinkedList<Transferable> getQueue() {
        return queue;
    }

    //gets the type of a clip object
    public String getType(Transferable t) {
        String type = "null";
        try {
            if (t != null) {
                if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    type = "Image";
                }
                if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    type = "FileList";
                }
                if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String s = getClipboardText(clipObject);
                    if (s.startsWith("<img src=") && s.endsWith("/>")) {
                        type = "Image";
                        return type;
                    }
                    if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("www.") && s.length() > 12)
                        type = "Hyperlink";
                    else
                        type = "String";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //note that the position of the if conditions creates a hierarchy favoring the bottom types when multiple flavors are expected
        return type;
    }

    //gets the clipboard contents as a file list
    public java.util.List<File> getClipboardFileList(Transferable t) {
        java.util.List<File> fileList = new ArrayList<>();
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                fileList = new LinkedList<>((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return fileList;
    }

    //gets the clipboard contents as text
    public String getClipboardText(Transferable t) {
        String text = "";
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                text = (String) t.getTransferData(DataFlavor.stringFlavor);
                return text;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return text;
    }

    //gets the clipboard contents as an image
    public javafx.scene.image.Image getClipboardImage(Transferable t) {
        BufferedImage bf = null;
        WritableImage wr = null;
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                bf = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
                wr = null;
                if (bf != null) {
                    wr = new WritableImage(bf.getWidth(), bf.getHeight());
                    PixelWriter pw = wr.getPixelWriter();
                    for (int x = 0; x < bf.getWidth(); x++) {
                        for (int y = 0; y < bf.getHeight(); y++) {
                            pw.setArgb(x, y, bf.getRGB(x, y));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return wr;
    }

    static class ImageTransferable implements Transferable {
        private java.awt.Image image;

        public ImageTransferable(java.awt.Image image) {
            this.image = image;
        }

        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException {
            if (isDataFlavorSupported(flavor)) {
                return image;
            } else {
                throw new UnsupportedFlavorException(flavor);
            }
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor == DataFlavor.imageFlavor;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.imageFlavor};
        }
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType() {
        dataType = getType(getClipObject());
    }

    public int getQueueIndex() {
        return queueIndex;
    }

    public boolean isChanged() {
        return notifyChanged;
    }

    public void setChanged(boolean b) {
        notifyChanged = b;
    }

    public boolean isQueued() {
        return notifyQueued;
    }

    public void setQueued(boolean b) {
        notifyQueued = b;
    }

    public boolean isNew() {
        return notifyNew;
    }

    public void setNew(boolean b) {
        notifyNew = b;
    }

    public void setEdit(boolean b) {
        isEdit = b;
    }

    public boolean isPageUpdate() {
        return notifyPageUpdate;
    }

    public void setPageUpdate(boolean b) {
        notifyPageUpdate = b;
    }

    public Transferable getClipObject() {
        return clipObject;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void updateMaxQueueSize() {
        MAX_QUEUE_SIZE = Settings.Vars.maximumQueueSize.getInt();
    }
}