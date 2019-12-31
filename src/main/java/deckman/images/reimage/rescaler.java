
package deckman.images.reimage;

import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.awt.geom.*;
import java.io.*;
import deckman.utils.codeLib;
import deckman.images.imageFileFilter;

    /**
     * This class is used to create a rescaled or "shrunken" copy of a source image file for use as a thumbnail picture.
     * There is only one public method: {@link #generateThumb(boolean) generateThumb}. From there 4 different methods are 
     * possible for rescaling the source image (detailed in {@link #generateThumb(boolean) generateThumb}). The one used
     * depends on the {@link #RescaleMethod RescaleMethod} passed to the constructor. If a constructor is used that does
     * not include the {@link #RescaleMethod RescaleMethod} parameter, the SHRINK_CONVOLVE method is used by default
     * because it is found to be easily the best combination of speed and quality.
     */
public class rescaler {
    
	/** The full pathname of the source image used to create a rescaled image (thumbnail). */
    private String sImgInPathName = null;
	/** The directory location the rescaled image (thumbnail) will be written out to. */
    private String sImgOutDir = null;
	/** The name (without path info) of the rescaled image (thumbnail). */
    private String sImgOutShortName = null;
	/** The fullname - including path info - of the rescaled image (thumbnail). */
    private String sImgOutPathName = null;
	/** The type of image rescaling will produce. Indicates a file extension (gif, jpg, bmp, etc.) */
    private String sTargetImgType = null;
	/** Indicates what steps are involved in rescaling the image (see {@link #generateThumb(boolean) generateThumb} for details) */
    private RescaleMethod method;
	/** The new size in pixels the larger of the two dimensions of source Image (sImgInPathName) will be resized to */
    private int iThumbSize;
	/** Indicates whether or not to "frame" the image against a square background */
    private boolean bSquareFramed;  
    

        /** Enumeration for the 4 methods of recaling a source image details at {@link #generateThumb(boolean) generateThumb} */
    public static enum RescaleMethod{ MULTI_CONVOLVE, SHRINK_CONVOLVE, NO_CONVOLVE, SCALE_INSTANCE };
    
	/** Sharpening filter kernel. May find a use if this class is broadened to "sharpen" images */
    public static final float[] SHARPEN3x3 = { 
        0.f, -1.f,  0.f,
       -1.f,  5.f, -1.f,
        0.f, -1.f,  0.f
    };    

	/** Blurring filter kernel used in the convolve operation in function {@link #writeDrawnThumb(BufferedImage, int, int) writeDrawnThumb} */    
    public static final float[] BLUR3X3 = {
        1/9f, 1/9f, 1/9f, 
        1/9f, 1/9f, 1/9f, 
        1/9f, 1/9f, 1/9f
    };
    
    
        // constructors
    public rescaler(String sImgInPathName, String sImgOutDir, String sImgOutShortName, imageFileFilter.ImageType TargetImgType, RescaleMethod method, int iThumbSize, boolean bSquareFramed){
        this.sImgInPathName = sImgInPathName;
        this.sImgOutDir = sImgOutDir;
        this.sTargetImgType = getImageTypeAsString(TargetImgType);
	this.sImgOutShortName = sImgOutShortName;
	setThumbDetails();
        this.method = method;
        this.iThumbSize = iThumbSize;
        this.bSquareFramed = bSquareFramed;
    }

    public rescaler(File imgIn, String sImgOutDir, imageFileFilter.ImageType TargetImgType, RescaleMethod method, int iThumbSize, boolean bSquareFramed){
        this(imgIn.getPath(), sImgOutDir, imgIn.getName(), TargetImgType, method, iThumbSize, bSquareFramed);
    }

    public rescaler(File imgIn, String sImgOutDir){
        this(imgIn.getPath(), sImgOutDir, imgIn.getName(), imageFileFilter.ImageType.GIF, RescaleMethod.SHRINK_CONVOLVE, 100, true);
    }

