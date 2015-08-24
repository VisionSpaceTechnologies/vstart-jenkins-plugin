/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.tasks.test.AbstractTestResultAction;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.json.JSONArray;
import org.json.JSONObject;

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
        return "VSTART Report";
    }

    @Override
    public String getUrlName() {
        String urlName = build.getProject().getAbsoluteUrl() + "ws/VSTART_HTML/VSTART_REPORT_" + build.getNumber() + ".html";
        return urlName;
    }
    
    public AbstractBuild getBuild(){
        return this.build;
    }
    
    public JSONArray getJSON(){
        String rel = "VSTART_JSON/";
        FilePath rp = new FilePath(build.getWorkspace(), rel);
        Path file = FileSystems.getDefault().getPath(rp.getRemote(), "/VSTART_JSON_" + build.getNumber() + ".json");
        byte[] fileArray;
        try {
            fileArray = Files.readAllBytes(file);
            if(fileArray.length == 0){
                    return new JSONArray();
            }
            String str = new String(fileArray, Charset.defaultCharset());
            JSONObject jObj = new JSONObject(str);
            JSONArray json = jObj.getJSONArray("steps");
            return json;
        } catch (IOException ex) {
            Logger.getLogger(VSPluginRecorder.class.getName()).log(Level.SEVERE, null, ex);
            return new JSONArray();
        }
        
    }

    @Override
    public int getFailCount() {
        JSONArray json = getJSON();
        int fails = 0;

        for(int i = 0; i < json.length(); i++){
            if(json.getJSONObject(i).getString("status").equals("FAILED")){
                fails++;
            }
        }
        
        return fails;
    }

    @Override
    public int getTotalCount() {
        JSONArray json = getJSON();
        return json.length();
    }

    @Override
    public Object getResult() {
        return getBuild().getResult();
    }
    
    public ArrayList<JSONObject> getReportList(){
        JSONArray json = getJSON();
        ArrayList<JSONObject> reportList = new ArrayList<JSONObject>();
        
        for(int i = 0; i < json.length(); i++){
            Long endTime = json.getJSONObject(i).getLong("endTime");
            Long startTime = json.getJSONObject(i).getLong("startTime");
            Long duration = endTime - startTime;
            Date date = new Date(duration);
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String dateString = sdf.format(date);
            json.getJSONObject(i).put("duration", dateString);
            reportList.add(json.getJSONObject(i));
        }
        
        return reportList;
    }
    
}
