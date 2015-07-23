package com.visionspace.vstart.plugin;

import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import com.visionspace.vstart.api.Vstart;
import hudson.model.FreeStyleProject;
import hudson.util.ListBoxModel;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import org.apache.http.client.HttpResponseException;



/**
 * Vstart Plugin Builder
 * 
 * @author pedro.marinho
 */
public class VSPluginBuilder extends Builder {

    private final String vstAddress;
    private final String vstUser;
    private final String vstPass;
    
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public VSPluginBuilder(String vstAddress, String vstUser, String vstPass) throws URISyntaxException {
       
        this.vstAddress = vstAddress;
        this.vstUser = vstUser;
        this.vstPass = vstPass;
    }

    public String getVstAddress() {
        return vstAddress;
    }
    
    public String getVstUser() {
        return vstUser;
    }
    
    public String getVstPass(){
        return vstPass;
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        // This is where you 'build' the project.
        // Since this is a dummy, we just say 'hello world' and call that a build.

        // This also shows how you can consult the global configuration of the builder
       
        return true;
    }
       

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public Descriptor getDescriptor() {
        return (Descriptor)super.getDescriptor();
    }

    
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static class Descriptor extends BuildStepDescriptor<Builder> {
        
        private String vstAddress;
        private String vstUser;
        private String vstPass;
        
        @Override
        public String getDisplayName()
        {
            return "Execute VSTART tasks.";
        };
        
        public void setVstAddress(String s){
            this.vstAddress = s;
        }
        
        public void setVstUser(String u){
           this.vstUser = u;
        }
        
        public void setVstPass(String pwd){
            this.vstPass = pwd;
        }
        
        /**
         * Tests the validity of an URL
         * @param url 
         * @return true if valid, false if not valid
         */
        public boolean isValidURL(String nUrl){
            URL u = null;
            
            try {
                u = new URL(nUrl);
            } catch (MalformedURLException e) {
                return false;
            }
            try {
                u.toURI();
            } catch (URISyntaxException z) {
                return false;
            }
            return true;
        }
        
        public FormValidation doCheckAddress(@QueryParameter("vstAddress") final String address)
                throws IOException, ServletException {
                                 
            if(this.isValidURL(address))
                return FormValidation.ok("Success!");
            else return FormValidation.error("Please insert a valid URL!");
            
        }
        
             
        public FormValidation doCheckLogin(@QueryParameter("vstAddress") final String address , 
                @QueryParameter("vstUser") final String user, @QueryParameter("vstPass") final String pass) 
                throws URISyntaxException, IOException
        {
            try {   
                Vstart v = new Vstart(address, user, pass);
                return FormValidation.ok("Valid credentials!");
            } catch(HttpResponseException e) {    
                return FormValidation.error("Login failed!");
            }
        }
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return FreeStyleProject.class.isAssignableFrom(jobType);
        };

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            req.bindJSON(this, formData);
            
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }
        
        public ListBoxModel doFillProjectItems() throws URISyntaxException, IOException{
            ListBoxModel items = new ListBoxModel();
            Vstart vst = new Vstart(this.vstAddress, this.vstUser, this.vstPass);
                        
            for(int j = 0; j < vst.listProjects().length(); j++){
                String project =  vst.listProjects().getJSONObject(j).getString("title");
                items.add(project);
            }
            
            return items;
        }
        
    }
    
    
}

