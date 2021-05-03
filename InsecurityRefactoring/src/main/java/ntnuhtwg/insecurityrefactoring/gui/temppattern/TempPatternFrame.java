/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.temppattern;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import ntnuhtwg.insecurityrefactoring.refactor.base.MissingCall;
import ntnuhtwg.insecurityrefactoring.refactor.base.TempPattern;

/**
 *
 * @author blubbomat
 */
public class TempPatternFrame extends JDialog{

    private JPanel  mainPanel = new JPanel();
    private List<MissPatternEntry> entries = new LinkedList<>();

    public TempPatternFrame(Frame owner) {
        super(owner);
        setModal(true);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        this.add(panel);
        JScrollPane scollPane = new JScrollPane(mainPanel);
        
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        this.setSize(800, 800);
        panel.add(scollPane, BorderLayout.CENTER);
    }
    
    
    public void refresh(Map<MissingCall, Integer> missingCalls){
        mainPanel.removeAll();
        entries.clear();
        
        // sort the calls by occurrance
        Map<MissingCall, Integer> sorted = missingCalls.entrySet()
                .stream()
                .sorted(Map.Entry.<MissingCall, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        for(Entry<MissingCall, Integer> entry : sorted.entrySet()){
            MissPatternEntry missPatternEntry = new MissPatternEntry(entry.getKey(), entry.getValue());
            mainPanel.add(missPatternEntry);
            entries.add(missPatternEntry);
        }
    }
    
    
    public List<TempPattern> getTempPatterns(){
        List<TempPattern> retval = new LinkedList<>();
        
        for(MissPatternEntry entry : entries){
            TempPattern tempPattern = entry.getTempPattern();
            if(tempPattern != null){
                retval.add(tempPattern);
            }
        }
        
        return retval;
    }
    
    
}
