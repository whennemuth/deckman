<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="deckman.settings.*, deckman.utils.codeLib"%>

<%!
    private static final String sSelectedClass = "adminSelected";
    private static final String sUnselectedClass = "adminUnselected";
    private static final String sRolloverClass = "adminRollover";
    
        /**
        * returns the class name for styles that should be applied to one of the anchor tags that make up the banner that
        * this jsp page creates.
        * @param request javax.servlet.http.HttpServletRequest, the request that comes in from the jsp includes calling this page.
        * @param sPage String, The anchor tags that make up the banner this jsp page creates are identified by the page that they
        * link to with the sPage parameter. If the banner_selection request parameter matches sPage, then that anchor tag should 
        * be assigned a specific class that displays it with a "selected" color, else a non-selected color.
        */
    public String getClass(javax.servlet.http.HttpServletRequest request, String sPage){
        String sSelection = request.getParameter("banner_selection");
		if(sSelection == null){
			return sUnselectedClass;
		}
		else{
			if(sSelection.equalsIgnoreCase(sPage)) return sSelectedClass;
			else return sUnselectedClass;			
		}
    }
    
        /**
        * returns the class name for styles that should be applied to one of the anchor tags that make up the banner that
        * this jsp page creates.
        * @param request javax.servlet.http.HttpServletRequest, the request that comes in from the jsp includes calling this page.
        * @param sPage String, The anchor tags that make up the banner this jsp page creates are identified by the page that they
        * link to with the sPage parameter. If the banner_selection request parameter matches sPage, then that anchor tag should 
        * be assigned a specific class that displays it with a "selected" color, else a non-selected color.
        * @param bRollover boolean, Indicates that a class name should be returned that when applied to the achor tag will cause 
        * it to appear red. This is the color the tag should be if the user mouses over it.
        */
    public String getClass(javax.servlet.http.HttpServletRequest request, String sPage, boolean bRollover){
        if(bRollover){
            String sSelection = request.getParameter("banner_selection");
			if(sSelection == null){
				return sRolloverClass;
			}
			else{
				if(sSelection.equalsIgnoreCase(sPage)) return sSelectedClass;
				else return sRolloverClass;				
			}
        }
        else{
            return getClass(request, sPage);            
        }
    }
%>





<%
	response.setHeader("Cache-Control", "no-cache");
	response.addHeader("Cache-Control", "no-store");
	response.addHeader("Cache-Control", "must-revalidate");
	response.setDateHeader("Expires", 0); 
	response.addHeader("Expires", "Mon, 8 Aug 2006 10:00:00 GMT"); // some date in the past 
	response.setHeader("Pragma", "no-cache"); 

    String[] aPages = new String[]{"fileUpload", "unzip", "cleanup", "organize", "settings_1"};
    String[] aText = new String[]{"Upload a File", "Unzip Uploaded Zip File", "Site Cleanup", "Organize Images", "Site Settings"};
%>

<table cellpadding=6 cellspacing=0 width="800px">
<tr>
    <%
        for(int i=0; i<aPages.length; i++){
            %>
                <td valign="bottom" align="center">
                    <a 
                        href="<%=(aPages[i]+".jsp")%>" 
						target="_top" 
                        class="<%=getClass(request, aPages[i])%>"
                        onmouseover="javascript:this.className = '<%=getClass(request, aPages[i], true)%>';" 
                        onmouseout="javascript:this.className='<%=getClass(request, aPages[i])%>';" 
                    >
                        <%=aText[i]%>
                    </a>
                </td>
            <%
            if((i+1) < aPages.length){
                %>
                <td valign="middle" class="<%=getClass(request, "|")%>">|</td>
                <%
            }
        }
    %>
</tr>
</table>
