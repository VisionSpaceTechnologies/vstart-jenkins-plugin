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

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolProperty;
import hudson.util.FormValidation;
import java.io.IOException;
import java.util.List;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author pedro.marinho
 */
public class VstartInstallation extends ToolInstallation implements EnvironmentSpecific<VstartInstallation>, NodeSpecific<VstartInstallation> {

    @DataBoundConstructor
    public VstartInstallation(String name, String home, List<? extends ToolProperty<?>> properties){
        super(name, home, properties);
    }
    
    @Override
    public VstartInstallation forEnvironment(EnvVars environment) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VstartInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Extension
    public static class DescriptorImpl extends ToolDescriptor<VstartInstallation> {
        
        private volatile VstartInstallation[] installations = new VstartInstallation[0];
        
        public DescriptorImpl(){
            load();
        }
        
        @Override
        public String getDisplayName() {
            return "VSTART"; //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public VstartInstallation[] getInstallations(){
            return installations;
        }
        
        @Override
        public void setInstallations(VstartInstallation[] installations){
            this.installations = installations;
            save();
        }
        
        public FormValidation doCheckHome(){
            //TODO: integrate with VSTART API method for validation of vstart addresses
            return FormValidation.ok("This is a valid server.");
        }
    
    }
    
}