    public rescaler(String sImgInPathName, String sImgOutDir){
        this(sImgInPathName, sImgOutDir, codeLib.removePathFromFileSpec(sImgInPathName), imageFileFilter.ImageType.GIF, RescaleMethod.SHRINK_CONVOLVE, 100, true);
    }
    
    
	/**
	 * It may be necessary to reassign the target thumbnail filename specified by the {@link #sImgOutShortName sImgOutShortName}
	 * constructor parameter to a unique name that does not conflict with any existing files in the target directory.
	 * This and the full path name of the thumbnail file is set here.
	 */
    private void setThumbDetails(){
	String sBaseName = codeLib.removeFileExtension(this.sImgOutShortName);
	String sName = sBaseName + "." + this.sTargetImgType;
	sName = codeLib.getUniqueFileName(sName, this.sImgOutDir); 
	this.sImgOutShortName = sName;
	String sPathName = null;
	if(this.sImgOutDir.endsWith(File.separator)){
	    sPathName = this.sImgOutDir + sName;
	}
	else{
	    sPathName = this.sImgOutDir + File.separator + sName;
	}
	
	this.sImgOutPathName = sPathName;
    }
    
    
	/**
	 * An image file is read from a file and buffered into memory. The data from the image file undergoes simple
	 * encoding while it is read. However, the buffered image has not been obtained as the resulting ouput from any
	 * graphics operation and so is "undrawn", which means that the BufferedImage does not contain anything that is
	 * organized against a "device space" as would be the case when using the java.awt.Graphics.drawImage() method 
	 * which reflects the use of a coordinate plane, or bounded workspace, in the output BufferedImage data. That
	 * is a theory (understanding of java image and graphics concepts is poorly understood at this point), but it
	 * serves to explain why buffered image operations, like convolving, produce a java.awt.image.ImagingOpException
	 * when performed against an "undrawn" image.
	 * @return BufferedImage, the image data endoded and buffered into memory.
	 */
    private BufferedImage getUndrawnImage(){
        File in = new File(this.sImgInPathName);
        BufferedImage img = null;
        if(!in.exists()) return null;
        try{
            img = ImageIO.read(in);
        }
        catch(java.io.IOException e){ }
        
        return img;
    }

    
	/**
	 * Obtains a buffered image that has been "drawn" using the java.awt.Graphics.drawImage() method. For an 
	 * explanation of the difference between a "drawn" and "undrawn" image see {@link #getUndrawnImage(String) getUndrawnImage}
	 * @param imgIn BufferedImage, the "undrawn" source image data
	 * @param iWidth int, the target width of the image in the returned BufferedImage (doesn't have to
	 * reflect the same ratio with iHeight as found in the source image (imgIn).
	 * @param iHeight int, the target height of the image in the returned BufferedImage (doesn't have to
	 * reflect the same ratio with iWidth as found in the source image (imgIn).
	 * @param iImgType int, The type of image return in the BufferedImage object
	 * @return BufferedImage, The "drawn" image data in a BufferedImage object.
	 */
    private BufferedImage getDrawnImage(BufferedImage imgIn, int iWidth, int iHeight, int iImgType){
        BufferedImage imgOut = new BufferedImage(iWidth, iHeight, iImgType);
        Graphics g = imgOut.getGraphics();
        g.drawImage(imgIn, 0, 0, null);
        g.dispose();
        return imgOut;
    }
    
    
	/**
	 * Accomplishes the same thing as {@link #getDrawnImage(BufferedImage, int, int, int) getDrawnImage(BufferedImage, int, int, int)}
	 * except the height, width and image type parameters are missing in favor of deriving them from the image being drawn. Obviously,
	 * no size or type differences will exist between the source and drawn images 
	 * @return BufferedImage, The "drawn" image data in a BufferedImage object.
	 */
    private BufferedImage getDrawnImage(){
        BufferedImage img = getUndrawnImage();
        img = getDrawnImage(img, img.getType());
        return img;
    }    
     
    
	/**
	 * Accomplishes the same thing as {@link #getDrawnImage(BufferedImage, int, int, int) getDrawnImage(BufferedImage, int, int, int)}
	 * except the height and width parameters are missing in favor of deriving them from the image being drawn. Obviously,
	 * no size difference will exist between the source and drawn images 
	 * @param imgIn BufferedImage, the "undrawn" source image data
	 * @param iImgType int, The type of image return in the BufferedImage object
	 * @return BufferedImage, The "drawn" buffered image data
	 */   
    private BufferedImage getDrawnImage(BufferedImage imgIn, int iImgType){
        return getDrawnImage(imgIn, imgIn.getWidth(), imgIn.getHeight(), iImgType);
    }    
    
