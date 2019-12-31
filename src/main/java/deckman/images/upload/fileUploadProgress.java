/*
 * fileUploadProgress.java
 *
 * Created on November 4, 2007, 4:47 PM
 */

package deckman.images.upload;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import com.warren.logging.*;

public class fileUploadProgress extends HttpServlet {
    
    /**Get the ProgressReporter attributes set by the latest of repeating callbacks to its update method by the 
     * ServletFileUpload object (reference to ProgressReporter instance is stored in session variable LISTENER)
     * Alternately, all callback results can be returned - it depends on what the "type" querystring parameter that
     * went with the AJAX call to this servlet was set to ("ALL" or "LAST").
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        response.setHeader("Last-Modified", (new Date()).toString());
        response.setHeader( "Pragma", "no-cache" );
        response.addHeader( "Cache-Control", "must-revalidate" );
        response.addHeader( "Cache-Control", "no-cache" );
        response.addHeader( "Cache-Control", "no-store" );
        response.setDateHeader("Expires", -1);

        try{
            String sReply = "";
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            HttpSession session = request.getSession();
                /*get the ProgressReporter attributes set by the latest of repeating callbacks to its update method by the 
                  ServletFileUpload object (reference to ProgressReporter instance is stored in session variable LISTENER)*/
            String sType = request.getParameter("type");
            if(sType == null) sType = "last";

            if(sType.equalsIgnoreCase("LAST")){
                HashMap map = uploader.getLatestUploadProgressUpdate(session);
                if(map.containsKey(uploader.NO_SESSION)){
                    sReply = uploader.NO_SESSION;
                }
                else if(map.containsKey(uploader.NO_LISTENER)){
                    sReply = uploader.NO_LISTENER;
                }
                else if(map.containsKey(uploader.NO_UPDATE_DATA)){
                    sReply = uploader.NO_UPDATE_DATA;
                }
                else{
                    sReply += String.valueOf(map.get(new String("Item"))) + ",";
                    sReply += String.valueOf(map.get(new String("BytesRead"))) + ",";
                    sReply += String.valueOf(map.get(new String("ContentLength")));
                }
            }


                //return the latest data set on the ProgressReporter 
            out.write(sReply);
            out.flush();
            response.flushBuffer();
            out.close();
        }
        catch(IOException e){
            StaticLog.log(e, LogEntry.EntryType.ERROR);
            throw e;
        }
        catch(NullPointerException e){
            StaticLog.log(e, LogEntry.EntryType.ERROR);
            e.printStackTrace();
        }    
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
   }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
