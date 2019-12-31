/*
 * testservlet.java
 *
 * Created on October 13, 2008, 3:49 AM
 */

package deckman.tests;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author Warren
 * @version
 */
public class testservlet extends HttpServlet {
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
	response.setContentType("text/html;charset=UTF-8");
	PrintWriter out = response.getWriter();
	out.println("<html>");
	out.println("<head>");
	out.println("<title>Servlet testservlet</title>");
	out.println("</head>");
	out.println("<body>");
	out.println("<h1>Servlet testservlet at " + request.getContextPath () + "</h1>");
	out.println("<br>");
	out.println("<div style='color:red'>");
	out.print("static hello = '" + deckman.tests.hello.getStaticHello());
	out.println("<br>");
	deckman.tests.hello myhello = new deckman.tests.hello();
	out.println("instance hello = '" + myhello.getInstanceHello());
	out.println("</div>");
	out.println("</body>");
	out.println("</html>");
	out.close();
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
