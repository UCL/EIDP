/*	
 * MutationException.java	
 *	
 * Created on 12. Januar 2006, 14:30	
 */	
	
package com.eidp.webctrl.modules.ESID.Mutation;	
	
/**	
 *	
 * @author  david	
 */	
public class MutationException extends java.lang.Exception {	
    	
    /**	
     * Creates a new instance of <code>MutationException</code> without detail message.	
     */	
    public MutationException() {	
    }	
    	
    	
    /**	
     * Constructs an instance of <code>MutationException</code> with the specified detail message.	
     * @param msg the detail message.	
     */	
    public MutationException(String msg) {	
        super(msg);	
    }	
}	
