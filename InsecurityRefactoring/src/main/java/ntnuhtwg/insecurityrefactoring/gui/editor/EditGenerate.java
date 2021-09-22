/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import ntnuhtwg.insecurityrefactoring.base.JSONUtil;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.GenerateParameters;
import ntnuhtwg.insecurityrefactoring.gui.patterntester.PatternCodeEditor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author blubbomat
 */
public class EditGenerate extends JPanel {

    private Map<String, GenerateInterface> edits = new HashMap<>();
    private GenerateParameters params;
    PatternCodeEditor paramsEditor;
    private PatternStorage patternStorage;

    private static final Set<String> patternCodes = new HashSet<>(
            Arrays.asList(
                    "init",
                    "generate_output_code"
            ));

    JPanel content = SwingUtil.layoutBoxY();

    JButton ok = new JButton("ok");
    JButton cancel = new JButton("cancel");

    public EditGenerate(GenerateParameters params, PatternStorage patternStorage) {
        this.params = params;
        this.patternStorage = patternStorage;

        this.setLayout(new BorderLayout());
        this.add(content, BorderLayout.CENTER);
        paramsEditor = new PatternCodeEditor(patternStorage, "");
        paramsEditor.setCode(params.getParameters());
        paramsEditor.generateCodeFromPattern();
        updateAttributes();
    }

    private void updateAttributes() {
        content.removeAll();
        content.add(new JLabel("Params"));
        content.add(paramsEditor);
        edits.clear();

        for (Entry<String, Object> entry : params.getToReplace().entrySet()) {
            JButton removeAttribute = new JButton("X");
            removeAttribute.addActionListener((arg0) -> {
                if (JOptionPane.showConfirmDialog(null, "Delete " + entry.getKey() + " ?", "Delete confirmation", JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION) {
                    removeParam(entry.getKey());
                    updateAttributes();
                }
            });

            if (patternCodes.contains(entry.getKey())) {
                content.add(SwingUtil.layoutFlow(
                        new JLabel(entry.getKey()),
                        removeAttribute
                ));
                JSONArray jsonArray = (JSONArray) entry.getValue();
                PatternCodeEditor codeEditor = new PatternCodeEditor(patternStorage, null);
                codeEditor.setCode(jsonArray);
                codeEditor.generateCodeFromPattern();
                edits.put(entry.getKey(), codeEditor);
                content.add(codeEditor);

                continue;
            }

            GenerateEditor generateEditor = new GenerateEditor();
            generateEditor.setKey(entry.getKey());
            generateEditor.setValue(entry.getValue());
            generateEditor.addDeleteActionListener((arg0) -> {
                if (JOptionPane.showConfirmDialog(null, "Delete " + entry.getKey() + " ?", "Delete confirmation", JOptionPane.YES_OPTION) == JOptionPane.YES_OPTION) {
                    removeParam(entry.getKey());
                    updateAttributes();
                }
            });
            content.add(generateEditor);
            edits.put(entry.getKey(), generateEditor);
        }

        JButton addAttribute = new JButton("Add new attribute");

        content.add(addAttribute);
        addAttribute.addActionListener((arg0) -> {
            params.addToReplace("newAttribute", "Value");
            updateAttributes();
        });

        content.add(SwingUtil.layoutBoxX(ok, cancel));
        
        content.revalidate();
        content.repaint();
    }
    
    private void removeParam(String param){
        params.getToReplace().remove(param);
        edits.remove(param);
    }

    public void updateGenerateParams() {
        // params itself
        params.getParameters().clear();
        params.getParameters().addAll(paramsEditor.getCodeLines());

        // replace attributes
        params.getToReplace().clear();
        for (Entry<String, GenerateInterface> edited : edits.entrySet()) {
            String editedKey = edited.getValue().getKey();
            String key = editedKey != null ? editedKey : edited.getKey();
            Object value = edited.getValue().getJSON();
            params.getToReplace().put(key, value);
        }
    }

    public void addOkListener(ActionListener l) {
        ok.addActionListener(l);
    }

    public void addCancelListener(ActionListener l) {
        cancel.addActionListener(l);
    }

}
