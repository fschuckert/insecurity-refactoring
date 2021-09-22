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
import ntnuhtwg.insecurityrefactoring.base.Util;
import ntnuhtwg.insecurityrefactoring.base.tree.DFATreeNode;
import ntnuhtwg.insecurityrefactoring.refactor.temppattern.ScanProgress;
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
public class OrderIT {
    
    private Framework framework;
    
    @Before
    public void setUpClass() throws Exception{
        framework = new Framework();        
        framework.init();
    }

    @After
    public  void tearDownClass() throws Exception{
        framework.closeDB();
    }
    
    @Test
    public void testDotAssign() throws Exception{
        File pipFolder = new File("src/test/non-packaged-test-files/order/dot_assign");
        
        
        // dot assign
        framework.scan(pipFolder.getAbsolutePath(), true, new LinkedList<>(), new ScanProgress(), false, null);
        List<DFATreeNode> pips = framework.getPips(false, false);
        
        // only one pip
        Assert.assertEquals(1, pips.size());        
        DFATreeNode pip = pips.get(0);
        
        // search assign op
        DFATreeNode assignOP = pip.findFirstOfTypeDown(ASTNodeTypes.ASSIGN_OP);        
        Assert.assertNotNull("Didn't find assign op node", assignOP);
        
        DFATreeNode a = assignOP.getChildren_().get(0);
        DFATreeNode b = assignOP.getChildren_().get(1);
        
        Assert.assertEquals("a", framework.getDSL().getVariableName(a.getObj()));
        Assert.assertEquals("b", framework.getDSL().getVariableName(b.getObj()));   
    }
    
    @Test
    public void testConcat() throws Exception{
        File pipFolder = new File("src/test/non-packaged-test-files/order/concat");
        
        
        // dot assign
        framework.scan(pipFolder.getAbsolutePath(), true, new LinkedList<>(), new ScanProgress(), false, null);
        List<DFATreeNode> pips = framework.getPips(false, false);
        
        // only one pip
        Assert.assertEquals(1, pips.size());        
        DFATreeNode pip = pips.get(0);
        
        // search assign op
        DFATreeNode binaryOP = pip.findFirstOfTypeDown(ASTNodeTypes.BINARY_OP);        
        Assert.assertNotNull("Didn't find binaryOP node", binaryOP);
        
        DFATreeNode a = binaryOP.getChildren_().get(0);
        DFATreeNode b = binaryOP.getChildren_().get(1);
        
        Assert.assertEquals("a", framework.getDSL().getVariableName(a.getObj()));
        Assert.assertEquals("b", framework.getDSL().getVariableName(b.getObj()));   
    }
}
