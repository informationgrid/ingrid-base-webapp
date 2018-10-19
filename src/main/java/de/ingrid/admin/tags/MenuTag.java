/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

public class MenuTag  extends TagSupport {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static Log log = LogFactory.getLog(MenuTag.class);
    
    private String text;
    private String url;
    
    public int doStartTag() throws JspTagException
    {
        Object pdExists = pageContext.getRequest().getAttribute( "plugdescriptionExists" );
        boolean hasPlugDescr = pdExists == null ? false : (Boolean) pdExists;
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
            log.error("Error during parsing", e);
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
