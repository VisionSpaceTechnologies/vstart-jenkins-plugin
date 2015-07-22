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
import hudson.util.ListBoxModel;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.validator.UrlValidator;



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
    public VSPluginBuilder(String name, String address, String user, String pass) {
       
        this.vstAddress = address;
        this.vstUser = user;
        this.vstPass = pass;
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
    
    /**
     * Logs in with credentials received from the jenkins config page.
     * @return Vstart API object
     * @throws URISyntaxException 
     */
    public Vstart login() throws URISyntaxException{
        Vstart vst = new Vstart(vstAddress, vstUser, vstPass);
        return vst;
    };

    
    public ListBoxModel doFillProjectTypeItems(Vstart v) throws URISyntaxException {
            ListBoxModel items = new ListBoxModel();
            v = this.login();
            
            for (int i = 0; i < v.listProjects().length(); i++) {
                items.add(v.listProjects().getString(i));
            }
            return items;
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
        
        @Override
        public String getDisplayName()
        {
            return "Execute VSTART tasks.";
        };
        
        public FormValidation doCheckAddress(@QueryParameter String address)
                throws IOException, ServletException {
            UrlValidator urlVal = new UrlValidator();
            
            if (urlVal.isValid(address))
                return FormValidation.ok();
            else return FormValidation.error("Please insert a valid URL!");
            
        }
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        };

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        
    }
    
    
}

