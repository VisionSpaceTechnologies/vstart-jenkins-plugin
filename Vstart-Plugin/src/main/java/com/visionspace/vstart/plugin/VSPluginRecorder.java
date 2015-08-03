/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginRecorder extends Recorder {

    @DataBoundConstructor
    public VSPluginRecorder(){
    
    }
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }
    
    @Override
     public boolean perform(AbstractBuild build, Launcher launcher, final BuildListener listener){
         
        FilePath ws = build.getWorkspace();
        
        if( ws == null){
            listener.getLogger().println("Workspace is unreachable.");
            return false;
        }
        
        Result buildResult = build.getResult();

        if (!Result.SUCCESS.equals(buildResult)) {
            // Don't process for unsuccessful builds
            listener.getLogger().println("Build status is not SUCCESS (" + build.getResult().toString() + ").");
            return true;
        }
        
        String spacename = "/VSTART/";
        FilePath vs = new FilePath(ws, spacename);
        
        try {
            if(!vs.exists()){
                vs.mkdirs();
            }
        } catch (IOException ex) {
            Logger.getLogger(VSPluginRecorder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(VSPluginRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            spacename = build.getEnvironment(listener).expand("VSTART");
        } catch (IOException ex) {
            Logger.getLogger(VSPluginRecorder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(VSPluginRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
     }
     
    @Override
     public BuildStepDescriptor getDescriptor(){
         return super.getDescriptor();
     }
     
    @Override
     public boolean needsToRunAfterFinalized(){
         return true;
     }
     
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static class DescriptorRecorder extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return FreeStyleProject.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return "VSTART Publisher.";
        }
    
    }
}
