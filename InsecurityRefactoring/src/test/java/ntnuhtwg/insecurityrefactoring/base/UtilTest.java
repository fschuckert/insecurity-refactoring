/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.base;

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
public class UtilTest {
    
    public UtilTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
     * Test of isLastIndex method, of class Util.
     */
    @Test
    public void testIsLastIndex() {
    }

    /**
     * Test of joinStr method, of class Util.
     */
    @Test
    public void testJoinStr_StringArr_String() {
    }

    /**
     * Test of formatJSON method, of class Util.
     */
    @Test
    public void testFormatJSON() {
    }

    /**
     * Test of joinStr method, of class Util.
     */
    @Test
    public void testJoinStr_Collection_String() {
    }

    /**
     * Test of splitOnLines method, of class Util.
     */
    @Test
    public void testSplitOnLines() {
        String str = "This is a line;yeah\nAnotherLine\n;\nblubb";
        
        assertEquals(2, Util.splitOnLines(str, ";").size());
        
        assertEquals(2, Util.splitOnLines(str, "^(\\s)*;(\\s)*$").size());
        assertEquals(1, Util.splitOnLines(str, "this_split_is_not_found_in_the_string").size());
        
        
    }

    /**
     * Test of isType method, of class Util.
     */
    @Test
    public void testIsType() {
    }

    /**
     * Test of isAnyCall method, of class Util.
     */
    @Test
    public void testIsAnyCall() {
    }

    /**
     * Test of prettyPrint method, of class Util.
     */
    @Test
    public void testPrettyPrint() {
    }

    /**
     * Test of debugPrintLoc method, of class Util.
     */
    @Test
    public void testDebugPrintLoc() {
    }

    /**
     * Test of isPrePath method, of class Util.
     */
    @Test
    public void testIsPrePath() {
    }

    /**
     * Test of codeLocation method, of class Util.
     */
    @Test
    public void testCodeLocation() {
    }

    /**
     * Test of getOriginalFileContent method, of class Util.
     */
    @Test
    public void testGetOriginalFileContent() {
    }

    /**
     * Test of createTree method, of class Util.
     */
    @Test
    public void testCreateTree() throws Exception {
    }

    /**
     * Test of sha1FromFile method, of class Util.
     */
    @Test
    public void testSha1FromFile() {
    }

    /**
     * Test of getASTasJSONRec method, of class Util.
     */
    @Test
    public void testGetASTasJSONRec() throws Exception {
    }

    /**
     * Test of createMap method, of class Util.
     */
    @Test
    public void testCreateMap() {
    }

    /**
     * Test of readLineByLineJava8 method, of class Util.
     */
    @Test
    public void testReadLineByLineJava8() {
    }

    /**
     * Test of isAnyOf method, of class Util.
     */
    @Test
    public void testIsAnyOf() {
    }

    /**
     * Test of runCommandOnBashDANGEROES method, of class Util.
     */
    @Test
    public void testRunCommandOnBashDANGEROES() throws Exception {
    }

    /**
     * Test of runCommand method, of class Util.
     */
    @Test
    public void testRunCommand() throws Exception {
    }

    /**
     * Test of codeSnippet method, of class Util.
     */
    @Test
    public void testCodeSnippet() {
    }

    /**
     * Test of previewAST method, of class Util.
     */
    @Test
    public void testPreviewAST() throws Exception {
    }

    /**
     * Test of previewCode method, of class Util.
     */
    @Test
    public void testPreviewCode() throws Exception {
    }

    /**
     * Test of deleteFolder method, of class Util.
     */
    @Test
    public void testDeleteFolder() throws Exception {
    }
    
}
