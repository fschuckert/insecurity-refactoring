/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring;

import ntnuhtwg.insecurityrefactoring.gui.insecurityrefactoring.PIPRenderer;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.ScanProgress;

/**
 *
 * @author blubbomat
 */
public class ScanTask extends SwingWorker<Object, Float> {

    final String scanPath;
    final String scanSpecific;
    final Framework framework;
    final boolean skipPreScan;
    final PIPRenderer pIPRenderer;
    final boolean controlFuncCheck;
    final SourceLocation specificLocation;

    public ScanTask(Framework framework, String scanPath, String scanSpecific, boolean skipPreScan, PIPRenderer pIPRenderer, boolean controlFuncCheck, SourceLocation specificLocation) {
        this.scanPath = scanPath;
        this.scanSpecific = scanSpecific;
        this.framework = framework;
        this.skipPreScan = skipPreScan;
        this.pIPRenderer = pIPRenderer;
        this.controlFuncCheck = controlFuncCheck;
        this.specificLocation = specificLocation;
    }

    @Override
    protected Object doInBackground() throws Exception {
        File folderToScan = new File(scanPath);
        ScanProgress scanProgress = new ScanProgress();
        scanProgress.setGuiTask(this);
        if (folderToScan.exists()) {

            List<String> specificPatterns = new LinkedList<>();
            if (scanSpecific != null) {
                specificPatterns.add(scanSpecific);
            }

            framework.scan(folderToScan.getAbsolutePath(), !skipPreScan, specificPatterns, scanProgress, controlFuncCheck, specificLocation);
        } else {
            JOptionPane.showMessageDialog(null, "Path is invalid!");
        }

        return null;
    }
    
    @Override 
    protected void done(){
        pIPRenderer.finishedScan();
    }

    public void progressUpdate(int progress) {
        setProgress(progress);
    }

}
