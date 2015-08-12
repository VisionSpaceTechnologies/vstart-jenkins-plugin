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
import hudson.FilePath;
import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.remoting.VirtualChannel;
import hudson.util.ListBoxModel;
import java.io.BufferedReader;
import java.io.File;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.client.HttpResponseException;
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
    private long testCase;

    /**
     * VSPluginBuilder Constructor
     *
     * @param String vstAddress - web address of the vstart server
     * @param String credentialsId - Id assigned to each credential in the
     * credentials plugin
     * @throws URISyntaxException
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

    public void setVstProjectId(long vstProjectId) {
        this.vstProjectId = vstProjectId;
    }

    @JavaScriptMethod
    public long getTestCase() {
        return this.testCase;
    }

    public void setTestCase(long testCase) {
        this.testCase = testCase;
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        
        org.json.JSONObject runObject = null; //There are two types of JSONObject in conflict throughout the code
        org.json.JSONObject logger = null;
        Object obj = new Object();
        long timeInterval = 2000;

        //       VST Report!!
        String root = build.getWorkspace().toString();
        FilePath ws = new FilePath(build.getWorkspace(), root + "/VSTART/");

        if (!ws.exists()) {
            ws.mkdirs();
        }
        String path = ws.toString();
        String jobName = path + "/VSTREPORT_" + build.getId();
        PrintWriter wp = new PrintWriter(jobName + ".html", "UTF-8");
        wp.println("Info JOB " + build.getBuiltOnStr() + " on build "+ build.getId() + "\n");
 
        wp.close();
        
        FilePath jPath = new FilePath(build.getWorkspace(), root + "/VSTART_JSON");
        if (!jPath.exists()) {
            jPath.mkdirs();
        }
        
        PrintWriter wj = new PrintWriter(jPath + "/VSTART_JSON_" + 
                                                build.getId() + ".json");
        
        //gets dummy file
        Path file = FileSystems.getDefault().getPath("/home/pmarinho/Repos/vstart-plugin/Vstart-Plugin/src/main/resources/com/visionspace/vstart/plugin/VSPluginBuilder/", "newjson.json");
        byte[] fileArray;
        fileArray = Files.readAllBytes(file);
        String str = new String(fileArray, Charset.defaultCharset());
        
        //prints dummy json file into project workspace
        wj.println(str);
        
        try {
            Vstart vst = getDescriptor().getVst();
//            listener.getLogger().println(" Does this run? Answer: " + vst.canRun(testCase));
//            listener.getLogger().println("This is my Project ID: " + vstProjectId + "\n And this is my TestCase ID: " + testCase);
            
            runObject = vst.run(testCase);
            logger = vst.getLog(runObject.getLong("reportId"), 0l);
            
            synchronized(obj){
                while(!logger.getBoolean("finished")) {
                    long timeStamp = 0;
                    JSONArray jArray = logger.getJSONArray("log");
                    for(int i = 0; i < jArray.length(); i++){
                        org.json.JSONObject json = jArray.getJSONObject(i);
                        listener.getLogger().println(json.getString("level") +
                                " " + json.getLong("timestamp") + " [" + 
                                json.getString("resource") + "]" + " - " +
                                json.getString("message"));

                        //Stores the last timestamp
                        if(i == jArray.length() - 1){
                            timeStamp = json.getLong("timestamp");
                        }
                    }    
                        if(!logger.getBoolean("finished")){
                            obj.wait(timeInterval);
                            logger = vst.getLog(runObject.getLong("reportId"), timeStamp);
                        } else {
                            break;
                        }
                    }
            }    
            //Closing
            vst.close();

        } catch (URISyntaxException ex) {
            Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }

        wj.close();
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

        public int incSeconds = 0;
        
        private String vstAddress;
        private String vstUser;
        private String vstPass;
        private boolean stat;
        private String credentialsId;
        private transient Vstart vst;
        private int randomId;

        public Descriptor() {
            load();
            //TODO: make vst transient
            vst = null;
            //end.
            this.stat = false;
        }

        @Override
        public String getDisplayName() {
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

            try {
                if (vst == null) {
                    setVst(new Vstart(this.vstAddress, this.vstUser, this.vstPass));
                } else {
                    vst.login(vstUser, vstPass);
                }

                this.stat = true;
                vst.close();
            } catch (URISyntaxException ex) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
                this.stat = false;
            } catch (IOException ex) {
                Logger.getLogger(VSPluginBuilder.class.getName()).log(Level.SEVERE, null, ex);
                this.stat = false;
            }

            return this.stat;
        }

        public String getCredentialsId() {
            return this.credentialsId;
        }

        public Vstart getVst() {
            try {
                if (vst == null) {
                    setVst(new Vstart(this.vstAddress, this.vstUser, this.vstPass));
                } else {
                    vst.login(vstUser, vstPass);
                }
            } catch (Exception e) {
                System.err.println("EXCEPTION while logging into server:" + e.getMessage());
                e.printStackTrace();
            }

            return vst;
        }

        public void setVst(Vstart vst) {
            this.vst = vst;
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

        public void setStat(boolean stat) {
            this.stat = stat;
        }
         
//        @JavaScriptMethod
//        public void setVstProjectId(long vstProjectid) {
//            this.vstProjectId = vstProjectid;
//        }
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

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            // Indicates that this builder can be used with all kinds of project types
            return FreeStyleProject.class.isAssignableFrom(jobType);
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
            vst = null;
            save();
            return super.configure(req, formData);
        }

        @Override
        public VSPluginBuilder newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
            return req.bindJSON(VSPluginBuilder.class, formData);
        }

        public ListBoxModel doFillVstProjectIdItems() throws IOException {

            try {
                vst.login(vstUser, vstPass);
                ListBoxModel items = new ListBoxModel();
                JSONArray array = vst.listUserProjects();
                vst.close();
                
                for (int j = 0; j < array.length(); j++) {
                    String project = array.getJSONObject(j).getString("name");
                    String id = Long.toString(array.getJSONObject(j).getLong("id"));
                    items.add(new ListBoxModel.Option(project, id, false));
                }
                
                return items;

            } catch (URISyntaxException ex) {

                ListBoxModel items = new ListBoxModel();
                this.stat = false;
                ex.printStackTrace();
                
                return items;
            } catch (HttpResponseException exc)   {

                ListBoxModel items = new ListBoxModel();
                this.stat = false;
                exc.printStackTrace();
                
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

        public ListBoxModel doFillTestCaseItems(@QueryParameter("vstProjectId") final long vstProjectId) {

            try {
                
                vst.login(vstUser, vstPass);
                ListBoxModel items = new ListBoxModel();
                JSONArray array = vst.listProjectTestCases(vstProjectId);

                for (int j = 0; j < array.length(); j++) {
                    String testcase = array.getJSONObject(j).getString("name");
                    String id = Long.toString(array.getJSONObject(j).getLong("id"));
                    if ( id.equals(Long.toString(vstProjectId)) ) {
                        items.add(new ListBoxModel.Option(testcase, id, true));
                    } else {
                        items.add(new ListBoxModel.Option(testcase, id, false));
                    }
                }
                vst.close();
                return items;

            } catch (IOException e) {

                ListBoxModel items = new ListBoxModel();
                FormValidation.error("Error retrieving testcases. Please try again.");
                return items;

            } catch (URISyntaxException ex) {

                ListBoxModel items = new ListBoxModel();
                FormValidation.error("Error retrieving testcases. Please try again.");
                return items;
            }
        }
    }

}
