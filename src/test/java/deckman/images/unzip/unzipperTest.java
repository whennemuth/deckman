/*
 * unzipperTest.java
 * JUnit based test
 *
 * Created on January 27, 2008, 11:57 PM
 */

package deckman.images.unzip;

import junit.framework.*;
import java.util.*;
import java.util.zip.*;
import java.io.*;
import deckman.utils.codeLib;

/**
 *
 * @author Warren
 */
public class unzipperTest extends TestCase {
    
    public unzipperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of unzip method, of class deckman.images.unzip.unzipper.
     */
    public void testUnzip() {
        System.out.println("unzip");
        
        boolean bClearTargetDir = true;
        unzipper instance = null;
        
        result expResult = null;
        result result = instance.unzip(bClearTargetDir);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZipShortFileName method, of class deckman.images.unzip.unzipper.
     */
    public void testGetZipShortFileName() {
        System.out.println("getZipShortFileName");
        
        ZipEntry zEntry = null;
        String sOutDir = null;
        
        String expResult = "";
        String result = unzipper.getZipShortFileName(zEntry, sOutDir);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getZipToDirFileName method, of class deckman.images.unzip.unzipper.
     */
    public void testGetZipToDirFileName() {
        System.out.println("getZipToDirFileName");
        
        ZipEntry zEntry = null;
        String sMoveToDir = "";
        result ZipResult = null;
        
        String expResult = "";
        String result = unzipper.getZipToDirFileName(zEntry, sMoveToDir, ZipResult);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of main method, of class deckman.images.unzip.unzipper.
     */
    public void testMain() {
        System.out.println("main");
        
        String[] args = null;
        
        unzipper.main(args);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
