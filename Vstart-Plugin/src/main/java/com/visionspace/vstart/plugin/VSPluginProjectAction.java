/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.model.AbstractProject;
import hudson.model.Action;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginProjectAction implements Action {

    private final AbstractProject project;
    
    public VSPluginProjectAction(AbstractProject project){
        this.project = project;
    }
    
    @Override
    public String getIconFileName() {
        return "credentials.png";
    }

    @Override
    public String getDisplayName() {
        return "VSTART report";
    }

    @Override
    public String getUrlName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public AbstractProject getProject(){
        return this.project;
    }
    
}
