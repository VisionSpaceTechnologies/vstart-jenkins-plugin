/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.FilePath;
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
            builder.append("<link rel='stylesheet' href='http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css'>").append("\n");
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
                builder.append("<tr> \n <td>Language: </td>\n" + "<td>"+ json.getString("scriptLanguage").toString() + "</td>\n</tr>"
                        + "<tr> \n <td>Status: </td>\n" + "<td>" + json.getString("status").toString() + "</td>\n</tr>" 
                        + "<tr> \n <td> Source: </td> \n <td> \n <pre> \n" + json.getString("scriptSource").toString() + "</pre> \n </td> \n </tr>"
                        /*  + "<tr> \n <td>Return Value (Expected): </td>\n"+"<td>"+ json.getString("expectedValue").toString() + "</td>\n</tr>"+*/
                        + "<tr><td>Parameters: </td>\n" + "<td>\n<table>\n<tbody>\n");
                //Print parameters table
                while(keys.hasNext()){
                    String key = (String) keys.next();
                    if( json.get(key) instanceof JSONObject){
                        builder.append("<tr>").append("\n");
                        builder.append("<td>").append("\n");
                        builder.append(key.toString()).append("</td>").append("\n");
                        builder.append("<td>").append("\n");
                        builder.append(json.get(key).toString()).append("</td>").append("\n");
                        builder.append("</tr>").append("\n");
                    }    
                }
                builder.append("</tbody> \n </table> \n </td>").append("\n");
                builder.append("<tr>\n<td>Script Output: </td>\n" /*+ "<td>" + json.getString("scriptOutput").toString()+"</td>"+*/ + "</tr>\n"
                        + "<tr><td>Return Value (Actual): </td>" /*+ "<td>" json.getString("returnValue").toString() + "</td>"*/ + "</tr>").append("\n");
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
