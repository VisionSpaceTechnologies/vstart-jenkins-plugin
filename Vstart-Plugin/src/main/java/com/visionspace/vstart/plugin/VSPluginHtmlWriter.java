/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginHtmlWriter {
        
    public VSPluginHtmlWriter(){
    }
    
    public boolean doHtmlReport(AbstractBuild build){
        try {
            FilePath hPath = new FilePath(build.getWorkspace(), build.getWorkspace().toString() + "/VSTART_HTML");
            if(!hPath.exists()){
                hPath.mkdirs();
            }
            
            PrintWriter wp = new PrintWriter(hPath + "/VSTART_REPORT_" + build.getNumber() + ".html");
            StringBuilder builder = new StringBuilder();
            builder.append("<!DOCTYPE html>");
            builder.append("<html>");
            builder.append("<head><title>Vstart Report</title></head>");
            builder.append("<body><h1>Vstart Report</h1></body>");
            builder.append("</html>");
            
            wp.print(builder.toString());
            
            wp.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InterruptedException ex) {
            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
