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
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "vstart";
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
