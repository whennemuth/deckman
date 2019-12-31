/*
 * reimageProgress.java
 *
 * Created on April 30, 2008, 11:56 PM
 */

package deckman.images.reimage;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import deckman.utils.codeLib;
/**
 *
 * @author Warren
 * @version
 */
public class reimageProgress extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
	String sRetval = null;
	PrintWriter out = response.getWriter();
	HttpSession session = request.getSession(true);
	

	try{
	    response.setContentType("text/html;charset=UTF-8");

	    String sType = request.getParameter("type");
	    if(sType == null) sType = "percent";

	    Map m = getProgressCounter(session);
	    Float fDone = new Float(((Integer)m.get("done")).intValue());
	    Float fTotal = new Float(((Integer)m.get("total")).intValue());

	    if(fDone==0 && fTotal==0){
		sRetval = "0,0,0";
	    }
	    else{
		if(sType.equalsIgnoreCase("PERCENT")){
		    float fPercent = fDone/fTotal * 100f;
		    sRetval = String.valueOf(Math.round(fPercent));	    
		}
		else if(sType.equalsIgnoreCase("DONE")){
		    sRetval = String.valueOf(Math.round(fDone));
		}
		else if(sType.equalsIgnoreCase("REMAIN")){
		    sRetval = String.valueOf(Math.round(fTotal - fDone));
		}
		else if(sType.equalsIgnoreCase("DETAILED")){
		    float fPercent = fDone/fTotal * 100f;
		    sRetval = String.valueOf(Math.round(fDone)) + ",";
		    sRetval += (String.valueOf(Math.round(fTotal)) + ",");
		    sRetval += (String.valueOf(Math.round(fPercent)));
		}
	    }
	}
	catch(Exception e){
	    sRetval = codeLib.getStackTraceAsString(e);
	}

	out.write(sRetval);
	out.flush();
	response.flushBuffer();
	out.close();
    }
    
    
    private Map getProgressCounter(HttpSession session) throws Exception {
	Map map = (Map)session.getAttribute("THUMB_PROGRESS");
	if(map == null){
	    map = new HashMap();
	    map.put("done", new Integer(0));
	    map.put("total", new Integer(0));
	}
	
	return map;			
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
	processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
	processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
	return "Short description";
    }
    // </editor-fold>

}
