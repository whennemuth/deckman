/*
 * myReimageTest.java
 *
 * Created on September 20, 2008, 10:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package deckman.tests;

import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;


public class myReimageTest {
    
    public myReimageTest() {
    }

    public static void main(String[] args){
	
	boolean bWITH_TRANSPARENCY = true;
	String sFileIn = "c:\\myjpg.jpg";
	String sFileOut = null;

        File in = new File(sFileIn);
        BufferedImage imgIn = null;
        try{
            imgIn = ImageIO.read(in);
        }
        catch(java.io.IOException e){ }
	
	BufferedImage imgOut = null;
	Graphics gr = null;
	
	    // define height and width of image with 50 pixel border around it.
	int wImg = imgIn.getWidth();
	int hImg = imgIn.getHeight();
	    // define height and width of image inside the border.
	int wBuf = wImg+100;
	int hBuf = hImg+100;
	
	
	if(bWITH_TRANSPARENCY){
		// create a ColorModel based on an TYPE_BYTE_INDEXED buffered image
	    sFileOut = "c:\\mygif_grainy.gif";
	    BufferedImage ex = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_INDEXED);
	    IndexColorModel icm = (IndexColorModel) ex.getColorModel();	
	    int SIZE = 256;
	    byte[] r = new byte[SIZE];
	    byte[] g = new byte[SIZE];
	    byte[] b = new byte[SIZE];
	    byte[] a = new byte[SIZE];
	    icm.getReds(r);
	    icm.getGreens(g);
	    icm.getBlues(b);
	    java.util.Arrays.fill(a, (byte)255);
	    r[0] = g[0] = b[0] = a[0] = 0; //transparent
	    IndexColorModel cm = new IndexColorModel(8, SIZE, r, g, b, a);
	    
		// draw content on another buffered image that is byte indexed using the new ColorModel.
	    imgOut = new BufferedImage(wBuf, hBuf, BufferedImage.TYPE_BYTE_INDEXED, cm);
	    gr = imgOut.getGraphics();
	    gr.setColor(new Color(0,0,0,0));		
	    gr.fillRect(0, 0, wBuf, hBuf);
	    gr.setColor(Color.BLACK);
	    gr.drawRect(1, 1, wBuf-3, hBuf-3);
	    gr.drawImage(imgIn, 50, 50, wImg, hImg, null);
	    gr.dispose();	    
	}
	else{
		// draw content using the existing ColorModel.
		//It will have no alpha component as the image is based on a jpg, which has no transparency
	    //imgOut = new BufferedImage(wBuf, hBuf, imgIn.getType());
	    sFileOut = "c:\\mygif_normal.gif";
	    imgOut = new BufferedImage(wBuf, hBuf, imgIn.getType());
	    gr = imgOut.getGraphics();
	    gr.setColor(Color.RED);		
	    gr.fillRect(0, 0, wBuf, hBuf);
	    gr.setColor(Color.BLACK);
	    gr.drawRect(1, 1, wBuf-3, hBuf-3);
	    gr.drawImage(imgIn, 50, 50, wImg, hImg, null);
	    gr.dispose();	    
	}
	
	    // write out the image to a file.
	try{
	    ImageIO.write(imgOut, "gif", new File(sFileOut));	
	} catch (IOException ex) {
	    ex.printStackTrace();
	}	
    }
}
