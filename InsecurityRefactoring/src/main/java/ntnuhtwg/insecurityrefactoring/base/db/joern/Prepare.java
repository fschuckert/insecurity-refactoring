/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.db.joern;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.ScanProgress;
import static org.neo4j.cypher.internal.plandescription.Root.line;

/**
 *
 * @author blubbomat
 */
public class Prepare {
    
    
    
    
    public boolean prepareDatabase(String pathToScan, ScanProgress scanProgress) throws IOException, InterruptedException{
        
        String execute = "./analyse.sh " + pathToScan;
        
        String importCommand = "./import_neo4j.sh";
        
        File rootFolder = new File(GlobalSettings.rootFolder);

	System.out.println(rootFolder.getAbsolutePath());
        
        System.out.println(pathToScan);
        
        System.out.println("" + execute);
        Util.runCommand(execute, rootFolder);
        scanProgress.joernScanned();
        
        String content = Util.readLineByLineJava8(rootFolder.getAbsolutePath()+"/cpg_edges.csv");
        // checks if CPG creation worked
        if(!content.startsWith(":START_ID")){
            return false;
        }
        
        System.out.println("" + importCommand);
        Util.runCommand(importCommand, rootFolder);
        scanProgress.joernImported();
        
        return true;
    }
    
    public void startDB() throws IOException, InterruptedException{
        Util.runCommand("./start_neo4j.sh", new File(GlobalSettings.rootFolder));
    }
}
