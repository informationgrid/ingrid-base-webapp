package de.ingrid.admin.tags;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public class MenuTag  extends TagSupport {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    
    private String text;
    private String url;
    
    public int doStartTag() throws JspTagException
    {
        boolean hasPlugDescr = (Boolean) pageContext.getRequest().getAttribute( "plugdescriptionExists" );
        String target = url.substring( url.lastIndexOf( '/' ) + 1, url.length() - 5 );
        boolean isActive = target.equals( pageContext.getRequest().getAttribute( "active" ) );
        
        try {
            if (!hasPlugDescr) {
                if (isActive) {
                    pageContext.getOut().print( "<li class='active'>" + text + "</li>" );                
                    
                } else {
                    pageContext.getOut().print( "<li>" + text + "</li>" );
                }
            } else if (!isActive) {
                pageContext.getOut().print( "<li><a href=\"" + url + "\">" + text + "</a></li>" );                
                
            } else {
                pageContext.getOut().print( "<li class='active'>" + text + "</li>" );                
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      return SKIP_BODY;
    }

    public int doEndTag() throws JspTagException
    {
      return EVAL_PAGE;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
