package com.visionspace.vstart.plugin;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import static com.google.common.collect.Lists.newArrayList;
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
import hudson.model.Job;
import hudson.util.ListBoxModel;


import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import jenkins.model.Jenkins;
import org.json.JSONArray;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * Vstart Plugin Builder
 *
 * @author pedro.marinho
 */
public class VSPluginBuilder extends Builder {

    private final String vstAddress;
    private final String credentialsId;
    private long vstProjectId;
    private long vstTestId;

    /**
     * VSPluginBuilder Constructor
     *
     * @param String vstAddress - web address of the vstart server
     * @param String credentialsId - Id assigned to each credential in the
     * credentials plugin
     * @throws URISyntaxException
     */
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
//    @DataBoundConstructor
//    public VSPluginBuilder(String vstAddress, String credentialsId, long vstProjectId, int vstTestId) throws URISyntaxException {
//
//        this.vstAddress = vstAddress;
//        this.credentialsId = credentialsId;
//        this.vstProjectId = vstProjectId;
//        this.vstTestId = vstTestId;
//    }
    
    @DataBoundConstructor
    public VSPluginBuilder() throws URISyntaxException {

        this.vstAddress = getDescriptor().getVstAddress();
        this.credentialsId = getDescriptor().getCredentialsId();
        this.vstProjectId = getDescriptor().getVstProjectId();
        this.vstTestId = getDescriptor().getVstTestId();
    }

    public String getVstAddress() {
        return vstAddress;
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
        return (Descriptor) super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static class Descriptor extends BuildStepDescriptor<Builder> {

        private String vstAddress;
        private String vstUser;
        private String vstPass;
        private boolean stat;
        private String credentialsId;
        private long vstProjectId;     
        private long vstTestId;
        
        public Descriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            //teste
            try {
                Vstart vst = new Vstart(this.vstAddress, this.vstUser, this.vstPass);
                this.stat = true;

            } catch (IOException e) {

                this.stat = false;

            } catch (URISyntaxException ex) {

                this.stat = false;

            }

            return "Execute VSTART tasks.";
        }

            
        public String getVstAddress() {
            return this.vstAddress;
        }

        
        public String getVstUser() {
            return this.vstUser;
        }

        public String getVstPass() {
            return this.vstPass;
        }

        public boolean getStat() {
            return this.stat;
        }

        public long getVstProjectId() {
            return this.vstProjectId;
        }

        public String getCredentialsId() {
            return this.credentialsId;
        }

        public long getVstTestId() {
            return vstTestId;
        }

        @JavaScriptMethod
        public void setVstTestId(long vstTestId) {
            this.vstTestId = vstTestId;
        }
        
        public void setVstAddress(String s) {
            this.vstAddress = s;
        }

        public void setVstUser(String user) {
            this.vstUser = user;
        }

        public void setVstPass(String pass) {
            this.vstPass = pass;
        }

        public void setCredentialsId(String credentialsId) {
            this.credentialsId = credentialsId;
        }

        public void setVstProjectId(long id) {
            this.vstProjectId = id;
        }

        /**
         * Tests the validity of an URL
         *
         * @param nUrl
         * @return true if valid, false if not valid
         */
        public boolean isValidURL(String nUrl) {
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

            if (this.isValidURL(address)) {
                return FormValidation.ok("Success!");
            } else {
                return FormValidation.error("Please insert a valid URL!");
            }

        }

          //Removed because of the integration of the Credentials plugin   
//        public FormValidation doCheckLogin(@QueryParameter("vstAddress") final String address , 
//                @QueryParameter("vstUser") final String user, @QueryParameter("vstPass") final String pass) 
//                throws URISyntaxException, IOException
//        {
//            try {   
//                Vstart v = new Vstart(address, user, pass);
//                return FormValidation.ok("Valid credentials!");
//            } catch(HttpResponseException e) {    
//                return FormValidation.error("Login failed!");
//            } catch(IOException ex){
//                return FormValidation.error("Login failed!");
//            }
//        }
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project types
            return FreeStyleProject.class.isAssignableFrom(jobType);
        }

        ;
        
        @JavaScriptMethod
        public int add(int x, int y) {
            return x + y;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            req.bindJSON(this, formData);
            List<DomainRequirement> domainRequirements = newArrayList();
            List<StandardUsernamePasswordCredentials> c = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.getInstance(), null, domainRequirements);
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            for (int i = 0; i < c.size(); i++) {
                if (c.get(i).getId().equals(formData.getString("credentialsId"))) {
                    setVstUser(c.get(i).getUsername());
                    setVstPass(c.get(i).getPassword().getPlainText());
                    break;
                }
            }
//            setVstProjectId(Long.valueOf(formData.getString("vstProjectId")));
            save();
            return super.configure(req, formData);
        }

        public ListBoxModel doFillVstProjectIdItems() {

            try {
                Vstart vst = new Vstart(this.vstAddress, this.vstUser, this.vstPass);
                this.stat = true;
                ListBoxModel items = new ListBoxModel();

                for (int j = 0; j < vst.listUserProjects().length(); j++) {
                    String project = vst.listUserProjects().getJSONObject(j).getString("name");
                    String id = Long.toString(vst.listUserProjects().getJSONObject(j).getLong("id"));
                    items.add(new ListBoxModel.Option(project, id));
                }
                return items;

            } catch (IOException e) {

                ListBoxModel items = new ListBoxModel();
                this.stat = false;
                return items;

            } catch (URISyntaxException ex) {

                ListBoxModel items = new ListBoxModel();
                this.stat = false;
                return items;
            }
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Job<?, ?> owner) {
            if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) { // or whatever permission is appropriate for this page
                // Important! Otherwise you expose credentials metadata to random web requests.
                return new ListBoxModel();
            }

            List<DomainRequirement> domainRequirements = newArrayList();

            return new StandardUsernameListBoxModel().withEmptySelection().withAll(
                    CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, owner, null, domainRequirements));
        }

        public ListBoxModel doFillTestCaseItems() {

            try {
                Vstart vst = new Vstart(this.vstAddress, this.vstUser, this.vstPass);
                ListBoxModel items = new ListBoxModel();

                for (int j = 0; j < vst.listProjectTestCases(this.vstProjectId).length(); j++) {
                    String project = vst.listProjectTestCases(this.vstProjectId).getJSONObject(j).getString("name");
                    String id = vst.listProjectTestCases(this.vstProjectId).getJSONObject(j).getString("name");
                    items.add(project, id);
                }
                return items;

            } catch (IOException e) {

                ListBoxModel items = new ListBoxModel();
                return items;

            } catch (URISyntaxException ex) {

                ListBoxModel items = new ListBoxModel();
                return items;
            }
        }
        
//        @JavaScriptMethod
//        public ListBoxModel doFillTestCaseItems(String tests){
//            JSONArray json = new JSONArray(tests);
//            ListBoxModel items = new ListBoxModel();
//            
//            for(int i = 0; i < json.length(); i++){
//                String test = json.getJSONObject(i).getString("name");
//                String id = json.getJSONObject(i).getString("id");
//                items.add(test, id);        
//            }
//            
//            return items;
//        }
        
        @JavaScriptMethod
        public String getTestCases(int id) throws URISyntaxException, IOException{
            Vstart vst = new Vstart(this.vstAddress, this.vstUser, this.vstPass);
            JSONArray array = vst.listProjectTestCases(id);
            setVstProjectId(id);
            JSONObject json = new JSONObject();
                        
            return array.toString();
        }
        
    }

}
