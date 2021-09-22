/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.gui.editor.EditGenerate;

/**
 *
 * @author blubbomat
 */
public class SwingUtil {

    public static JPanel layoutBorder() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        return panel;
    }

    public static JPanel layoutFlow(Component... elements) {
        JPanel layout = new JPanel();
//        layout.setLayout(new BoxLayout(layout, BoxLayout.X_AXIS));
        layout.setLayout(new FlowLayout());

        for (Component element : elements) {
            layout.add(element);
        }

        return layout;
    }

    public static JPanel layoutBoxY(Component... elements) {
        JPanel layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));

        for (Component element : elements) {
            layout.add(element);
        }

        return layout;
    }

    public static JPanel layoutBoxX(Component... elements) {
        JPanel layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.X_AXIS));

        for (Component element : elements) {
            layout.add(element);
        }

        return layout;
    }

    public static void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    public static JDialog createDialog(JPanel from, JPanel content) {
        JFrame frame = (JFrame) SwingUtilities.windowForComponent(from);     

        JDialog dialog = new JDialog(frame);
        dialog.setSize(1200, 1000);
        dialog.add(content);
        dialog.setVisible(true);
        
        return dialog;
    }
}
