/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.tasks.test.AbstractTestResultAction;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.kohsuke.stapler.StaplerProxy;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginBuildAction extends AbstractTestResultAction {

    private final AbstractBuild build;
    
    
    public VSPluginBuildAction(AbstractBuild build){
        this.build = build;
    }
    
    @Override
    public String getIconFileName() {
        return "graph.gif";
    }

    @Override
    public String getDisplayName() {
        return "VSTART DUMMY";
    }

    @Override
    public String getUrlName() {
        return null;
    }
    
    public AbstractBuild getBuild(){
        return this.build;
    }
    
    public JSONObject getJSON(){
        String rel = "VSTART_JSON/";
        FilePath rp = new FilePath(build.getWorkspace(), rel);
        Path file = FileSystems.getDefault().getPath(rp.getRemote(), "/VSTART_JSON_" + build.getNumber() + ".json");
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

    @Override
    public int getFailCount() {
        JSONObject json = getJSON();
        //This is going to have to be a loop through a JSONArray
        
        if(json.getString("status").equals("FAILED")){
            return 1;
        }
        return 0;
    }

    @Override
    public int getTotalCount() {
        return 1;
    }

    @Override
    public Object getResult() {
        return this;
    }
}
