/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.FilePath;
import hudson.Functions;
import hudson.model.AbstractBuild;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author pedro.marinho
 */
public class VSPluginHtmlWriter {

    public VSPluginHtmlWriter() {
    }

    public boolean doHtmlReport(AbstractBuild build, JSONObject jsonReport) {
        try {
            if (jsonReport == null) {
                return false;
            }

            FilePath hPath = new FilePath(build.getWorkspace(), build.getWorkspace().toString() + "/VSTART_HTML");
            if (!hPath.exists()) {
                hPath.mkdirs();
            }
            JSONArray jSteps = jsonReport.getJSONArray("steps");

            PrintWriter wp = new PrintWriter(hPath + "/VSTART_REPORT_" + build.getNumber() + ".html");
            StringBuilder builder = new StringBuilder();

            builder.append("<!DOCTYPE html>").append("\n");
            builder.append("<html>").append("\n");
            builder.append("<head><title>Vstart Report</title></head>").append("\n");
            builder.append("<body><h1>Vstart Report</h1>").append("\n");
            //Graph
            builder.append("<div id='graph' style='display: block; width: 800px; height: 600px;'></div>");
            builder.append("<script type='text/javascript'>").append("\n");
            builder.append("data=").append(new JSONObject(jsonReport.getString("extendedGraph")).toString()).append("\n");
            builder.append("pathPrefix=").append(" '" + Jenkins.getInstance().getRootUrl()).append("' \n");
            builder.append("</script>").append("\n");
            builder.append("<script type='text/javascript' src='https://cdnjs.cloudflare.com/ajax/libs/cytoscape/2.4.6/cytoscape.js'></script>").append("\n");
            builder.append("<script type='text/javascript' src='" + Jenkins.getInstance().getRootUrl() + "plugin/Vstart-Plugin/dagre.js'></script>").append("\n");
            builder.append("<script src='" + Jenkins.getInstance().getRootUrl() + "plugin/Vstart-Plugin/DesignGraph.js'></script>").append("\n");
            builder.append("<script src='" + Jenkins.getInstance().getRootUrl() + "plugin/Vstart-Plugin/app.js'></script>").append("\n");
            
            //Table
            for (int i = 0; i < jSteps.length(); i++) {
                JSONObject json = jSteps.getJSONObject(i);
                Iterator<?> keys = json.keys();
                
                builder.append("<div> <h3>" + json.getString("scriptName").toString() + "</h3> </div>").append("\n");
                builder.append("<div> \n <table> \n <tbody>").append("\n");
                builder.append("<tr>\n" + "<td>Name: " + json.getString("scriptName").toString() + "</td>\n" + "<td>Language: "+ json.getString("scriptLanguage").toString() + "</td>\n"
                        + "<td>Status: " + json.getString("status").toString() + "</td>\n" +/* "<td>Return Value (Expected): " + json.getString("expectedValue").toString() +*/ "</td>\n"
                        + "<td>Parameters: <table><tbody>");
                //Print parameters table
                while(keys.hasNext()){
                    String key = (String) keys.next();
                    if( json.get(key) instanceof JSONObject){
                        builder.append("<tr>").append("\n");
                        builder.append("<td>").append("\n");
                        builder.append(key.toString()).append("</td>").append("\n");
                        builder.append("<td>").append("\n");
                        builder.append(json.get(key).toString()).append("</td>").append("\n");
                    }    
                }
                
                builder.append("</td>\n" + "<td>Script Output: " /*+ json.getString("scriptOutput").toString()+*/ + "</td>\n"
                        + "<td>Return Value (Actual): " /*+ json.getString("returnValue").toString()*/ + "</td>\n" + "</tr>").append("\n");
                builder.append("</tbody> \n </table> \n </div>");
            }
            
            builder.append("</body>").append("\n");
            builder.append("</html>").append("\n");

            wp.print(builder.toString());

            wp.close();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InterruptedException ex) {
            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

//    private JSONObject getJsonReport(AbstractBuild build) {
//        
//        InputStream reader = null;
//        try {
//            FilePath hPath = new FilePath(build.getWorkspace(), null)
//            reader = new FileInputStream();
//            String jsonText = IOUtils.toString(reader);
//            JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonText);
//            return json;
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                reader.close();
//            } catch (IOException ex) {
//                Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
}
