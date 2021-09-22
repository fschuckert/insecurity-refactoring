/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.editor;

import ntnuhtwg.insecurityrefactoring.gui.patterntester.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.context.VulnerabilityDescription;
import ntnuhtwg.insecurityrefactoring.base.exception.GenerateException;
import ntnuhtwg.insecurityrefactoring.base.exception.NotExpected;
import ntnuhtwg.insecurityrefactoring.base.patternpersist.JsonPatternStore;
import ntnuhtwg.insecurityrefactoring.base.patterns.Pattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.ContextPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.LanguagePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SanitizePattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SinkPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.SourcePattern;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.constructor.CodeSampleCreator;

/**
 *
 * @author blubbomat
 */
public class PatternEditor extends JPanel {

    JPanel west = new JPanel();

    JTextField patternsPath = new JTextField("src/main/non-packaged-resources/patterns");
//    JTextField patternsPath = new JTextField("target/files/patterns");
    JButton loadPatterns = new JButton("Reload patterns");

//    EditPattern editPattern = new EditPattern();
    JTabbedPane editFiles = new JTabbedPane();

    JComboBox<DataflowPattern> dataflowPatterns = new JComboBox<>();
    JComboBox<SourcePattern> sourcePatterns = new JComboBox<>();
    JComboBox<SinkPattern> sinkPatterns = new JComboBox<>();
    JComboBox<SanitizePattern> sanitizePatterns = new JComboBox<>();
    JComboBox<ContextPattern> contextPatterns = new JComboBox<>();

    JTextField samplePath = new JTextField("/home/blubbomat/Development/pattern_sample/");
    JButton sourceCodeOnly = new JButton("Source only");
    JButton createOnly = new JButton("Create Sample");
    JButton createSampleAndHost = new JButton("Host Sample");
    JButton stopDocker = new JButton("Stop");

    private DefaultListModel<LanguagePattern> languagePatternsModel = new DefaultListModel();
    JList<LanguagePattern> languagePatterns = new JList<>(languagePatternsModel);

    PatternStorage patternStorage;

