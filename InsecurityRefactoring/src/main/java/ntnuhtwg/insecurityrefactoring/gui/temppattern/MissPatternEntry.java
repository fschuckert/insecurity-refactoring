/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.temppattern;

import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import ntnuhtwg.insecurityrefactoring.base.DataType;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.MissingCall;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.TempPattern;

/**
 *
 * @author blubbomat
 */
public class MissPatternEntry extends JPanel{
    
    private MissingCall call;
    private JCheckBox source = new JCheckBox("Source");
    private JTextField passthroughParameter = new JTextField();

    public MissPatternEntry(MissingCall call, int occurenceCount) {
        this.call = call;
        
        passthroughParameter.setMaximumSize(new Dimension(50, 20));
        
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        this.add(new JLabel("" + occurenceCount+ "  ") );
        this.add(new JLabel(call.toString()+ "                  "));
        
        this.add(source);
        this.add(new JLabel("Passthrough index:"));
        this.add(passthroughParameter);
    }
    
    
    public TempPattern getTempPattern(){
        if(source.isSelected()){
            return TempPattern.createSource(call, DataType.String());
        }
        
        String indexStr = passthroughParameter.getText();
        if("".equals(indexStr.trim())){
            return null;
        }
        try{
            Integer index = Integer.valueOf(indexStr);
            if(index >= 0 && index < call.getNumberOfParams()){
                return TempPattern.createPassthrough(call, DataType.String(), DataType.String(), index);
            }
        }
        catch(Exception ex){
//            ex.printStackTrace();
        }
        
        return null;
    }
}
