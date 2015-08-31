/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.tasks.test.AbstractTestResultAction;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginBuildAction extends AbstractTestResultAction {

    private final AbstractBuild build;

    public VSPluginBuildAction(AbstractBuild build) {
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

    public AbstractBuild getBuild() {
        return this.build;
    }

    public JSONArray getJSON() {
        String content;
        try {
            FilePath jPath = new FilePath(build.getWorkspace(), build.getWorkspace() + "/VSTART_JSON");
            String filePath = jPath + "/VSTART_JSON_"
                    + build.getId() + ".json";

            //read .json file
            File jsonFile = new File(filePath);
            InputStream is = new FileInputStream(jsonFile); 
            
            content = IOUtils.toString(is, StandardCharsets.UTF_8);

            JSONArray json = new JSONArray(content);
            return json;
        } catch (IOException ex) {
            Logger.getLogger(VSPluginPublisher.class.getName()).log(Level.SEVERE, null, ex);
            return new JSONArray();
        } catch (JSONException ex) {
            System.out.println("");
            return new JSONArray();
        }

    }

    @Override
    public int getFailCount() {
        JSONArray json = getJSON();
        int fails = 0;

        for (int i = 0; i < json.length(); i++) {
            JSONArray jSteps = json.getJSONObject(i).getJSONArray("steps");
            for (int j = 0; j < jSteps.length(); j++) {
                if (jSteps.getJSONObject(j).getString("status").equals("FAILED")) {
                    fails++;
                }
            }
        }

        return fails;
    }

    @Override
    public int getTotalCount() {
        JSONArray json = getJSON();
        int total = 0;

        for (int i = 0; i < json.length(); i++) {
            JSONArray jSteps = json.getJSONObject(i).getJSONArray("steps");
            total += jSteps.length();
        }
        return total;
    }

    @Override
    public Object getResult() {
        return getBuild().getResult();
    }

    public HashMap< String, ArrayList<JSONObject>> getReportMap() {
        JSONArray json = getJSON();
        HashMap<String, ArrayList<JSONObject>> reportMap = new HashMap<String, ArrayList<JSONObject>>();

        for (int i = 0; i < json.length(); i++) {
            String testCaseName = json.getJSONObject(i).getString("testCaseName");
            JSONArray thisStep = json.getJSONObject(i).getJSONArray("steps");
            ArrayList<JSONObject> steps = new ArrayList<JSONObject>();

            for (int j = 0; j < thisStep.length(); j++) {
                Long endTime = thisStep.getJSONObject(j).getLong("endTime");
                Long startTime = thisStep.getJSONObject(j).getLong("startTime");
                Long duration = endTime - startTime;
                Date date = new Date(duration);
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                String dateString = sdf.format(date);
                thisStep.getJSONObject(j).put("duration", dateString);
                steps.add(thisStep.getJSONObject(j));
            }
            reportMap.put(testCaseName, steps);
        }

        return reportMap;
    }

}
