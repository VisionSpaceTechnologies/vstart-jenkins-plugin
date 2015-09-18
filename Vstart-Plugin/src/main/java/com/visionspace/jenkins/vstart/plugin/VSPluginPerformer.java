/* 
 * Copyright (C) 2015 VisionSpace Technologies, Lda.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.visionspace.jenkins.vstart.plugin;

import com.visionspace.vstart.api.Vstart;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginPerformer {

    private final Vstart vstObject; //Vstart object cannot be changed throughout a VSPluginPerformer instance's lifetime

    public VSPluginPerformer(Vstart vst) {
        this.vstObject = vst;
    }
    
    public VSPluginPerformer(){
        this.vstObject = null;
    }

    public Vstart getVstObject() {
        return vstObject;
    }

    public void addBuildAction(AbstractBuild build) {
        VSPluginBuildAction buildAction = build.getAction(VSPluginBuildAction.class);
        if (buildAction == null) {
            buildAction = new VSPluginBuildAction(build);
            build.addAction(buildAction);
        }
    }

    public boolean validateTestCase(Long id, AbstractBuild build, BuildListener listener) {
        try {
            if (!vstObject.canRun(id)) {
                listener.getLogger().println("This job can't be run at the moment. [JOB: " + build.getProject().getName() + " BUILD NO " + build.getNumber() + "]);");
                return false;
            } else {
                return true;
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Exception during the test case validation -> " + ex.getReason());
            return false;
        } catch (IOException ex) {
            Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Exception during the VSTART run! -> " + ex.getLocalizedMessage());
            return false;
        }
    }

    public Long runVstart(Long id, BuildListener listener, int timeInterval) {
        try {
            //Run VSTART
            org.json.JSONObject runObject = vstObject.run(id);
            listener.getLogger().println("\nVSTART is now running!");
            Long reportId = runObject.getLong("reportId");
            
            //Inform which test case is running
            //listener.getLogger().println("The test case " + vstObject.getTestCase(testCaseId) + "has started.");
            
            //For concurrency issues
            Object obj = new Object();
            
            synchronized (obj) {
                long timeStamp = 0;
                org.json.JSONObject logger = null;
                
                do {
                    logger = vstObject.getLog(reportId, timeStamp);
                    JSONArray jArray = logger.getJSONArray("log");
                    for (int i = 0; i < jArray.length(); i++) {
                        org.json.JSONObject json = jArray.getJSONObject(i);
                        Long eventTimeStamp = json.getLong("timestamp");
                        Date date = new Date(eventTimeStamp);
                        listener.getLogger().println(json.getString("level")
                                + " " + /*eventTimeStamp*/date + " ["
                                + json.getString("resource") + "]" + " - "
                                + json.getString("message") + "\n");
                        
                        //Stores the latest timestamp
                        if (eventTimeStamp > timeStamp) {
                            timeStamp = eventTimeStamp;
                        }
                    }
                    if (!logger.getBoolean("finished")) {
                        obj.wait(timeInterval);
                    }
                } while (!logger.getBoolean("finished"));
            }
            
            //inform the finishing of the test case execution
            //listener.getLogger().println("The test case " + vstObject.getTestCase(testCaseId) + "has finished its execution.");
            
            //Finished run
            listener.getLogger().println("VSTART run has now ended!");
            
            return reportId;
        } catch (URISyntaxException ex) {
            Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Exception during the VSTART run! -> " + ex.getReason());
            return 0l;
        } catch (IOException ex) {
            Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Exception during the VSTART run! -> " + ex.getMessage());
            return 0l;
        } catch (InterruptedException ex) {
            Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Exception during the VSTART run! -> " + ex.getMessage());
            return 0l;
        }
    }

    public boolean logToWorkspace(Long reportId, AbstractBuild build, BuildListener listener){
        try {
            org.json.JSONObject report = vstObject.getReport(reportId);
            FilePath jPath = new FilePath(build.getWorkspace(), build.getWorkspace().toString() + "/VSTART_JSON");
            if (!jPath.exists()) {
                jPath.mkdirs();
            }
            
            String filePath = jPath + "/VSTART_JSON_"
                    + build.getId() + ".json";
            
            JSONArray reports;
            try {
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                reports = new JSONArray(content);
            } catch (IOException e) {
                Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.WARNING, null, e);
                reports = new JSONArray();
            }
            
            reports.put(report);
            
            PrintWriter wj = new PrintWriter(filePath);
            wj.println(reports.toString());
            wj.close();
             
            //TODO: check test case result
            /*
            if (report.getString("status").equals("PASSED")) {          
            */
            return true;
            /* } */

        } catch (URISyntaxException ex) {
            Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Error on performing the workspace logging -> " + ex.getReason());
        } catch (IOException ex) {
            Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Error on performing the workspace logging -> " + ex.toString());
        } catch (InterruptedException ex) {
            Logger.getLogger(VSPluginPerformer.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Error on performing the workspace logging -> " + ex.getMessage());
        }
        return false;

    } 
}
