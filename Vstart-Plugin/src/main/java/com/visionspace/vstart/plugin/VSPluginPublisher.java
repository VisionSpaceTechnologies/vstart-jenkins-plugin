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
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginPublisher extends Publisher {

    @DataBoundConstructor
    public VSPluginPublisher() {

    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new VSPluginProjectAction(project);
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, final BuildListener listener) {

        Result buildResult = build.getResult();

        if (!Result.SUCCESS.equals(buildResult)) {
            // Don't process for unsuccessful builds
            listener.getLogger().println("Build status is not SUCCESS (" + build.getResult().toString() + ").");
            return false;
        }

        try {

            //DO REPORT HERE
            FilePath jPath = new FilePath(build.getWorkspace(), build.getWorkspace() + "/VSTART_JSON");

            if (!jPath.exists()) {
                return false;
            }

            String filePath = jPath + "/VSTART_JSON_"
                    + build.getId() + ".json";
            
            //read .json file
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONArray reports = new JSONArray(content);
            
            //Generate html report
            VSPluginHtmlWriter htmlWriter = new VSPluginHtmlWriter();
            boolean reportResult = htmlWriter.doHtmlReport(build, reports);
            
            return reportResult;
            
        } catch (IOException ex) {
            Logger.getLogger(VSPluginPublisher.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InterruptedException ex) {
            Logger.getLogger(VSPluginPublisher.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return (BuildStepDescriptor) super.getDescriptor();
    }

    @Override
    public boolean needsToRunAfterFinalized() {
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
