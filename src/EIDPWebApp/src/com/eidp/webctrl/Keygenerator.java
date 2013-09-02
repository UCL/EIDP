/*
 * keygenerator.java
 *
 * Created on 24. Januar 2005, 14:52
 */

package com.eidp.webctrl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 *
 * @author Stephan Rusch
 * @version 3.0
 * @copyright Copyright (C) 2005 Stephan Rusch (schmutz@powl.name)
 * Enterprise Integration and Development Platform
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License form ore details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 */


public class Keygenerator {
    
    
    /** Creates a new instance of keygenerator */
    public Keygenerator() {
    }
    
    public static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).toUpperCase().substring(1,3));
        }
        return sb.toString();
    }
    
    public String getWebKey( String UserID ) {
        String webkey = "";
        Date date = new Date();
        Long millisec = new Long(date.getTime());
        String mykey = UserID + millisec.toString();
        try {
            MessageDigest inst = MessageDigest.getInstance("MD5");
            byte[] hash = inst.digest(mykey.getBytes());
            String receptor = hex(hash);
            webkey = receptor;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Problems with the algorithm :-(" + e);
        }
        return webkey;
    }
    
}
