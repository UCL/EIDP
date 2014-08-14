/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.webctrl.modules;

import com.eidp.UserScopeObject.UserScopeObject;
import java.io.ByteArrayOutputStream;

/**
 *
 * @author brit.sysman
 */
public interface Document {
    
    public String getContentType();
    public String getFileExtension();
    public String getFilename();
    public ByteArrayOutputStream generate(UserScopeObject uso);
    
}
