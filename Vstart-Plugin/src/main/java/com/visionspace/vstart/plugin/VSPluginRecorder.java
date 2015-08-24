/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
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
    public Action getProjectAction(AbstractProject<?, ?> project){
        return new VSPluginProjectAction(project);
    }
    
    @Override
     public boolean perform(AbstractBuild build, Launcher launcher, final BuildListener listener){
     
        Result buildResult = build.getResult();
                
        if (!Result.SUCCESS.equals(buildResult)) {
            // Don't process for unsuccessful builds
            listener.getLogger().println("Build status is not SUCCESS (" + build.getResult().toString() + ").");
            return false;
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
