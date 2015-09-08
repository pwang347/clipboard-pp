package zenas.util;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * Created by Paul on 8/31/2015.
 */
public class TextFormat {
    private String oldText;
    private int counter;

    public TextFormat(String oldText) {
        this.oldText = oldText;
    }

    public synchronized String removeInstances(String sequence) {
        counter = 0;
        String newText = oldText;
        if (sequence != null && !sequence.equals("")) {
            while (newText.contains(sequence) && newText != "") {
                newText = newText.substring(0, newText.indexOf(sequence)) + newText.substring(newText.indexOf(sequence) + sequence.length());
                counter++;
            }
        }
        return newText;
    }

    public synchronized String replaceSequence(String replacedSequence, String newSequence) {
        counter = 0;
        String newText = oldText;
        if (replacedSequence != null && newSequence != null && !replacedSequence.equals("")) {
            while (newText.contains(replacedSequence)) {
                int insertPos = newText.indexOf(replacedSequence);
                newText = newText.substring(0, insertPos) + newSequence + newText.substring(insertPos+replacedSequence.length());
                counter++;
           }
        }
        return newText;
    }

    public String chopString(int length) {
        if(length>=oldText.length()){
            counter = 0;
            return oldText;
        }
        counter = oldText.length()-length;
        return oldText.substring(0, length);
    }

    public String addDblQuotes() {
        if (!oldText.startsWith("\"") && !oldText.endsWith("\"")) {
            return "\"" + oldText + "\"";
        } else if(!oldText.endsWith("\"")) {
            return oldText.substring(1, oldText.length());
        } else if(!oldText.startsWith("\"")) {
            return oldText.substring(0, oldText.length() - 1);
        } else {
            return oldText.substring(1, oldText.length() - 1);
        }
    }

    public String addSnglQuotes() {
        if (!oldText.startsWith("\'") && !oldText.endsWith("\'")) {
            return "\'" + oldText + "\'";
        } else if(!oldText.endsWith("\'")) {
            return oldText.substring(1, oldText.length());
        } else if(!oldText.startsWith("\'")) {
            return oldText.substring(0, oldText.length() - 1);
        } else {
            return oldText.substring(1, oldText.length() - 1);
        }
    }

    public synchronized int getCount() {
        return counter;
    }

    public static ChangeListener<String> limit(int len) {
        return (event, oldValue, newValue) -> {
                    if (newValue.length() > len) {
                        ((StringProperty) event).setValue(oldValue);
                    }
        };
    }

    public static ChangeListener<String> numLimit(int len) {
        return (event, oldValue, newValue) -> {
            if(newValue.length()>0) {
                if (newValue.length() > len) {
                    ((StringProperty) event).setValue(oldValue);
                } else {
                    for (char c : newValue.toCharArray()) {
                        if (!Character.isDigit(c)) {
                            ((StringProperty) event).setValue(oldValue);
                            break;
                        }
                    }
                }
            }
        };
    }

}