    public PatternEditor() {
        this.setLayout(new BorderLayout());
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
        this.add(west, BorderLayout.WEST);

        patternsPath.setMaximumSize(new Dimension(2000, 20));
        west.add(patternsPath);
        west.add(loadPatterns);

//        editFiles.set
        west.add(new JLabel("Sources:"));
        addToPanelAndAddCopyAndEdit(sourcePatterns, west);

        west.add(new JLabel("Sanitization:"));
        addToPanelAndAddCopyAndEdit(sanitizePatterns, west);

        west.add(new JLabel("Dataflow:"));
        addToPanelAndAddCopyAndEdit(dataflowPatterns, west);

        west.add(new JLabel("Context"));
        addToPanelAndAddCopyAndEdit(contextPatterns, west);

        west.add(new JLabel("Sinks:"));
        addToPanelAndAddCopyAndEdit(sinkPatterns, west);

        west.add(samplePath);
        samplePath.setEditable(false);
        west.add(SwingUtil.layoutFlow(sourceCodeOnly, createOnly, createSampleAndHost, stopDocker));
        west.add(languagePatterns);

        this.add(editFiles);
        
        sourceCodeOnly.addActionListener((arg0) -> {
            String path = samplePath.getText();
            try {
                createSample(path, true);
            } catch (IOException ex) {
                Logger.getLogger(PatternEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        loadPatterns.addActionListener((arg0) -> {
            try {
                reloadPatterns(null);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Failed to load files: \n" + ex.getLocalizedMessage());
            }
        });

        createOnly.addActionListener((arg0) -> {
            String path = samplePath.getText();
            try {
                Util.deleteFolder(path);
                createSample(path, false);
            } catch (IOException ex) {
                Logger.getLogger(PatternEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        createSampleAndHost.addActionListener((arg0) -> {
            String path = samplePath.getText();
            try {
                Util.deleteFolder(path);
                createSample(path, false);
                hostSample(path);
            } catch (IOException ex) {
                Logger.getLogger(PatternEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

        });

        stopDocker.addActionListener((arg0) -> {
            String path = samplePath.getText();
            try {
                Util.runCommandOnBashDANGEROES("docker ps -a -q | xargs -n 1 -P 8 -I {} docker stop {}", new File(path));
            } catch (IOException ex) {
                Logger.getLogger(PatternEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(PatternEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public void createSample(String path, boolean sourceCodeOnly) throws HeadlessException, IOException {
        SourcePattern source = (SourcePattern) sourcePatterns.getSelectedItem();
        SanitizePattern sanitize = (SanitizePattern) sanitizePatterns.getSelectedItem();
        DataflowPattern dataflow = (DataflowPattern) dataflowPatterns.getSelectedItem();
        ContextPattern context = (ContextPattern) contextPatterns.getSelectedItem();
        SinkPattern sink = (SinkPattern) sinkPatterns.getSelectedItem();

        PatternStorage patternStorage = new PatternStorage();
        patternStorage.readPatterns(patternsPath.getText());
        CodeSampleCreator codeSampleCreator = new CodeSampleCreator(patternStorage);
        codeSampleCreator.setCreateGenerateFiles(true);
        codeSampleCreator.setOnlyCount(false);
        if(sourceCodeOnly){
            codeSampleCreator.setCreateManifest(false);
            codeSampleCreator.setCreateGenerateFiles(false);            
        }

        context = searchPatternByName(patternStorage.getContexts(), context);
        sink = searchPatternByName(patternStorage.getSinks(), sink);
        dataflow = searchPatternByName(patternStorage.getDataflows(), dataflow);
        source = searchPatternByName(patternStorage.getSources(), source);
        sanitize = searchPatternByName(patternStorage.getSanitizations(), sanitize);

        try {
            VulnerabilityDescription description = codeSampleCreator.createSample(context, sink, dataflow, source, sanitize, samplePath.getText()).get(0);
        } catch (GenerateException ex) {
            JOptionPane.showMessageDialog(null, "Cannot create sample: " + ex.getLocalizedMessage());
        }
    }

    public void hostSample(String path) throws HeadlessException {
        try {
            Util.runCommandOnBashDANGEROES("docker ps -a -q | xargs -n 1 -P 8 -I {} docker stop {}", new File(path));
            Util.runCommand("docker rm apache_php", new File(path));
            Util.runCommand("docker container prune --force", new File(path));
            Util.runCommand("docker-compose up --build -d", new File(path));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Failed to start docker: \n" + ex.getLocalizedMessage());
        }
    }

    private <E extends Pattern> E searchPatternByName(Collection<E> col, E pattern) {
        for (E updatedPattern : col) {
            if (updatedPattern.getName().equals(pattern.getName())) {
                return updatedPattern;
            }
        }
        return null;
    }

    private <E extends Pattern> void addToPanelAndAddCopyAndEdit(JComboBox<E> comboBox, JPanel toAdd) {
//        toAdd.add(comboBox);

        JButton copy = new JButton("COPY");
        JButton edit = new JButton("edit");
        comboBox.setMaximumSize(new Dimension(300, 20));
        copy.setMaximumSize(new Dimension(500, 20));
        edit.setMaximumSize(new Dimension(500, 20));

        toAdd.add(SwingUtil.layoutBoxX(comboBox, copy, edit));

        copy.addActionListener((arg0) -> {
            Pattern df = getSelectedBasePattern(comboBox);
            createPatternFileCopy(df, comboBox);
            openEditPattern(df, patternStorage);
//            editPattern.refreshPattern(df, patternStorage);
            selectPattern(df, comboBox);
        });

        edit.addActionListener((arg0) -> {
            Pattern df = getSelectedBasePattern(comboBox);
            openEditPattern(df, patternStorage);
        });

    }

    private <E extends Pattern> E getSelectedBasePattern(JComboBox<E> comboBox) {
        E df = (E) comboBox.getSelectedItem();
        if (df.isForGenerate()) {
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                E other = comboBox.getItemAt(i);
                if (other.getName().equals(df.getBasePattern())) {
                    return other;
                }
            }
            throw new NotExpected("Base pattern not found " + df.getBasePattern());
        } else {
            return df;
        }
    }

    private <E extends Pattern> void createPatternFileCopy(Pattern pattern, JComboBox<E> comboBox) throws HeadlessException {
        if (pattern == null) {
            JOptionPane.showMessageDialog(null, "No dataflow pattern selected!");
            return;
        }

        String newName = (String) JOptionPane.showInputDialog(
                null,
                "Select a new name",
                "New name",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                pattern.getName() + System.currentTimeMillis());

        if (newName == null) {
            return;
        }

        if (newName.contains("/") || newName.contains(";")) {
            JOptionPane.showMessageDialog(null, "Invalid character!");
        }

        pattern.setName(newName);
        String newPath = pattern.getPatternFileLocation().replaceFirst("[^/]*$", newName + ".json");
        pattern.setPatternFileLocation(newPath);

        try {
            JsonPatternStore.storePattern(pattern);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to save copy: \n" + ex.getStackTrace());
            return;
        }

        try {
            reloadPatterns(comboBox);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Failed to load files: \n" + ex.getStackTrace());
            return;
        }
    }

    private <E extends Pattern> void selectPattern(Pattern df, JComboBox<E> comboBox) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Pattern pattern = comboBox.getItemAt(i);
            if (pattern.getName().equals(df.getName())) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private <E extends Pattern> void reloadPatterns(JComboBox<E> comboBox) throws IOException {
        patternStorage = new PatternStorage();
        patternStorage.readPatterns(patternsPath.getText());

        if (comboBox == null || comboBox.equals(dataflowPatterns)) {
            addItemsSorted(dataflowPatterns, patternStorage.getDataflows());
        }
        if (comboBox == null || comboBox.equals(sourcePatterns)) {
            addItemsSorted(sourcePatterns, patternStorage.getSources());
        }
        if (comboBox == null || comboBox.equals(sinkPatterns)) {
            addItemsSorted(sinkPatterns, patternStorage.getSinks());
        }
        if (comboBox == null || comboBox.equals(sanitizePatterns)) {
            addItemsSorted(sanitizePatterns, patternStorage.getSanitizations());
        }

        if (comboBox == null || comboBox.equals(contextPatterns)) {
            addItemsSorted(contextPatterns, patternStorage.getContexts());
        }

        if (comboBox == null) {
            reloadLanguagePatterns();
        }

    }

    private void reloadLanguagePatterns() {
        languagePatternsModel.removeAllElements();
        List<LanguagePattern> languagePatterns = new LinkedList<>();
        languagePatterns.addAll(patternStorage.getLanguagePatterns());
        languagePatterns.sort((arg0, arg1) -> {
            return arg0.toString().compareTo(arg1.toString());
        });
        for (LanguagePattern languagePattern : languagePatterns) {
            languagePatternsModel.addElement(languagePattern);
        }
    }

    private <E extends Pattern> void addItemsSorted(JComboBox<E> comboBox, Collection<E> patterns) {
        comboBox.removeAllItems();
        List<E> patternsSorted = new LinkedList<>();
        for (E pattern : patterns) {
            if (pattern.getName().startsWith("list_call") || pattern.getName().startsWith("list_method")) {
                continue;
            }
            patternsSorted.add(pattern);
        }
        patternsSorted.sort((arg0, arg1) -> {
            return arg0.toString().compareTo(arg1.toString());
        });
        for (E pattern : patternsSorted) {
            comboBox.addItem(pattern);
        }
    }

    private void openEditPattern(Pattern df, PatternStorage patternStorage) {
        for (int i = 0; i < editFiles.getTabCount(); i++) {
            if (df.getName().equals(editFiles.getTitleAt(i))) {
                editFiles.setSelectedIndex(i);
                return;
            }
        }

        EditPattern editPattern = new EditPattern();
        editPattern.refreshPattern(df, patternStorage);
        editPattern.addCloseListener((arg0) -> {
            editFiles.remove(editPattern);
        });

        this.editFiles.add(df.getName(), editPattern);
        editFiles.setSelectedComponent(editPattern);
    }

}
