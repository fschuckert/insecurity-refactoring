/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;

import java.io.File;
import java.util.LinkedList;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.SourceLocation;
import ntnuhtwg.insecurityrefactoring.refactor.base.ScanProgress;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author blubbomat
 */
public class FindPipsIT {
    
    @Test
    public void testFindPip() throws Exception{
        File pipsFolder = new File("src/test/non-packaged-test-files/pips/");
        
        Framework framework = new Framework();        
        framework.init();
        
        
        for(File pipFolder : pipsFolder.listFiles()){
            if(pipFolder.isDirectory()){
                framework.scan(pipFolder.getAbsolutePath(), true, new LinkedList<>(), new ScanProgress(), false, null);
                if(framework.getPips(false, false).isEmpty()){
                    Assert.fail("Pip not found for: " + pipFolder.getAbsolutePath());
                }
            }
        }
        
        
        framework.close();
    }
    
    @Test
    public void testFindSpecificPip() throws Exception{
        File path = new File("src/test/non-packaged-test-files/pips/simple");
        SourceLocation specific = new SourceLocation("simple.php:6");
        
        Framework framework = new Framework();        
        framework.init();
        
        framework.scan(path.getAbsolutePath(), true, new LinkedList<>(), new ScanProgress(), true, specific);
        
        if(framework.getPips(false, false).isEmpty()){
            Assert.fail("Pip not found for specific: " );
        }
    }
}
