/*	
 * EIDPAddInLoader.java	
 *	
 * Created on 3. Mai 2007, 14:43	
 *	
 * To change this template, choose Tools | Template Manager	
 * and open the template in the editor.	
 */	
	
package com.eidp.webctrl.modules;	
	
import com.eidp.UserScopeObject.UserScopeObject;	
import java.io.PrintWriter;	
import java.lang.reflect.Constructor;	
import javax.servlet.http.HttpServletRequest;	
import javax.servlet.http.HttpServletResponse;	
	
/**	
 *	
 * @author rusch	
 */	
public class EIDPAddInLoader extends EIDPAddInLoaderAPI {	
    	
    /** Creates a new instance of EIDPAddInLoader */	
    public EIDPAddInLoader( String loadAddOnClass , PrintWriter printWriter, HttpServletRequest request , HttpServletResponse response , UserScopeObject uso ) throws java.rmi.RemoteException , java.io.IOException , java.sql.SQLException , org.xml.sax.SAXException {		
        this.EIDPAddInLoaderINIT( loadAddOnClass , printWriter, request , response , uso ) ;		
    }		
    		
    public void EIDPLoadClass( String loadAddOnClass , PrintWriter printWriter , HttpServletRequest request , HttpServletResponse response , UserScopeObject uso )  {		
        try{		
            String AddOnClassName = "com.eidp.webctrl.addinmodules." + loadAddOnClass ;		
            System.out.println("LOADING AddIn " + AddOnClassName);	
            Class klasse = Class.forName( AddOnClassName ) ;		
            Class[] paramClasses = { PrintWriter.class , HttpServletRequest.class , HttpServletResponse.class , UserScopeObject.class } ;		
            Constructor constr = klasse.getConstructor( paramClasses ) ;		
            Object[] paramObjects = { printWriter , request , response , uso } ;		
            Object object = constr.newInstance( paramObjects ) ;		
        } catch ( java.lang.ClassNotFoundException e ) {	
            System.out.println("LOADING AddIn " + e);	
            e.printStackTrace();	
        } catch ( java.lang.NoSuchMethodException e ) {		
            System.out.println("LOADING AddIn " + e);	
            e.printStackTrace();	
        } catch ( java.lang.InstantiationException e ) {		
            System.out.println("LOADING AddIn " + e);	
            e.printStackTrace();	
        } catch ( java.lang.IllegalAccessException e ) {		
            System.out.println("LOADING AddIn " + e);	
            e.printStackTrace();	
        } catch ( java.lang.reflect.InvocationTargetException e ) {	
            System.out.println("LOADING AddIn " + e);	
            e.printStackTrace();	
        }		
        		
    }	
}	
