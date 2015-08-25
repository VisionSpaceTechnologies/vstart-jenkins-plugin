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
//            builder.append("<link rel='stylesheet' href='http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css'>").append("\n");
            builder.append("<head> "
                    + "<meta charset='utf-8'>"
                    + "<meta name='viewport' content='width=\"device-width\", initial-scale=1'> "
                    + "<link rel='stylesheet' href='http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css'>"
                    + "<script src='https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js'></script>"
                    + "<script src='http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js'></script>"
                    + "<style type='text/css'>"
                    + ".center {"
                    + "margin: auto;"
                    + "width: 60%;"
                    + "border:0px;"
                    + "padding: 10px;"
                    + "}"
                    + "h3{"
                    + "font-family: inherit;"
                    + "color: #1faf8e;"
                    + "line-height: 1.1;"
                    + "}"
                    + ".FAILED{"
                    + "color: red;"
                    + "}  "
                    + ".PASSED{"
                    + "color: #8ac007;"
                    + "}"
                    + "</style>  "
                    + "<title>VSTART Report</title>"
                    + "</head>").append("\n");
            builder.append("<body>\n" +
                            "        <div class=\"container-fluid\">\n" +
                            "            <h1>VSTART Report #"+ build.getNumber() +"</h1>").append("\n");
            
            //Graph
            builder.append("<div class=\"center\">\n" +
                            "                <div id='graph' style='display: block; width: 800px; height: 600px;'></div>\n" +
                            "            </div>            \n" +
                            "            <script type='text/javascript'>").append("\n");
            builder.append("data=").append(new JSONObject(jsonReport.getString("extendedGraph")).toString()).append("\n");
            builder.append("pathPrefix=").append(" '" + Jenkins.getInstance().getRootUrl()).append("' \n");
            builder.append("</script>\n" +
                            "            <script type='text/javascript' src='https://cdnjs.cloudflare.com/ajax/libs/cytoscape/2.4.6/cytoscape.js'></script>\n" +
                            "            <script type='text/javascript' src='http://localhost:8080/jenkins/plugin/Vstart-Plugin/dagre.js'></script>\n" +
                            "            <script src='http://localhost:8080/jenkins/plugin/Vstart-Plugin/DesignGraph.js'></script>\n" +
                            "            <script src='http://localhost:8080/jenkins/plugin/Vstart-Plugin/app.js'></script>").append("\n");
            builder.append("</div>").append("\n");
            //end of Graph
            
            //Table
            builder.append("<div class=\"container-fluid\">\n").append("\n"); 
            for (int i = 0; i < jSteps.length(); i++) {
                JSONObject json = jSteps.getJSONObject(i);
                JSONObject jParam = json.getJSONObject("scriptParameters"); //Parameters JSONObject for iteration
                Iterator<?> keys = jParam.keys();
                
                if( (i % 2) == 0 ){
                    builder.append("            <div class='row'>\n").append("\n");
                }
                builder.append("                <div class='col-md-6'>\n" +
                                "                    <div class=\"panel panel-default\">\n" +
                                "                        <div class='panel-heading' style='overflow: auto;'> ").append("\n");
                builder.append("<h3>" + json.getString("scriptName").toString() + "</h3>"
                                +"</div>").append("\n");
                builder.append("<table class='table'> \n" +
                                "                            <tbody>").append("\n");
                
                //Script Language
                builder.append("<tr> \n" +
                                "                                    <td class='field-names'>Language: </td>"
                                +"<td>"+ json.getString("scriptLanguage").toString() + "</td>\n</tr>").append("\n");
                
                //Test case status
                if(json.getString("status").equals("PASSED")){
                    builder.append("                                <tr> \n" +
"                                    <td class='field-names'>Status: </td>\n" +
"                                    <td class='PASSED'>PASSED</td>\n" +
"                                </tr>").append("\n");
                } else if (json.getString("status").equals("FAILED")){
                    builder.append("<tr> \n" +
"                                    <td class='field-names'>Status: </td>\n" +
"                                    <td class='FAILED'>FAILED</td>\n" +
"                                </tr>").append("\n");
                }
                
                //Script source code
                builder.append("<tr> \n" +
"                                    <td class='field-names'> Source: </td>").append("\n");
                builder.append("                                    <td> \n" +
                                "                                        <pre>"
                        +  json.getString("scriptSource") 
                        + "</pre> \n" +
                            "                                    </td> \n" +
                            "                                </tr>").append("\n");
                
                //Expected Return Value
                builder.append("<tr> \n" +
                                "                                    <td class='field-names'>Return Value (Expected): </td>").append("\n");
                builder.append("                                    <td>" + json.getString("expectedValue").toString() + "</td>"
                                + "                                </tr>").append("\n");
                
                //Print parameters table
                builder.append("                                    <td class='field-names'>Parameters: </td>\n" +
                                "                                    <td>\n" +
                                "                                        <table class='table table-striped'>\n" +
                                "                                            <tbody>").append("\n");
                while(keys.hasNext()){
                    String key = (String) keys.next();
                    if( jParam.get(key) instanceof String){
                        builder.append("<tr>").append("\n");
                        builder.append("<td class='field-names'>").append("\n");
                        builder.append(key.toString()).append("</td>").append("\n");
                        builder.append("<td>").append("\n");
                        builder.append(jParam.get(key).toString()).append("</td>").append("\n");
                        builder.append("</tr>").append("\n");
                    }    
                }
                builder.append("</tbody> \n </table> \n </td>").append("\n");
                
                //Script Output
                builder.append("                                <tr>\n" +
                                "                                    <td class="
                                + "'field-names'>Script Output: </td>").append("\n");
                builder.append("<td><pre>" + json.getString("scriptOutput").toString()+"</pre>" + "</tr>\n").append("\n");
                
                //Actual return value
                builder.append("                                <tr>\n" +
                                "                                    <td class='field-names'>Return Value (Actual): </td>"
                                +"                                    <td>"
                                + json.getString("returnedValue").toString() +
                                "</td>"
                                +"                                </tr>").append("\n");
                
                //Close element
                builder.append("                            </tbody> \n" +
                                "                        </table> \n" +
                                "                    </div>\n" +
                                "                </div>").append("\n");
                
                if( (i % 2) != 0 ){
                    builder.append("            </div>\n").append("\n");
                }
            }
            
            //Finish document
            builder.append("            </div>\n" +
                            "        </div>\n" +
                            "\n" +
                            "    </body>\n" +
                            "</html>").append("\n");

            //Print to html file on project's workspace                
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
}
