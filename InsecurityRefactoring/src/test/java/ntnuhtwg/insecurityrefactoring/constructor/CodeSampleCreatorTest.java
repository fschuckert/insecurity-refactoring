/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ntnuhtwg.insecurityrefactoring.constructor;

import java.util.LinkedList;
import java.util.List;
import ntnuhtwg.insecurityrefactoring.base.patterns.GenerateFile;
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
public class CodeSampleCreatorTest {
    
    public CodeSampleCreatorTest() {
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
     * Test of createAllPossibleSamples method, of class CodeSampleCreator.
     */
    @Test
    public void testCreateAllPossibleSamples() throws Exception {
    }

    /**
     * Test of mergedFiles method, of class CodeSampleCreator.
     */
    @Test
    public void testMergedFiles() {
        List<GenerateFile> file1 = new LinkedList<>();
        file1.add(new GenerateFile("foo/bar.txt", "inbetween"));
        
        List<GenerateFile> file2 = new LinkedList<>();
        file2.add(new GenerateFile("|pre|foo/bar.txt", "01"));
        
        List<GenerateFile> file3 = new LinkedList<>();
        file3.add(new GenerateFile("|aft|foo/bar.txt", "02"));
        
        List<GenerateFile> merged = new CodeSampleCreator(null).mergedFiles(file1, file2, file3);
        assertEquals(1, merged.size());
        assertEquals("01\ninbetween\n02", merged.get(0).getFileContent());
        assertEquals("foo/bar.txt", merged.get(0).getPath());
    }

    /**
     * Test of main method, of class CodeSampleCreator.
     */
    @Test
    public void testMain() throws Exception {
    }
    
}