	/**
	 * Accomplishes the same thing as {@link #getDrawnImage(BufferedImage, int, int, int) getDrawnImage(BufferedImage, int, int, int)}
	 * except the height and width parameters are missing in favor of deriving them from the image being drawn. Obviously,
	 * no size difference will exist between the source and drawn images 
	 * @return BufferedImage, The "drawn" buffered image data
	 */       
    private BufferedImage getDrawnThumbImage(){
        BufferedImage imgIn = getUndrawnImage();
        if(imgIn == null){
            return null;
        }
        else{
            return getDrawnThumbImage(imgIn, imgIn.getType(), this.iThumbSize, this.bSquareFramed);
        }    
    }
    
    
	/**
	 * Accomplishes the same thing as {@link #getDrawnImage(BufferedImage, int, int, int) getDrawnImage(BufferedImage, int, int, int)}
	 * except the height and width parameters are missing in favor of deriving them from the image being drawn. Obviously,
	 * no size difference will exist between the source and drawn images 
	 * @param iImgType int, The type of image return in the BufferedImage object
	 * @param iMaxDim int, The new size in pixels the larger of the two dimensions of source Image will be resized to
	 * @param bSquareFramed boolean, indicates whether or not to "frame" the image against a square background.
	 * @return BufferedImage, The "drawn" buffered image data
	 */       
    private BufferedImage getDrawnThumbImage(int iImgType, int iMaxDim, boolean bSquareFramed){
        BufferedImage imgIn = getUndrawnImage();
        if(imgIn == null){
            return null;
        }
        else{
            return getDrawnThumbImage(imgIn, iImgType, iMaxDim, bSquareFramed);
        }
    }
    
