/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.dockable;

import bibliothek.gui.DockFrontend;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.DefaultDockable;
import bibliothek.gui.dock.FlapDockStation;
import bibliothek.gui.dock.SplitDockStation;
import bibliothek.gui.dock.StackDockStation;
import javax.swing.JFrame;

import bibliothek.gui.dock.common.*;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.menu.SingleCDockableListMenuPiece;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.facile.menu.RootMenuPiece;
import bibliothek.gui.dock.station.split.SplitDockGrid;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring.InsecurityRefactoring;
import ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring.RefactoringRenderer;
import ntnuhtwg.insecurityrefactoring.gui.astrenderer.ASTRenderer;
import ntnuhtwg.insecurityrefactoring.gui.editor.PatternEditor;
import ntnuhtwg.insecurityrefactoring.gui.patterntester.PatternCodeEditor;
import ntnuhtwg.insecurityrefactoring.gui.patterntester.PatternTesterPanel;

/**
 *
 * @author blubbomat
 */
public class GuiDocking {

    public void init(Framework framework) {
        // init frame
        JFrame frame = new JFrame("Insecurity Refactoring");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(20, 20, 400, 400);
        frame.setVisible(true);

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
//            UIManager.put("control", new Color(128, 128, 128));
//            UIManager.put("info", new Color(128, 128, 128));
//            UIManager.put("nimbusBase", new Color(18, 30, 49));
//            UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
//            UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
//            UIManager.put("nimbusFocus", new Color(115, 164, 209));
//            UIManager.put("nimbusGreen", new Color(176, 179, 50));
//            UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
//            UIManager.put("nimbusLightBackground", new Color(18, 30, 49));
//            UIManager.put("nimbusOrange", new Color(191, 98, 4));
//            UIManager.put("nimbusRed", new Color(169, 46, 34));
//            UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
//            UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
//            UIManager.put("text", new Color(230, 230, 230));
            SwingUtilities.updateComponentTreeUI(frame);
        } catch (UnsupportedLookAndFeelException exc) {
            System.err.println("Nimbus: Unsupported Look and feel!");
        }

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                framework.stop();
            }
        });

        // init docking frames
        CControl control = new CControl(frame);
        control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
//        control.setTheme(ThemeMap.KEY_BASIC_THEME);

        /* Finally we start with the real work: setting up the stations. The stations
		 * are the anchor points for the Dockables. To make things easier we use the
		 * default "CContentArea" which is offered by the CControl. It does not require
		 * any additional setup other than putting it on the main-frame. */
        frame.add(control.getContentArea());

//        CWorkingArea refactoringArea = control.createWorkingArea("refactoring");
//        CWorkingArea editPatternsArea = control.createWorkingArea("Edit patterns");
        InsecurityRefactoring pIPRenderer = new InsecurityRefactoring(framework, frame);
//        SingleCDockable pipRenderer = createDockable("PIP finder", Color.RED, pIPRenderer);
//        SingleCDockable refactoring =  createDockable("Refactoring", Color.GREEN, refactoringRender);
//        SingleCDockable editPatterns = createDockable("Edit patterns dockable", Color.GREEN, new PatternEditor());    

//        control.addDockable(editPatterns);           
//        editPatterns.setLocation(editPatternsArea.getStationLocation());
//        editPatterns.setWorkingArea(editPatternsArea);
//        editPatterns.setVisible(true); 
//        control.addDockable(pipRenderer);        
//        pipRenderer.setLocation(refactoringArea.getStationLocation());
//        pipRenderer.setWorkingArea(refactoringArea);        
//        pipRenderer.setVisible(true);  
//        control.addDockable(refactoring);        
//        refactoring.setLocation(refactoringArea.getStationLocation());        
//        refactoring.setWorkingArea(refactoringArea);
//        refactoring.setVisible(true);
        CGrid grid = new CGrid(control);
        grid.add(0, 0, 1, 2, createDockable("PIP finder", Color.WHITE, pIPRenderer));
        grid.add(1, 0, 1, 1, createDockable("Edit patterns", Color.GREEN, new PatternEditor()));
        grid.add(1, 1, 1, 1, createDockable("AST", Color.GREEN, new ASTRenderer(framework)));
        grid.add(1, 1, 1, 1, createDockable("Code Tester", Color.GREEN, new PatternTesterPanel(framework.getPatternStorage(), "")));

        control.getContentArea().deploy(grid);

//        CWorkingArea workingArea = control.createWorkingArea("work");
//        
//        CGrid cgrid = new CGrid(control);
//        cgrid.add(0.0, 0.0, 1.0, 2.0, createDockable("Edit patterns", Color.GREEN, new PatternEditor()));
//        
//        workingArea.deploy(cgrid);
//        
//        Dockable editPattern = createDockable("Edit patterns", Color.GREEN, new PatternEditor());
//        workingArea.add(editPattern);
    }
//    
//    private SplitDockStation insecurityRefactoring(Framework framework, JFrame frame){
//        SplitDockStation station = new SplitDockStation();
//        RefactoringRenderer refactoringRender = new RefactoringRenderer(framework);
//        PIPRenderer pIPRenderer = new PIPRenderer(framework, refactoringRender, frame);
//        SplitDockGrid grid = new SplitDockGrid();
//        grid.addDockable(0, 0, 1, 1, createDockable("PIP finder", Color.RED, pIPRenderer));
//        grid.addDockable(0, 1, 1, 1, createDockable("Refactoring", Color.GREEN, refactoringRender));
//        station.dropTree(grid.toTree());
//        
//        return station;
//    }
//    
//    private SplitDockStation editPatterns(Framework framework, JFrame frame){
//        SplitDockStation station = new SplitDockStation();
//        SplitDockGrid grid = new SplitDockGrid();        
//        grid.addDockable(0, 0, 1, 2, createDockable("Edit patterns", Color.GREEN, new PatternEditor()));
//        grid.addDockable(1, 0, 1, 2, createDockable("AST", Color.RED, new ASTRenderer(framework.getDb())));
//        station.dropTree(grid.toTree());
//        
//        return station;
//    }

    public SingleCDockable createDockable(String title, Color color, JPanel panel) {
        DefaultSingleCDockable dockable = new DefaultSingleCDockable(title, title);
        dockable.setCloseable(false);
        dockable.setMaximizable(true);
        dockable.setExternalizable(true);
        dockable.setMaximizable(true);
        dockable.add(panel);

        return dockable;
    }
}
