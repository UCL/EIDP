/*	
 * EIDPAddInLoaderAPI.java	
 *	
 * Created on 3. Mai 2007, 14:44	
 *	
 * To change this template, choose Tools | Template Manager	
 * and open the template in the editor.	
 */	
	
package com.eidp.webctrl.modules;	
	
import com.eidp.UserScopeObject.UserScopeObject;	
import java.io.PrintWriter;	
import javax.servlet.http.HttpServletRequest;	
import javax.servlet.http.HttpServletResponse;	
	
/**	
 *	
 * @author rusch	
 */	
public abstract class EIDPAddInLoaderAPI {	
    	
    /**	
     * Creates a new instance of EIDPAddInLoaderAPI	
     */	
     public void EIDPAddInLoaderINIT( String loadAddOnClass, PrintWriter printWriter, HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws java.rmi.RemoteException , java.io.IOException , java.sql.SQLException , org.xml.sax.SAXException {		
        EIDPLoadClass( loadAddOnClass , printWriter , request , response , uso ) ;		
    }		
    		
    public abstract void EIDPLoadClass( String loadAddInClass , PrintWriter printWriter , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso )  ;		
    		
    	
}	
