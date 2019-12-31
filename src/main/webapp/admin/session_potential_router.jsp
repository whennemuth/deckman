<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="deckman.settings.*, deckman.utils.codeLib, java.util.regex.*, deckman.settings.*, com.warren.logging.*, com.warren.logging.logs.*"%>
    
<%!	
		/**
		 * Here authorization to view pages in the administrative portion of the website is given if the properties file 
		 * indicates that a password is required and that password matches the one also contained in the properties file.
		 * The properties is only accessed once for the password in login.jsp. After that the fact that the password was
		 * successfully matched is indicated by a session variable ("deckman in the house").
		 * @param session HttpSession, The data in the properties file may be in session state, so the session is a parameter
		 * @return boolean, true if the user can access administrative pages of the site, false if they cannot.
		 */
	public boolean entranceAuthorized(javax.servlet.http.HttpSession session, initialization init){
		Boolean bRequirePassword = init.getPropertyAsBoolean("RequireAdminPassword");

		if(bRequirePassword.equals(Boolean.TRUE)){
			Object sDeckmanIsHere = session.getAttribute("deckman in the house");
			boolean bDeckmanIsHere = false;
			if(sDeckmanIsHere != null){
				if(((String)sDeckmanIsHere).equalsIgnoreCase("yes")){
					bDeckmanIsHere = true;
				}
			}
			return bDeckmanIsHere;
		}
		else{
			return true;
		}
	}
%>




<%
        // This section of code gets an initialization object representing all the properties
        // in the default properties file and prepares a log file for static use throughout
        // the website.
	initialization init = initialization.getInstance(session);

    if(!StaticLog.isSet()){
        String sLogPathName = init.getPropertyAsString("AdminLogPathName");
        com.warren.logging.ILog log = null;
        try{
            log = new PeriodLog(sLogPathName, PeriodLog.period.MONTHLY);
        }
        catch(java.io.FileNotFoundException e){
            out.print("<pre>" +
                    "The location for the log file\r\n\r\n" +
                    "    " + com.warren.logging.codeLib.getPathFromFileSpec(sLogPathName) +
                    "\r\n\r\ncould not be found. Stack trace as follows:\r\n\r\n" +
                    com.warren.logging.codeLib.getStackTraceAsString(e));
        }
        StaticLog.setLog(log);
    }
%>




<%
	response.setHeader("Cache-Control", "no-cache");
	response.addHeader("Cache-Control", "no-store");
	response.addHeader("Cache-Control", "must-revalidate");
	response.setDateHeader("Expires", 0); 
	response.addHeader("Expires", "Mon, 8 Aug 2006 10:00:00 GMT"); // some date in the past 
	response.setHeader("Pragma", "no-cache"); 

	
	String sServletPath = request.getServletPath();
	boolean bIsLoginPage = sServletPath.endsWith("admin/login.jsp");
	boolean bResin = application.getServerInfo().toUpperCase().indexOf("RESIN") > -1;
	boolean bCheckSessionCookie = init.getPropertyAsBoolean("RequireSessionCookieCheck").booleanValue();
	boolean bSessionCookiesOK = !bCheckSessionCookie;	// see NOTE 1 below
	
	
	if(bResin){
		if(!bIsLoginPage){
				// testing browser session cookie support via redirect does not work with resin - see NOTE 1 below
				// therefore defer session cookie support to occur as part of the login process.
			if(!entranceAuthorized(session, init)){
					// the following session attribute will be used by a jsp forward in login.jsp, therefore needs a servlet path
				session.setAttribute("JspAfterLogin", request.getServletPath());
				response.sendRedirect("login.jsp");	// see NOTE 3 below
			}
		}
	}
	else{	// tomcat
		if(bCheckSessionCookie){
			String sQueryString = request.getQueryString();
			if(sQueryString == null){
				if(session.getAttribute("cookie_test") == null){
					 session.setAttribute("cookie_test", "oreo");
					 String sRefreshURL = request.getRequestURL() + "?CheckCookie";
					 response.sendRedirect(sRefreshURL);
				}
				else{
					bSessionCookiesOK = true;
				}
			}
			else{
				if(sQueryString.equals("CheckCookie")){
					if(session.getAttribute("cookie_test") == null){
						if(!response.isCommitted()){
							%> <jsp:forward page="/sessionless.jsp"/> <%	
						}
					}
					else{
						bSessionCookiesOK = true;
					}
				}
			}
		}
		
		if(bSessionCookiesOK && !bIsLoginPage){
			if(!entranceAuthorized(session, init)){
					// the following session attribute will be used by a jsp forward in login.jsp, therefore needs a servlet path
				session.setAttribute("JspAfterLogin", request.getServletPath());
				response.sendRedirect("login.jsp");	// see NOTE 3 below
			}
		}
	}
	
	
	/*  
		NOTE 1:
		
		NOTE 2:
		 
		NOTE 3: a <jsp:forward page="login.jsp /> alternative to redirecting results in an endless loop back to login.jsp 
		despite nesting a <jsp:param or adding a querystring like "login.jsp?forwarded" to serve as a flag to break the loop.
		The basic problem is: how do you tell if a page has just performed a forwarding action back to itself? In the 
		following sequence:
		1) a.jsp forwards --> 2) b.jsp forwards --> 3) b.jsp
		The request.getRequestURL will return a.jsp for 2) and 3) because the orignal request has not changed due to the fact
		that the jsp forward action keeps to the server, so the request object cannot be relied upon to provide
		the answer as to whether or not a page forwarded to itself.
		And it's unknown why:
		<jsp:forward page="login.jsp">
			<jsp:param name="forwarded" value="true" />
		</jsp:forward>
		leads to null when the page forwarded to performs a check for request.getParameter("forwarded")
	*/

%>
