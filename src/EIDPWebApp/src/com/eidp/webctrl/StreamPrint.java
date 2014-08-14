/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eidp.webctrl;

import com.eidp.UserScopeObject.UserScopeObject;
import com.eidp.webctrl.modules.Document;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author brit.sysman
 */
public class StreamPrint {
 
    public StreamPrint(HttpServletResponse response, UserScopeObject uso, Document document) {
        ByteArrayOutputStream byteArrayOutputStream = document.generate(uso);
        response.setContentType(document.getContentType());
        response.setHeader("Content-disposition", "inline; filename=" + document.getFilename() + "." + document.getFileExtension());
        response.setHeader("Cache-control", "private, max-age=0, no-cache, no-store");
        response.setContentLength(byteArrayOutputStream.size());
        ServletOutputStream servletOutputStream;
        try {
            servletOutputStream = response.getOutputStream();
            byteArrayOutputStream.writeTo(servletOutputStream);
            servletOutputStream.flush();
        } catch (IOException ex) {
            Logger.getLogger(StreamPrint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