	/**
	 * This function performs a similar function as {@link #getDrawnImage(BufferedImage, int, int, int) 
	 * getDrawnImage(BufferedImage, int, int, int)}, but with the following additional specifics:<p>
	 * - The "drawn" image is kept to the same proportions as the source image (imgIn) - that is, the width and height
	 * are kept in the same ratio<br>
	 * - The "drawn" image can optionally be "framed" against a square border, where the largest dimension of the image
	 * spans the entirety of the square in the corresponding direction, and in the other direction, the image is centered.
	 * This will produce an image whose "picture" component centers horizontally against a white background if it is "tall" 
	 * or centers vertically if it is "squat". This can make laying out multiple images on an HTML page simpler because it
	 * comes down to putting down a grid of squares (an HTML table may not even be necessary).
	 * @param imgIn BufferedImage, the "undrawn" source image data
	 * @param iImgType int, The type of image return in the BufferedImage object
	 * @param iMaxDim int, The new size in pixels the larger of the two dimensions of source Image will be resized to
	 * @param bSquareFramed boolean, indicates whether or not to "frame" the image against a square background.
	 * @return BufferedImage, The "drawn" buffered image data
	 */
    private BufferedImage getDrawnThumbImage(Image imgIn, int iImgType, int iMaxDim, boolean bSquareFramed){
        Graphics g = null;
        BufferedImage imgOut = null;
        float fMaxDim = (float)iMaxDim;
        int W; int H;
        if(imgIn instanceof BufferedImage){
            W = ((BufferedImage)imgIn).getWidth();
            H = ((BufferedImage)imgIn).getHeight();
        }
        else{
            W = imgIn.getWidth(null);
            H = imgIn.getHeight(null);
        }
        float fW = (float)W;
        float fH = (float)H;
        float fRatio = (W > H) ? (fH/fW) : (fW/fH);
        int w = (int)((W > H) ? iMaxDim : (fRatio * fMaxDim));
        int h = (int)((H > W) ? iMaxDim : (fRatio * fMaxDim));
        int l = (W > H) ? 0 : ((iMaxDim - w)/2);
        int t = (H > W) ? 0 : ((iMaxDim - h)/2);
        
        if(bSquareFramed){
	    boolean bWithTransparency = false;
	    
	    if(bWithTransparency){
		int iMethod = 1;
		Graphics2D g2d = null;
		
		switch(iMethod){
		    case 1:
			    // The incoming imgIn will probably be of type.TYPE_INT_ARGB (must support an alpha channel)
			    // Works, but reduces the image quality - makes it look "grainy" or "pixelated".
			IndexColorModel cm1 = createIndexColorModel();
			imgOut = new BufferedImage(iMaxDim, iMaxDim, BufferedImage.TYPE_BYTE_INDEXED, cm1);
			g = imgOut.getGraphics();
			g.setColor(new Color(0,0,0,0));		
			g.fillRect(0, 0, iMaxDim, iMaxDim);
			g.drawImage(imgIn, l, t, w, h, null);
			g.dispose();		
			break;
		    case 2:
			    // does not work. transparency areas appear black.
			imgOut = new BufferedImage(iMaxDim, iMaxDim, iImgType);
			g2d = imgOut.createGraphics();
			Composite com = g2d.getComposite();
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
			g2d.setColor(new Color(0,0,0,0));
			g2d.fillRect(0, 0, iMaxDim, iMaxDim);
			g2d.setComposite(com);		
			g2d.drawImage(imgIn, l, t, w, h, null);
			g2d.dispose();
			break;
		    case 3:
			    // does not work. transparency areas appear black.
			    // http://www.exampledepot.com/egs/java.awt.image/DrawOnImage.html
			BufferedImage tempBuff = new BufferedImage(iMaxDim, iMaxDim, iImgType);
			g2d = imgOut.createGraphics();
			Color transparent = new Color(0, 0, 0, 0);
			g2d.setColor(transparent);
			g2d.setComposite(AlphaComposite.Src);
			g2d.fill(new Rectangle2D.Float(0, 0, iMaxDim, iMaxDim));
			g2d.drawImage(imgIn, l, t, w, h, null);
			g2d.dispose();
			break;
		}
	    }
	    else{
		    // http://forums.sun.com/thread.jspa?forumID=20&threadID=5227456
		imgOut = new BufferedImage(iMaxDim, iMaxDim, iImgType);
		g = imgOut.getGraphics();
		g.setColor(Color.WHITE);		
		g.fillRect(0, 0, iMaxDim, iMaxDim);
		//g.drawImage(imgIn, l, t, (w+l), (h+t), 0, 0, W, H, null);            
		g.drawImage(imgIn, l, t, w, h, null);
		g.dispose();		
	    }	    
        }
        else{
            imgOut = new BufferedImage(w, h, iImgType);
            g = imgOut.getGraphics();
            g.drawImage(imgIn, 0, 0, w, h, null);
	    g.dispose();
        }
        
        return imgOut;
    }

    
    static IndexColorModel createIndexColorModel() {
	    // http://forums.sun.com/thread.jspa?messageID=10305410
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
        return  new IndexColorModel(8, SIZE, r, g, b, a);
    }
    
    
	/**
	 * Without a BufferedImage parameter this function can only be called to invoke thumb creation methods
	 * NO_CONVOLVE and MULTI_CONVOLVE (explained further at {@link #writeDrawnThumb(BufferedImage, int, int) 
	 * writeDrawnThumb(BufferedImage, int, int)}. The missing Buffered image, not explicitly provided, must
	 * must be created from the source image identified in the constructor of this class. 
	 * {@link #writeDrawnThumb(BufferedImage, int, int) writeDrawnThumb(BufferedImage, int, int)} is called
	 * and takes over from there.
	 * @param iMaxDim int, The new size in pixels the larger of the two dimensions of imgIn will be resized to.
	 * @param iConvolve int, The number of times a ConvolveOp configured to blur will be run against imgIn to produce a
	 * @exception Exception, Thrown to be caught by {@link #generateThumb(boolean) generateThumb} 
	 */
    private void writeDrawnThumb(int iMaxDim, int iConvolve) throws Exception {
        BufferedImage imgIn = getUndrawnImage();
        if(iConvolve > 0){
                /**
                 * to convolve a jpeg without getting "java.awt.image.ImagingOpException: Unable to convolve src image",
                 * you must first convert it to a simpler image by creating a new BufferedImage with the same 
                 * dimensions and TYPE_INT_RGB, then getting the graphics and drawing it before convolving
                 */
            imgIn = getDrawnImage(imgIn, BufferedImage.TYPE_INT_RGB);                
        }
        writeDrawnThumb(imgIn, iMaxDim, iConvolve);        
    }
    
    
	/**
	 * Except for SCALE_INSTANCE, the methods of producing a shrunken version of an image are implemented by calling
	 * this function. These methods (NO_CONVOLVE, MULTI_CONVOLVE, SHRINK_CONVOLVE) are documented at 
	 * {@link #generateThumb(boolean) generateThumb(boolean)}. Of the 3 methods, the one used depends on the parameters
	 * used:
	 * @param imgIn BufferedImage, a "drawn", buffered version of the source image. In drawing the image (see 
	 * {@link #getDrawnThumbImage(Image, int, int, boolean) getDrawnThumbImage} or 
	 * {@link #getDrawnImage(BufferedImage, int, int, int) getDrawnImage}), if the image is scaled down in size (preshrunk), 
	 * the method being invoked is SHRINK_CONVOLVE (iConvolve would be greater than zero because there would be no reason
	 * to preshrink an image if you didn't intend to blur it (a convolve op).<br>However, if imgIn were not scaled down,
	 * then NO_CONVOLVE is the invoked method if iConvolve is greater than zero, and MULTI_CONVOLVE if it is greater.
	 * @param iMaxDim int, The new size in pixels the larger of the two dimensions of imgIn will be resized to.
	 * @param iConvolve int, The number of times a ConvolveOp configured to blur will be run against imgIn to produce a
	 * new blurred BufferedImage to pass off to function {@link #getDrawnThumbImage(Image, int, int, boolean) getDrawnThumbImage}
	 * for the remainder of the shrinking.
	 * @exception Exception, Thrown to be caught by {@link #generateThumb(boolean) generateThumb} 
	 */
    private void writeDrawnThumb(BufferedImage imgIn, int iMaxDim, int iConvolve) throws Exception{
        BufferedImage imgOut = null;
        BufferedImage imgConvolved = null;

        if(iConvolve > 0){
                // now that it is drawn, you can convolve and redraw without the "java.awt.image.ImagingOpException"
            BufferedImageOp blurOp = new ConvolveOp(new Kernel(3, 3, BLUR3X3));
            
            imgConvolved = blurOp.filter(imgIn, null);
            for(int i=0; i<=(iConvolve-1); i++){
                imgConvolved = blurOp.filter(imgConvolved, null); // blur iConvolve times
            }
            imgOut = getDrawnThumbImage(imgConvolved, imgConvolved.getType(), iMaxDim, true);
        }
        else{
            imgOut = getDrawnThumbImage(imgIn, imgIn.getType(), iMaxDim, true);
        }

        
        if(codeLib.directoryExists(sImgOutDir)){
            ImageIO.write(imgOut, this.sTargetImgType, new File(this.sImgOutPathName));
        }
    }    
    
	
	/**
	 * The last method of producing a shrunken version of an image documented at {@link #generateThumb(boolean) 
	 * generateThumb(boolean)} is "SCALE_INSTANCE. Rather than shrinking, blurring multiple times and shrinking the rest
	 * of the way, an image is not smoothed (blurred) as the result of a convolve operation, but smoothed similtaneously
	 * with the shrinking (scaling) of the image. Therefore, the process is less involved because the image is shrunken
	 * once (not shrink, multi-convolve, shrink). The basic difference is that the following:<p>
	 *     BufferedImage imgOut = new BufferedImage(iWidth, iHeight, iImgType); <p>
	 * Is not used as the method of resizing an image in favor of:<p>
	 *     Image imgScaled = imgIn.getScaledInstance(iMaxDim, iHeight, BufferedImage.SCALE_SMOOTH);<p>
	 * NOTE: because SCALE_SMOOTH uses an image-scaling algorithm that gives higher priority to image smoothness than 
	 * scaling speed, it is very slow. Therefore, its use is deferred in favor of the smoothing being part of a separate 
	 * convolve operation (see {@link #writeDrawnThumb(BufferedImage, int, int) writeDrawnThumb(BufferedImage, int, int)}.
	 * @param iMaxDim int, The new size in pixels the larger of the two dimensions of source Image will be resized to
	 */
    private void writeDrawnThumbSlow(int iMaxDim){
        BufferedImage imgIn = getDrawnImage();
        
        float fRatio = (float)imgIn.getHeight()/(float)imgIn.getWidth();
        int iHeight = (int)((float)iMaxDim * fRatio);

	Image imgScaled = imgIn.getScaledInstance(iMaxDim, iHeight, BufferedImage.SCALE_SMOOTH);        
        BufferedImage imgOut = getDrawnThumbImage(imgScaled, imgIn.getType(), iMaxDim, true);

        try{
            if(codeLib.directoryExists(this.sImgOutDir)){
                ImageIO.write(imgOut, this.sTargetImgType, new File(this.sImgOutPathName));
            }
            else{
                System.out.println("target directory \"" + this.sImgOutDir + "\" does not exist");
            }
        }
        catch(FileNotFoundException e){
            // @TODO: code for this error
            // most likely an access denied error because the directory has already been tested for existence
        }
        catch(IOException e){
            // @TODO: code for this error
            // catch all the remaining io errors, but will not be an access denied error.
        }
    }    
   
	
	/**
	 * Each imageFileFilter.ImageType enumeration has a corresponding String value. The String value will be the file
	 * extension of that particular type of image.
	 * @param imgType imageFileFilter.ImageType, one of 6 image type enumerations
	 * @return String, The file extension of the image indicated by the imageFileFilter.ImageType enumeration
	 */
    private static String getImageTypeAsString(imageFileFilter.ImageType imgType){
	String sRetval = null;
	switch(imgType){
	    case GIF: sRetval = "gif"; break;
	    case JPG: sRetval = "jpg"; break;
	    case JPEG: sRetval = "jpeg"; break;
	    case PNG: sRetval = "png"; break;
	    case BMP: sRetval = "bmp"; break;
	    case WBMP: sRetval = "wbmp"; break;
	}
	return sRetval;
    }  

	
	/**
	 * This is the one public instance method of the rescaler class. It is the method that users of rescaler will call
	 * to create a single thumbnail. All parameters such as size of source image, location of source image, method of
	 * scaling the image, etc. will have been passed in through the rescaler constructor when instantiating the class. 
	 * The only real image generation occurs when private method writeDrawnThumb is called. This is a one line statement, but
	 * all the remaining functionality prepares to execute that statement by:
	 * <p>
	 * - Preparing a name for the generated thumb that prevents any naming conflicts in the target directory<br>
	 * - testing if the source image exists and getting a shrunken, buffered, drawn copy of it in memory to work on.<br>
	 * - testing if the source file is really an image<br>
	 * - testing if the target directory exists and was successfully cleared in the event that bClearTargetDir is true<br>
	 * - cancelling image generation if any of the tests fail and instantiating a results class to display failure details
	 * or details of any failure that results if thumbnail generation is authorized.
	 * - branches into different methods of thumb generation (rescaling) depending on what value was set for the 
	 * {@link #method method} parameter. These methods are as follows:<p>
	 * SHRINK_CONVOLVE: Preshrinks the image, blurs it, then shrinks it the rest of the way (seems to be best combination 
	 * of speed and quality). This is the preferred and default method (constructor with no method parameter is used)<br>
	 * MULTI_CONVOLVE: blurs the image many times, but does not preshrink before doing so. Shrinks fully AFTER blurring<br>
	 * NO_CONVOLVE: does not preshrink or blur the image. Full shrinking only<br>
	 * SCALE_INSTANCE: blurs the image a different way by specifying it as a parameter to the BufferedImage.getScaledInstance
	 * method. Is SLOW.
	 * These 4 shrinking methods were basically tests. SHRINK_CONVOLVE proved to be the most effect way of shrinking.
	 * @param bClearTargetDir boolean, indicates if the target location of the thumbnail should be cleared of all other files
	 * @return deckman.images.reimage.result, carries all success or failure details of thumbnail generation.
	 */
    public result generateThumb(boolean bClearTargetDir){
	
	result r = TestThumbability(bClearTargetDir);

	if(r.getType() == result.VALUE_NOT_SET){
	    try{
		switch(this.method){
		    case NO_CONVOLVE: // does not "preshrink" or blur the image. Full shrinking only.
			writeDrawnThumb(this.iThumbSize, 0);
			break;
		    case MULTI_CONVOLVE: // blurs the image many times, but does not preshrink before doing so. Shrinks fully AFTER blurring
			writeDrawnThumb(this.iThumbSize, 9);
			break;
		    case SHRINK_CONVOLVE: // preshrinks image, blurs it, then shrinks the rest of the way (seems to be best combination of speed and quality).
			//BufferedImage imgShrunk = getDrawnThumbImage(BufferedImage.TYPE_BYTE_INDEXED, (this.iThumbSize*6), false);
			BufferedImage imgShrunk = getDrawnThumbImage(BufferedImage.TYPE_INT_ARGB, (this.iThumbSize*6), false);
			writeDrawnThumb(imgShrunk, this.iThumbSize, 4);
			break;
		    case SCALE_INSTANCE: // blurs the image a different way by specifying it as a parameter to the BufferedImage.getScaledInstance method. Is SLOW.
			writeDrawnThumbSlow(this.iThumbSize);
			break;
		}        		
	    }
	    catch(Exception e){
		r.setError(codeLib.getStackTraceAsString(e));
		r.setType(result.REIMAGE_CANCELLED_ON_FIRST_ERROR);
	    }
	}

	if(r.getType() == result.VALUE_NOT_SET){
	    r.setType(result.REIMAGE_SUCCESS);
	}
	
	return r;
    }

    
	/**
	 * An image is "thumbable" - meaning a separate file can be produced from it through reimaging - if the following
	 * criteria are met:<p>
	 *    - The target directory for the thumb file could be cleared of all other files if so specified when calling this function<br>
	 *    - The source image can be tested bitwise for positive identification as some kind of image.<br>
	 *    - The target directory for the thumbfile exists.<br>
	 *    - The source image exists.<p>
	 * @param bClearTargetDir boolean, Specifies that the directory to which the thumb file is to be written is to be cleared of all other files first
	 * @return deckman.images.reimage.result, Thumbability will be indicated by the return of result.getType(). 
	 * If not result.VALUE_NOT_SET, then the source image is not thumbable and the actual value will indicate the reason why.
	 */
    private result TestThumbability(boolean bClearTargetDir){
	result r = new result();
	r.setImageFileInSpec(this.sImgInPathName);
	r.setThumbFileOutDir(this.sImgOutDir);
	r.setImageType(this.sTargetImgType);
	r.setImageSize(new File(sImgInPathName).length());	

	if(bClearTargetDir){
	    if(!codeLib.clearDirectory(this.sImgOutDir)){
		r.setType(result.REIMAGE_CANCELLED_FAILED_TO_CLEAR_TARGET_DIR);
	    }
	}

	if(!codeLib.isImageFile(new File(sImgInPathName))){
	    r.setType(result.REIMAGE_CANCELLED_NOT_IMAGE);
	}

	if(!(new File(sImgOutDir).exists())){
	    r.setType(result.REIMAGE_CANCELLED_NO_TARGET_DIR);
	}

	if(!(new File(sImgInPathName).exists())){
	    r.setType(result.REIMAGE_FILE_NOT_FOUND);
	}
	
	return r;
    }
    

