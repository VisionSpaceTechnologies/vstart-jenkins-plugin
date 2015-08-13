/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.test.TestResultProjectAction;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginProjectAction extends TestResultProjectAction {

    private final AbstractProject project;

    public VSPluginProjectAction(AbstractProject<?, ?> project){
        super(project);
        this.project = project;
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
        return null;
    }

    public AbstractProject getProject() {
        return this.project;
    }

    public AbstractBuild getLastFinishedBuild() {
        AbstractBuild lastBuild = this.project.getLastBuild();

        while (lastBuild != null && lastBuild.isBuilding()) {
            lastBuild = lastBuild.getPreviousBuild();
        }

        return lastBuild;
    }
}
