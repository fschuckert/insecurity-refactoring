/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base.patterns;

import java.util.Arrays;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.GlobalSettings;
import ntnuhtwg.insecurityrefactoring.base.db.neo4j.node.INode;
import ntnuhtwg.insecurityrefactoring.print.PrintAST;
import ntnuhtwg.insecurityrefactoring.base.tree.TreeNode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author blubbomat
 */
public class PatternTest {
    
    static PatternStorage patternStorage;
    
    public PatternTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception{
        patternStorage = new PatternStorage();
        patternStorage.readPatterns(GlobalSettings.patternFolder);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of containsAny method, of class Pattern.
     */
    @Test
    public void testCreateAST() {
        PatternImpl pattern = new PatternImpl();
        
        pattern.setCodeLines(Arrays.asList("<array>()"));
        
        List<TreeNode<INode>> ast = pattern.generateAst(null, patternStorage);
        new PrintAST().prettyPrint(ast.get(0));
    }



    public class PatternImpl extends Pattern {
    }
    
}
