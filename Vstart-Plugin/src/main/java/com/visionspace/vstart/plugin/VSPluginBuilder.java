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
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import com.visionspace.vstart.api.Vstart;
import hudson.model.Job;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.HttpResponseException;
import org.kohsuke.stapler.AncestorInPath;

/**
 * Vstart Plugin Builder
 *
 * @author pedro.marinho
 */
public class VSPluginBuilder extends Builder {

    private final String vstAddress;
    private final String credentialsId;
    private final long vstProjectId;
    private final long testCase;

    /**
     * VSPluginBuilder Constructor
     *
     * @param vstAddress
     * @param credentialsId
     * @param vstProjectId
     * @param testCase
     */
    @DataBoundConstructor
    public VSPluginBuilder(String vstAddress, String credentialsId, long vstProjectId, long testCase) {
        this.vstAddress = vstAddress;
        this.credentialsId = credentialsId;
        this.vstProjectId = vstProjectId;
        this.testCase = testCase;
    }

    public String getVstAddress() {
        return vstAddress;
    }

    public long getVstProjectId() {
        return vstProjectId;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public long getTestCase() {
        return testCase;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        try {
            //get user and password
            JSONObject json = Descriptor.getCredentialsById(credentialsId);
            String user = json.getString("user");
            String pass = json.getString("pass");

            //Informing the start of the perform method
            listener.getLogger().println("\nA VSTART buildstep has started.");

            //Informing connection
            listener.getLogger().println("\nTrying to connect to the VSTART server...");

            //Instanciation of VSTART API object & login
            Vstart vst = new Vstart(vstAddress, user, pass);
            VSPluginPerformer performer = new VSPluginPerformer(vst);
            
            //Informing success on connection
            listener.getLogger().println("Connection established with the VSTART server.");

//            //add action
//            performer.addBuildAction(build); >>> DONE IN PUBLISHER

            //test case validation
            boolean test = performer.validateTestCase(testCase, build, listener);

            //In case of validation failure -> Build cannot 
            if (!test) {
                performer.getVstObject().close();
                return false;
            }

            //Run VSTART
            int timeInterval = 2000; //wait 2 seconds to ask for a "finished" test case status 
            Long reportId = performer.runVstart(testCase, listener, timeInterval);

            //run status validation
            if (reportId == 0l) {
                performer.getVstObject().close();
                return false;
            }

            //log JSON file to workspace
            boolean result = performer.logToWorkspace(reportId, build, listener);

            //close VSTART session
            performer.getVstObject().close();

            //Logging to console
            listener.getLogger().println("VSTART buildstep has ended with success.");

            return result;

        } catch (URISyntaxException ex) {
            Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
            listener.getLogger().println("Impossible to reach VSTART server!");
        }

        return false;
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

        public transient VstartInstallation[] servers;

        public Descriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Execute VSTART tasks.";
        }

        public void setServers(VstartInstallation[] vServers) {
            this.servers = null;
            this.servers = vServers;
        }

        public FormValidation doCheckLogin(@QueryParameter("credentialsId") final String credentials, @QueryParameter("vstAddress") final String address) {

            JSONObject json = getCredentialsById(credentials);
            String user = json.getString("user");
            String pass = json.getString("pass");

            Vstart vstObject;
            try {
                vstObject = new Vstart(address, user, pass);
                //vstObject.login(user, pass);
                vstObject.close();
                return FormValidation.ok("Login: OK!");
            } catch (URISyntaxException ex) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
                return FormValidation.error("Login: error!");
            } catch (IOException e) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, e);
                return FormValidation.error("Login: error!");
            }
        }

        private synchronized JSONArray getProjects(String vstAddress, String vstUser, String vstPass) {
            try {

                Vstart api = new Vstart(vstAddress, vstUser, vstPass);

                JSONArray jArr = api.listUserProjects();
                api.close();
                return jArr;
           
            } catch (URISyntaxException ex) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
                return new JSONArray();
            } catch (HttpResponseException e ) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, e);
                return new JSONArray();
            } catch (IOException ex) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
                return new JSONArray();
            }
        }

        private boolean isValidProjectId(long id, String vstAddress, String vstUser, String vstPass) {

            JSONArray jArr = getProjects(vstAddress, vstUser, vstPass);

            for (int i = 0; i < jArr.length(); i++) {
                long pId = jArr.getJSONObject(i).getLong("id");
                if (pId == id) {
                    return true;
                }
            }

            return false;
        }

        public FormValidation doCheckVstProjectId(@QueryParameter("vstProjectId") final long id, @QueryParameter("vstAddress") final String address, @QueryParameter("credentialsId") final String credId) {
            if (id == 0l) {
                return FormValidation.error("It's not possible to retrieve projects. Please select a server and the user credentials.");
            }

            JSONObject json = getCredentialsById(credId);

            String user = json.getString("user");
            String pass = json.getString("pass");

            if (isValidProjectId(id, address, user, pass)) {
                return FormValidation.ok("Available project.");
            } else {
                return FormValidation.error("This project does not exist or it is not available at the current time.");
            }
        }

        private static JSONObject getCredentialsById(String id) {
            String user = new String();
            String pass = new String();
            List<DomainRequirement> domainRequirements = newArrayList();
            List<StandardUsernamePasswordCredentials> c = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, Jenkins.getInstance(), null, domainRequirements);

            for (int i = 0; i < c.size(); i++) {
                if (c.get(i).getId().equals(id)) {
                    user = c.get(i).getUsername();
                    pass = c.get(i).getPassword().getPlainText();
                    break;
                }
            }

            JSONObject json = new JSONObject();
            json.put("user", user);
            json.put("pass", pass);

            return json;
        }

        private synchronized boolean isValidTestCase(String address, String user, String pass, long id) {
            try {
                
                Vstart api = new Vstart(address, user, pass);
                boolean test = api.canRun(id);
                api.close();
                return test;
            } catch (IOException e) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, e);
                return false;
            } catch (URISyntaxException ex) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

        public FormValidation doCheckTestCase(@QueryParameter("testCase") final long id, @QueryParameter("vstAddress") final String address, @QueryParameter("credentialsId") final String credId) {
            if (id == 0l) {
                return FormValidation.error("There are no test cases to display, please select another project or make sure that you are logged in.");
            }
            
            JSONObject json = getCredentialsById(credId);

            String user = json.getString("user");
            String pass = json.getString("pass");
            
            boolean test = isValidTestCase(address, user, pass, id);
            if (test) {
                return FormValidation.ok("Test Case Available.");
            } else {
                return FormValidation.error("This test case is not available at the time, please select another.");
            }
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            req.bindJSON(this, formData);
            save();
            return super.configure(req, formData);
        }

        @Override
        public VSPluginBuilder newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            return req.bindJSON(VSPluginBuilder.class, formData);
        }

        public ListBoxModel doFillVstProjectIdItems(@QueryParameter("vstAddress") final String address,
                @QueryParameter("credentialsId") final String credId) throws IOException {

            JSONObject json = getCredentialsById(credId);
            String user = json.getString("user");
            String pass = json.getString("pass");
            JSONArray array = getProjects(address, user, pass);
            //validation
            if (array.isNull(0)) {
                return new ListBoxModel();
            }
            ListBoxModel items = new ListBoxModel();

            for (int j = 0; j < array.length(); j++) {
                String project = array.getJSONObject(j).getString("name");
                String id = Long.toString(array.getJSONObject(j).getLong("id"));
                items.add(new ListBoxModel.Option(project, id, false));
            }

            return items;

        }

        public ListBoxModel doFillVstAddressItems(@AncestorInPath Job<?, ?> owner) {
            if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) { // or whatever permission is appropriate for this page
                // Important! Otherwise you expose credentials metadata to random web requests.
                return new ListBoxModel();
            }

            if (servers == null) {
                return new ListBoxModel();
            }

            ListBoxModel items = new ListBoxModel();

            for (int i = 0; i < servers.length; i++) {
                String name = servers[i].getName();
                String address = servers[i].getHome();
                items.add(name, address);
            }

            return items;
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

        private synchronized JSONArray getTestCases( String address, String user, String pass, long vstProjectId) {

            try {
                Vstart api = new Vstart(address, user, pass);
                JSONArray array = api.listProjectTestCases(vstProjectId);
                api.close();
                return array;
            } catch (IOException e) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, e);
                return new JSONArray();
            } catch (URISyntaxException ex) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
                return new JSONArray();
            }
        }

        public ListBoxModel doFillTestCaseItems(@QueryParameter("vstAddress") final String address,
                                        @QueryParameter("credentialsId") final String credId,
                                        @QueryParameter("vstProjectId") final long vstProjectId) 
        {
            JSONObject json = getCredentialsById(credId);
            String user = json.getString("user");
            String pass = json.getString("pass");
            JSONArray array = getTestCases(address, user, pass, vstProjectId);
            //validation
            if (array == null) {
                return new ListBoxModel();
            }
            ListBoxModel items = new ListBoxModel();

            for (int j = 0; j < array.length(); j++) {
                String testcase = array.getJSONObject(j).getString("name");
                String id = Long.toString(array.getJSONObject(j).getLong("id"));
                if (id.equals(Long.toString(vstProjectId))) {
                    items.add(new ListBoxModel.Option(testcase, id, true));
                } else {
                    items.add(new ListBoxModel.Option(testcase, id, false));
                }
            }
            return items;
        }

        public VstartInstallation[] getInstallations() {
            return Jenkins.getInstance().getDescriptorByType(VstartInstallation.DescriptorImpl.class).getInstallations();
        }
    }
}
