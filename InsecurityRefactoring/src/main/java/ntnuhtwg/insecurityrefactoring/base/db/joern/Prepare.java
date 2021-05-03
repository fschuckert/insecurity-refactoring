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
import ntnuhtwg.insecurityrefactoring.refactor.base.ScanProgress;
import static org.neo4j.cypher.internal.plandescription.Root.line;

/**
 *
 * @author blubbomat
 */
public class Prepare {
    
    
    
    
    public void prepareDatabase(String pathToScan, ScanProgress scanProgress) throws IOException, InterruptedException{
        
        String execute = "./analyse.sh " + pathToScan;
        
        String importCommand = "./import_neo4j.sh";
        
        File rootFolder = new File(GlobalSettings.rootFolder);

	System.out.println(rootFolder.getAbsolutePath());
        
        System.out.println(pathToScan);
        
        System.out.println("" + execute);
        Util.runCommand(execute, rootFolder);
        scanProgress.joernScanned();
        
        
        System.out.println("" + importCommand);
        Util.runCommand(importCommand, rootFolder);
        scanProgress.joernImported();
    }
}
