/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginProjectAction implements Action {

    private final AbstractProject project;
    private final JSONObject jObj;
    
    public VSPluginProjectAction(AbstractProject project, JSONObject jArray){
        this.project = project;
        this.jObj = jArray;
    }

    public VSPluginProjectAction(AbstractProject<?, ?> project) {
        this.project = project;
        this.jObj = getJArray();
    }
    
    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }
    
    public AbstractProject getProject(){
        return this.project;
    }
    
    public AbstractBuild getLastFinishedBuild() {
        AbstractBuild lastBuild = project.getLastBuild();
        
        while(lastBuild != null && lastBuild.isBuilding()) {
            lastBuild = lastBuild.getPreviousBuild();
        }
        
        return lastBuild;
    }
    
    public JSONObject getJArray(){
        String rel = "VSTART_JSON/";
        FilePath rp = new FilePath(getLastFinishedBuild().getWorkspace(), rel);
        Path file = FileSystems.getDefault().getPath(rp.getRemote(), "/VSTART_JSON_" + getLastFinishedBuild().getNumber() + ".json");
        byte[] fileArray;
        try {
            fileArray = Files.readAllBytes(file);
            String str = new String(fileArray, Charset.defaultCharset());
            JSONObject json = new JSONObject(str);
            return json;
        } catch (IOException ex) {
            Logger.getLogger(VSPluginRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
