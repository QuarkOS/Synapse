import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class ClipboardUtil {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    public void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text); // text as StringSelection
        clipboard.setContents(stringSelection, null);
    }

    public String getClipboardContent() {
        try {
            return (String) clipboard.getData(DataFlavor.stringFlavor); // get clipboard content as String
        } catch (Exception e) {
            e.printStackTrace();
            return null; // return null if an error occurs
        }
    }

}
