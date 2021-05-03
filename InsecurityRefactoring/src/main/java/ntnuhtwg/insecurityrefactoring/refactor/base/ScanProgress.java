/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.refactor.base;

import javax.swing.SwingWorker;
import ntnuhtwg.insecurityrefactoring.gui.ScanTask;

/**
 *
 * @author blubbomat
 */
public class ScanProgress {
    ScanTask guiTask;
    
    boolean joernScan = false;
    boolean joernImport = false;
    
    int nSinks = -1;
    int scannedSinks = 0;
    
    int nSinkTypes = -1;
    int sinksSearched = 0;

    
    public void setnSinks(int nSinks) {
        this.nSinks = nSinks;
    }

    public int getnSinkTypes() {
        return nSinkTypes;
    }

    public void setnSinkTypes(int nSinkTypes) {
        this.nSinkTypes = nSinkTypes;
    }

    public int getSinksSearched() {
        return sinksSearched;
    }

    public void setSinksSearched(int sinksSearched) {
        if(this.sinksSearched != sinksSearched){
            this.sinksSearched = sinksSearched;
            refresh();
        }
    }
    
    
    
    public void setSinkScanned(int scannedSinks){
        if(this.scannedSinks != scannedSinks){
            this.scannedSinks = scannedSinks;
            refresh();
        }
    }
    
    public void joernScanned(){
        joernScan = true;
        refresh();
    }
    
    public void joernImported(){
        joernImport = true;
        refresh();
    }

    public void setGuiTask(ScanTask guiTask) {
        this.guiTask = guiTask;
    }

    private void refresh() {
        // 500 -> joernScan
        // 500 -> joernImport
        // searched sinks -> 2000
        // Progress: 7000 -> for pips
        int progress = 0;
        if(joernScan){
            progress += 500;
        }
        if(joernImport){
            progress += 500;
        }
        
        if(sinksSearched > 0){
            float sinkSearchProgress = (float)sinksSearched / (float)nSinkTypes * 2000f;
            progress += (int)sinkSearchProgress;
        }
        
        if(scannedSinks > 0){
            float pipsProgress = (float)scannedSinks / (float)nSinks * 7000f;
            progress += (int)pipsProgress;
        }
        
        if(guiTask != null){
            guiTask.progressUpdate(progress/100);
        }
    }
    
    
    
}
