/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.JsonPatternStore;
import ntnuhtwg.insecurityrefactoring.base.patterns.GenerateFile;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.GenerateParameters;
import ntnuhtwg.insecurityrefactoring.gui.patterntester.PatternCodeEditor;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextArea;
import scala.reflect.internal.util.NoFile;

/**
 *
 * @author blubbomat
 */
public class EditPattern extends JPanel {

    private List<ActionListener> beforeSaveListeners = new LinkedList<>();

    Pattern pattern;

    JPanel generateFilesPanel = SwingUtil.layoutBoxY();
    JPanel paramsPanel = SwingUtil.layoutBoxY();

    JButton writeToDisk = new JButton("Write to disk");
    JButton close = new JButton("Close");
    private JPanel content = SwingUtil.layoutBoxY();

    public EditPattern() {
        this.setLayout(new BorderLayout());
        JScrollPane scrollable = new JScrollPane(content);
        scrollable.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollable, BorderLayout.CENTER);

        this.add(SwingUtil.layoutFlow(writeToDisk, close), BorderLayout.SOUTH);

        writeToDisk.addActionListener((arg0) -> {
            try {
                triggerBeforeSaveListeners();
                JsonPatternStore.storePattern(pattern);
                JOptionPane.showMessageDialog(null, "Sucessful saved pattern: " + pattern.getPatternFileLocation());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Failed to save pattern: \n" + ex.getStackTrace());
            }
        });

    }

    public void refreshPattern(Pattern pattern, PatternStorage patternStorage) {
        this.pattern = pattern;
        content.removeAll();
        beforeSaveListeners.clear();

        if (pattern == null) {
            return;
        }

        basicPattern(pattern, patternStorage);

        this.revalidate();
        this.repaint();
    }

    private void basicPattern(Pattern pattern, PatternStorage patternStorage) {
        content.add(new JLabel("Name: " + pattern.getName()));
        content.add(new JLabel("Path: " + pattern.getPatternFileLocation()));
        content.add(new JLabel("Input: " + pattern.getInputType()));
        content.add(new JLabel("Output: " + pattern.getOutputType()));
        content.add(new JLabel("Outputreturn: " + pattern.isReturnOutput()));

        content.add(new JLabel("Data(out)" + pattern.getDataOutputType()));
        content.add(new JLabel("Data(in)" + pattern.getDataInputType()));

//        content.add(new JLabel("Pattern Code"));
        addCode(content, pattern.getCodeLines(), patternStorage, "Code");

//        content.add(new JLabel("Init code"));
        addCode(content, pattern.getInitCodeLines(), patternStorage, "Init");

        content.add(paramsPanel);
        addParams(pattern, patternStorage);

        content.add(generateFilesPanel);
        refreshGenerateFiles();

    }

    private void addCode(JPanel toAdd, List<String> patternLines, PatternStorage patternStorage, String name) {

        // preview panel
        JTextArea code = new JTextArea(Util.joinStr(patternLines, "\n"));
        code.setEditable(false);
        code.setMaximumSize(new Dimension(200, 200));
        JSeparator l;
        JButton editCode = new JButton("Edit");
        JPanel codeViewPanel = SwingUtil.layoutBoxY(
                new JSeparator(),
                SwingUtil.layoutBoxX(new JLabel(name), editCode),
                code,
                new JSeparator()
        );
        toAdd.add(codeViewPanel);

        // editor panel
        PatternCodeEditor codeEditor = new PatternCodeEditor(patternStorage, "code");
        toAdd.add(codeEditor);
        codeEditor.setVisible(false);

        // action listeners
        editCode.addActionListener((arg0) -> {
            codeEditor.setCode(patternLines);
            codeEditor.setVisible(true);
            codeViewPanel.setVisible(false);
        });
        codeEditor.addSaveListener((arg0) -> {
            toggleCodeEditToView(codeEditor, codeViewPanel, patternLines, code);

        });
        codeEditor.addCloseListener((arg0) -> {
            codeEditor.setVisible(false);
            codeViewPanel.setVisible(true);
        });

        addTriggerBeforeSaveListener((arg0) -> {
            toggleCodeEditToView(codeEditor, codeViewPanel, patternLines, code);
        });

    }

    public void toggleCodeEditToView(PatternCodeEditor codeEditor, JPanel codeViewPanel, List<String> patternLines, JTextArea code) {
        if (codeEditor.isVisible()) {
            codeEditor.setVisible(false);
            codeViewPanel.setVisible(true);
            List<String> newCode = codeEditor.getCodeLines();
            patternLines.clear();
            patternLines.addAll(newCode);
            code.setText(Util.joinStr(patternLines, "\n"));
        }
    }

    public void addCloseListener(ActionListener listener) {
        close.addActionListener(listener);
    }

    private String codePreviewStr(PatternStorage patternStorage, List<String> codeLines) {
        String codePreviewStr = "";
        try {
            codePreviewStr = Util.previewCode(patternStorage, codeLines);
        } catch (Exception ex) {
            codePreviewStr = "exp " + ex.getLocalizedMessage();
        }
        return codePreviewStr;
    }

    private void refreshGenerateFiles() {
        List<GenerateFile> generateFiles = pattern.getGenerateFiles();
        generateFilesPanel.removeAll();

        JButton addNewFile = new JButton("Add new generate file");

        generateFilesPanel.add(new JLabel("<h1>Generate Files</h1>"));
        for (GenerateFile genFile : generateFiles) {
            addGenerateFile(generateFilesPanel, genFile);
        }

        generateFilesPanel.add(addNewFile);

        addNewFile.addActionListener((arg0) -> {
            GenerateFile genFile = new GenerateFile("Filename", "Content");
            generateFiles.add(genFile);
            refreshGenerateFiles();
        });
    }

    private void addGenerateFile(JPanel content, GenerateFile genFile) {
        JTextField fileName = new JTextField(genFile.getPath());
        JButton delete = new JButton("Delete");
        content.add(SwingUtil.layoutFlow(fileName, delete));
        RSyntaxTextArea genFileContent = new RSyntaxTextArea(genFile.getFileContent());
        content.add(genFileContent);
        addTriggerBeforeSaveListener((arg0) -> {
            genFile.setPath(fileName.getText());
            genFile.setFileContent(genFileContent.getText());
        });

        delete.addActionListener((arg0) -> {
            int dialogResult = JOptionPane.showConfirmDialog(null, "Delete " + genFile.getPath() + " ?", "Delete confirmation", JOptionPane.YES_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                 pattern.getGenerateFiles().remove(genFile);
                 refreshGenerateFiles();
            }
           
        });
    }

    private void addParams(Pattern pattern, PatternStorage patternStorage) {
        paramsPanel.removeAll();
        for (GenerateParameters params : pattern.getGeneratesParams()) {
            addParam(params, patternStorage);
        }
        JButton addParams = new JButton("Add new generate params");
        paramsPanel.add(addParams);
        
        addParams.addActionListener((arg0) -> {
            GenerateParameters newParams = new GenerateParameters(new LinkedList<>());
            pattern.getGeneratesParams().add(newParams);
            refreshPattern(pattern, patternStorage);
        });
    }

    private void addParam(GenerateParameters params, PatternStorage patternStorage) {
        JButton edit = new JButton("edit");
        paramsPanel.add(SwingUtil.layoutBoxX(edit, new JLabel("Params"), new JLabel(Util.joinStr(params.getParameters(), "\n"))));
        for (Entry<String, Object> entry : params.getToReplace().entrySet()) {
            paramsPanel.add(SwingUtil.layoutBoxX(new JLabel(entry.getKey()), new JLabel(entry.toString())));
        }

        edit.addActionListener((arg0) -> {
            EditGenerate editGenerate = new EditGenerate(params, patternStorage);
            JDialog dialog = SwingUtil.createDialog(this, editGenerate);

            editGenerate.addOkListener((arg1) -> {
                editGenerate.updateGenerateParams();
                dialog.dispose();
            });
        });

    }

    private void triggerBeforeSaveListeners() {
        for (ActionListener listener : beforeSaveListeners) {
            listener.actionPerformed(null);
        }
    }

    public void addTriggerBeforeSaveListener(ActionListener l) {
        this.beforeSaveListeners.add(l);
    }

}