        /**
         * The following code will clip a 100 X 100 pixel section from the top left corner of an image 
	 * and write it out as a separate image of the type specified. This is useful to quickly test the basic operation
	 * of reading a file in, and writing some a portion of it out. So, for instance, one would find out this way 
	 * that writing out a jpeg as a gif does not work on a JVM prior to 1.6.
	 * @param sFileIn String, The image file from which the clip will be obtained
	 * @param the type of file the clip will be (gif, png, bmp, etc.)
         */
    public static void test(String sFileIn, imageFileFilter.ImageType imgType){
        File in = new File(sFileIn);
        BufferedImage imgIn = null;
        try{
            imgIn = ImageIO.read(in);        
            BufferedImage imgOut = new BufferedImage(100, 100, imgIn.getType());
            Graphics g = imgOut.getGraphics();
            g.drawImage(imgIn, 0, 0, null);
            g.dispose();
            String sFileOut = codeLib.removeFileExtension(sFileIn) + "." + getImageTypeAsString(imgType);
            ImageIO.write(imgOut, "gif", new File(sFileOut));            
        }
        catch(java.io.IOException e){
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args){       
            /* http://www.developer.com/java/other/article.php/3696676#Resources
             * EXPLAINS HOW TO PUT TOGETHER A KERNEL FOR A CONVOLVEOP: http://java.sun.com/developer/JDCTechTips/2004/tt0210.html */ 
	
	     // Set the parameters
        //String sImgInPathName = "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\web\\images\\temp\\reimage\\in\\!!!b-4.jpg";
        //String sImgOutDir = "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\web\\images\\temp\\reimage\\out";

        String sImgInPathName = "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\fullsize\\!!!b-4.JPG";
        String sImgOutDir = "C:\\myTechnicalStuff\\projects\\java\\projects\\netbeans\\web\\deckman\\build\\web\\images\\thumbnails";
	
	imageFileFilter.ImageType targetImgType = imageFileFilter.ImageType.GIF;
        RescaleMethod method = RescaleMethod.SHRINK_CONVOLVE;
        //RescaleMethod method = RescaleMethod.NO_CONVOLVE;
        int iThumbSize = 100;
        boolean bSquareFramed = true;
	
	    // instantiate the rescaler and produce the thumb.
        rescaler rsc = new rescaler(sImgInPathName, sImgOutDir, codeLib.removePathFromFileSpec(sImgInPathName), targetImgType, method, iThumbSize, bSquareFramed);
        result r = rsc.generateThumb(false);
        System.out.println(r.getDescription());

    }   
   
}
