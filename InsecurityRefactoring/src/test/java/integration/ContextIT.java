/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.Framework;
import ntnuhtwg.insecurityrefactoring.base.ASTNodeTypes;
import ntnuhtwg.insecurityrefactoring.base.info.ContextInfo;
import ntnuhtwg.insecurityrefactoring.base.info.PipInformation;
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.refactor.base.ScanProgress;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author blubbomat
 */
public class ContextIT {
    
    private Framework framework;
    
    @Before
    public void setUpClass() throws Exception{
        framework = new Framework();        
        framework.init();
    }

    @After
    public  void tearDownClass() throws Exception{
        framework.close();
    }
    
    @Test
    public void testApostroph() throws Exception{
        File pipFolder = new File("src/test/non-packaged-test-files/context/apostroph");
        
        
        // dot assign
        framework.scan(pipFolder.getAbsolutePath(), true, new LinkedList<>(), new ScanProgress(), false, null);      
        
        List<PipInformation> pipInformations = framework.getPipInformation();        
        Assert.assertEquals(1, pipInformations.size());
        
        
        PipInformation pipInformation = pipInformations.get(0);
        
        Assert.assertEquals(1, pipInformation.getVulnerableSources().size());
        
        DFATreeNode source = pipInformation.getVulnerableSources().get(0);
        
        framework.analyze(pipInformation, source);
        
        ContextInfo context = pipInformation.getContextInfo();
        Assert.assertNotNull("not context info", context);
        
        System.out.println("pre: " + context.getPre());
        System.out.println("pos: " + context.getPost());
        
//        Assert.assertTrue("Is apostrophe context", context.isApostrophe());
        
        
        
    }
    
    @Test
    public void testPaper() throws Exception{
        File pipFolder = new File("src/test/non-packaged-test-files/context/paper");
        
        
        // dot assign
        framework.scan(pipFolder.getAbsolutePath(), true, new LinkedList<>(), new ScanProgress(), false, null);      
        
        List<PipInformation> pipInformations = framework.getPipInformation();        
        Assert.assertEquals(1, pipInformations.size());
        
        
        PipInformation pipInformation = pipInformations.get(0);
        
        Assert.assertEquals(1, pipInformation.getPossibleSources().size());
        
        DFATreeNode source = pipInformation.getPossibleSources().get(0).getSource();
        
        framework.analyze(pipInformation, source);
        
        ContextInfo context = pipInformation.getContextInfo();
        Assert.assertNotNull("not context info", context);
        
        System.out.println("pre: " + context.getPre());
        System.out.println("pos: " + context.getPost());
        
        Assert.assertEquals("<a href=\"www.url.com/news", context.getPre());
        Assert.assertEquals("\">", context.getPost());
        
        
        
    }
    
    @Test
    public void testComplex() throws Exception{
        File pipFolder = new File("src/test/non-packaged-test-files/context/complex");
        
        
        // dot assign
        framework.scan(pipFolder.getAbsolutePath(), true, new LinkedList<>(), new ScanProgress(), false, null);      
        
        List<PipInformation> pipInformations = framework.getPipInformation();        
        Assert.assertEquals(1, pipInformations.size());
        
        
        PipInformation pipInformation = pipInformations.get(0);
        
        Assert.assertEquals(1, pipInformation.getPossibleSources().size());
        
        DFATreeNode source = pipInformation.getPossibleSources().get(0).getSource();
        
        framework.analyze(pipInformation, source);
        
        ContextInfo context = pipInformation.getContextInfo();
        Assert.assertNotNull("not context info", context);
        
        System.out.println("pre: " + context.getPre());
        System.out.println("pos: " + context.getPost());
        
        Assert.assertEquals("<a href=\"www.url.com/Newsbe", context.getPre());
        Assert.assertEquals("Blubblalalulublubb\">", context.getPost());
        
        
        
    }


}
