
package deckman.utils;

import javax.servlet.*;
import javax.servlet.http.*; 
    
    /**
     * NOTE - NOT IN USE: The sessionCreated events are being fired just fine, however, the sessionDestroyed events only fire when the session
     * timeout is reached and not when the browser is closed, the user navigates away from the site, or the web server is shut down.
     * Also, when the session time out is reached, the sessionDestroyed event doesn't always fire right away, and it has been seen
     * once to fire BEFORE the timeout was reached. Therefore it seems that tomcat 5.x is too unpredictable with respect to the 
     * sessonDestroyed event for it to be practical here. It may be useful to keep an all time sum of how many people have hit the site.
     * But in that case, the sessionCreated event will do. Originally its intended use was to keep a count of how many people have 
     * active sessions on the site. This would have been good to see on the home page "people currently viewing the site" and it would
     * also have provided a good indication when it was safest to renew the singleton initialization cache from the initialization.prop 
     * file. Oh well, maybe this will work ok with tomcat 6.x
     */

    /**
     * This class allows the tracking of session creation and destruction on an application wide scope. For this to work, 
     * this listener must be registered with tomcat by making an entry in the deployment descriptor (web.xml) as follows:
     *
     *   <listener> 
     *       <listener-class>deckman.DeckmanSessionListener</listener-class> 
     *   </listener>
     *
     *  For the deckman site, this will allow me to show how many people are browsing the site at any given time. All that needs
     * be done is to 
     */
public class DeckmanSessionListener implements HttpSessionListener {
    
    private static final String sAttrib = "deckman.sessions.created.counter";
    
    public void sessionCreated(HttpSessionEvent event){
        System.out.println("Session Created...");
        /*
        int[] counter = getCounter(event); 
        counter[0]++; 
         */
    }
    
    public void sessionDestroyed(HttpSessionEvent event){
        System.out.println("Session Destroyed...");
        /*
        int[] counter = getCounter(event); 
        counter[0]--; 
         */
    }
    
    private int[] getCounter(HttpSessionEvent hse) { 
        HttpSession session = hse.getSession(); 
        ServletContext context = session.getServletContext(); 
        int[] counter = (int[])context.getAttribute(sAttrib); 
        if(counter == null){ 
            counter = new int[1]; 
            context.setAttribute(sAttrib, counter); 
        } 
        return counter; 
    }
    
        /**
         * This function is for jsp pages that want to know how many people are currently viewing the website.
         * @param session - HttpSession, the session of the jsp page querying how many current viewers
         * @return - int, the number of people currently viewing the website (number of active sessions).
         */
    public static int sessionCount(HttpSession session){
        ServletContext context = session.getServletContext(); 
        int[] counter = (int[])context.getAttribute(sAttrib);
        return counter==null ? 1 : counter[0];
    }
}
