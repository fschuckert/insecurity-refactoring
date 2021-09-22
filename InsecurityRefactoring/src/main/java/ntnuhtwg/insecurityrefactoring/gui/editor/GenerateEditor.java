/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import ntnuhtwg.insecurityrefactoring.base.JSONUtil;
import ntnuhtwg.insecurityrefactoring.base.SwingUtil;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.exception.NotExpected;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author blubbomat
 */
public class GenerateEditor extends JPanel implements GenerateInterface{

    JTextField key = new JTextField();
    RSyntaxTextArea edit = new RSyntaxTextArea("", 20, 60);
    private JComboBox<Type> type = new JComboBox<>(Type.values());
    private JButton deleteButton = new JButton("X");

    public GenerateEditor() {
        this.setLayout(new BorderLayout());
        this.add(SwingUtil.layoutBoxX(key, type, deleteButton), BorderLayout.NORTH);
        this.add(edit, BorderLayout.CENTER);        
    }

    void setKey(String key) {
        this.key.setText(key);
    }

    void setValue(Object value) {
        // it is either String[], String, boolean
        if(value instanceof JSONArray){
            JSONArray array = (JSONArray)value;            
            type.setSelectedItem(Type.STRING_ARRAY);
            edit.setText(Util.joinStr(array, "\n"));
        }
        if(value instanceof String){
            type.setSelectedItem(Type.STRING);
            edit.setText((String)value);
        }
        if(value instanceof Boolean){
            type.setSelectedItem(Type.BOOLEAN);
            edit.setText(String.valueOf(value));
        }
    }

    @Override
    public Object getJSON() {
        switch((Type)type.getSelectedItem()){
            case STRING_ARRAY:
                return JSONUtil.toJSONArray(edit.getText().split("\n"));
            case STRING:
                return edit.getText().replaceAll("\n", "");
            case BOOLEAN:
                return Boolean.valueOf(edit.getText());
            default:
                throw new NotExpected("Not implemented type" + type.getSelectedItem());
        }
    }

    @Override
    public String getKey() {
        return key.getText();
    }

    public void addDeleteActionListener(ActionListener l){
        deleteButton.addActionListener(l);
    }
    
    
    private enum Type
    {
        STRING_ARRAY,
        STRING,
        BOOLEAN
    }

}
