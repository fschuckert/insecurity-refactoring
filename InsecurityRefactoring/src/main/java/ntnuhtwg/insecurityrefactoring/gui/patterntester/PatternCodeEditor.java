/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.patterntester;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.base.JSONUtil;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.ast.ASTFactory;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.base.patterns.impl.DataflowPattern;
import ntnuhtwg.insecurityrefactoring.base.patterns.PatternStorage;
import ntnuhtwg.insecurityrefactoring.print.PrintAST;
import ntnuhtwg.insecurityrefactoring.gui.editor.GenerateInterface;
import org.json.simple.JSONArray;

/**
 *
 * @author blubbomat
 */
public class PatternCodeEditor extends JPanel implements GenerateInterface, KeyListener{

    PatternCodeViewer patternCodeViewer = new PatternCodeViewer();
    JButton generateCode = new JButton("Generate");
    JPanel south = SwingUtil.layoutBoxX();
    JButton save = new JButton("Save");
    JButton close = new JButton("Close");
    JButton formatCode = new JButton("Format");

    private static String indentSpace = "\t";

    PatternStorage patternStorage;
    

    public PatternCodeEditor(PatternStorage patternStorage, String editField) {

        this.patternStorage = patternStorage;
        this.setLayout(new BorderLayout());
//        this.add(west, BorderLayout.WEST);
        this.add(patternCodeViewer, BorderLayout.CENTER);
        this.add(south, BorderLayout.SOUTH);

        south.add(generateCode);
        south.add(formatCode);
        south.add(save);
        south.add(close);

        if (editField != null) {
            save.setName("Save " + editField);
            south.add(SwingUtil.layoutFlow(save, close), BorderLayout.SOUTH);
        }
        
        formatCode.addActionListener((arg0) -> {
            formatCode();
        });

        generateCode.addActionListener((arg0) -> {
            generateCodeFromPattern();

        });
        
        patternCodeViewer.addPatternKeyListeners(this);
    }

    public void generateCodeFromPattern() {
        PrintAST printAST = new PrintAST();
        try {
            TreeNode<INode> finalAST = Util.previewAST(patternStorage, getCodeLines());
            patternCodeViewer.setSourceCode(printAST.prettyPrint(finalAST));
            patternCodeViewer.updateAST(finalAST);
        }catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            patternCodeViewer.setSourceCode("Exception occured: \n" + sw.toString());
        }
    }

    public void formatCode() {
        setCode(getCodeLines());
    }

    public void setCode(List<String> codeLines) {
        String code = "";
        for (String line : codeLines) {
            code += formatLine(line);
            if(code.endsWith("\n")){
                code += ";\n";
            }
            else {
                code += "\n;\n";
            }
        }

        patternCodeViewer.setCode(code);
    }

    private static String formatLine(String line) {
        String retval = "";
        int indent = 0;

        for (int i = 0; i < line.length(); i++) {
            Character chr = line.charAt(i);

            switch (chr) {
                case '(': {
                    String subString = line.substring(i, line.length());
                    Pattern regexPattern = Pattern.compile("^\\([^\\<\\>\\)]*\\)[\\s,]*");
                    Matcher matcher = regexPattern.matcher(subString);
                    if (matcher.find()) {
                        int endIndex = matcher.end();
                        String toAppend = subString.substring(0, endIndex);
                        retval += toAppend + "\n" + indent(indent);
                        i += endIndex - 1;
                    } else {
                        retval += "\n" + indent(indent) + "(\n" + indent(++indent);
                    }
                    break;
                }
                case ',': {
                    retval += ",\n" + indent(indent);
                    break;
                }
                case ')': {
                    indent--;
                    retval = removeOneIndent(retval);
                    String subString = line.substring(i, line.length());
                    Pattern regexPattern = Pattern.compile("^\\)\\s*,");
                    Matcher matcher = regexPattern.matcher(subString);
                    if (matcher.find()) {
                        int endIndex = matcher.end();
                        String toAppend = subString.substring(0, endIndex);
                        retval += toAppend + "\n" + indent(indent);
                        i += endIndex -1;
                    } else {
                        retval += ")\n" + indent(indent);
                    }

                    break;
                }
                case ' ':
                    break;
                default:
                    retval += chr;
            }
        }

        return retval;
    }

    private static String removeOneIndent(String retval) {
        if (retval.endsWith(indentSpace)) {
            retval = retval.substring(0, retval.length() - indentSpace.length());
        }
        return retval;
    }

    private static String indent(int indent) {
        String retval = "";
        for (int idx = 0; idx < indent; idx++) {
            retval += indentSpace;
//            retval += "" + indent;
        }
        return retval;
    }

    public List<String> getCodeLines() {
        List<String> retval = new LinkedList<>();
        String unformated = patternCodeViewer.getPattern();

        List<String> lines = Util.splitOnLines(unformated,"^(\\s)*;(\\s)*$");
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            if(line.trim().equals(";")){
                continue;
            }
            line = line.replaceAll("\n", "");
            line = line.replaceAll(" ", "");
            line = line.replaceAll(indentSpace, "");
            if(line.isBlank()){
                continue;
            }
            while(line.trim().endsWith(";")){
                line = line.trim().substring(0, line.length()-1);
            }
            System.out.println("Adding: " + line);
            retval.add(line);
        }
        return retval;
    }

    public void addSaveListener(ActionListener actionListener) {
        save.addActionListener(actionListener);
    }
    
    public void addCloseListener(ActionListener actionListener) {
        close.addActionListener(actionListener);
    }
    

    public void setPatternStorage(PatternStorage patternStorage) {
        this.patternStorage = patternStorage;
        System.out.println("Set to : " + patternStorage);
    }

    public static void main(String[] args) {
        String code = "<def_func>(getParam, <param_list>(<param>(param), <param_default_bin>(encode, false)),  <stmtlist>(<=>(<$>(value), <dim>(<$>(_GET), <$>(param))) , <if>(<$>(encode), <stmtlist>(<return>(  <call>(htmlspecialchars, <call>(stripslashes, <$>(value)), <c>(ENT_QUOTES))  )) ), <return>(<$>(value)) ))";
//        String code = "<param_default_bin>(encode, false)";
        System.out.println("" + formatLine(code));

    }

    @Override
    public Object getJSON() {
        List<String> codeLines = getCodeLines();
        JSONArray array =JSONUtil.toJSONArray(codeLines);
        return array;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)){
            generateCodeFromPattern();
        }
        if(e.getKeyCode() == KeyEvent.VK_P && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)){
            formatCode();
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

}
