/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.visionspace.vstart.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
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

    public boolean doHtmlReport(AbstractBuild build, JSONArray jArrayReport) {

        try {
            if (jArrayReport == null) {
                return false;
            }

            FilePath hPath = new FilePath(build.getWorkspace(), build.getWorkspace() + "/VSTART_HTML");
            if (!hPath.exists()) {
                hPath.mkdirs();
            }

            //Create file on workspace
            File htmlReportFile = new File(build.getWorkspace() + "/VSTART_HTML" + "/VSTART_REPORT_" + build.getNumber() + ".html");

            if (!htmlReportFile.createNewFile()) {
                return false;
            }

            //append on file with fileWriter
            FileWriter fileWriter = new FileWriter(htmlReportFile, true);
            BufferedWriter wp = new BufferedWriter(fileWriter);

            StringBuilder builder = new StringBuilder();

            //Begin document
            builder.append("<!DOCTYPE html>").append("\n");
            builder.append("<html>").append("\n");
            //Head
            builder.append("<head> "
                    + "<meta charset='utf-8'>"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1'> "
                    + "<link rel='stylesheet' href='" + Jenkins.getInstance().getRootUrl() +"/plugin/Vstart-Plugin/theme.min.css'>"
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
                    + "a#btn1 {"
                    + "margin-top : 10px;"
                    + "border: none;"
                    +   "}"
                    + "</style>  "
                    + "<title>VSTART Report #" + build.getNumber() + "</title>"
                    + "</head>").append("\n");

            //Body
            builder.append("<body>\n").append("\n");
            //Button
            builder.append("<div class=\"container-fluid\">\n").append("\n");
            builder.append("<div class='row'>\n").append("\n");
            builder.append("<div class='col-md-3'>\n").append("\n");
            builder.append("<a class=\"btn btn-primary\" href='"
                    + build.getProject().getAbsoluteUrl()
                    +"' id=\"btn1\">Back to Project</a>"
                    + "</div>"
                    + "</div>"
                    + "</div>").append("\n");
            //Structure
            builder.append("<div class=\"container-fluid\">\n").append("\n");
            builder.append("<div class='row'>\n").append("\n");
            //Main column
            builder.append("<div class='col-md-9'style=\"\n"
                    + "    border-right: lightgray dashed 1px;\n" 
                    +   "\">\n").append("\n");
            
            //Header
            builder.append("<div class=\"container-fluid\">\n").append("\n");
            builder.append("<h1>VSTART Report #" + build.getNumber() + "</h1>").append("\n");
//            builder.append("<p class='lead'>Started:" + build + "</p>").append("\n");
            builder.append("<p class='lead'>Duration:" + build.getDurationString() + "</p>").append("\n");
            builder.append("</div>\n").append("\n");

            boolean isFirstTime = true; //Control graph ids to communicate with javascript

            for (int i = 0; i < jArrayReport.length(); i++) {
                //get testcase jsonobject report
                JSONObject jsonReport = jArrayReport.getJSONObject(i);

                builder.append("        <div class=\"container-fluid\">\n"
                        + "            <h2"
                        + " id=\"testcase" + i
                        + "\">" + "Test Case - "
                        + jsonReport.getString("testCaseName")
                        + "</h2>").append("\n");

                //Graph
                Random rand = new Random();
                int id = rand.nextInt(1000) + 1;

                builder.append("<div class= 'row'>"
                        + "<div class='col-md-12'>"
                        + "<div class='panel panel-default'>"
                        + "<div class='panel-heading'> Test Case Execution Graph </div>"
                        + "<div class='panel-body'>"
                        + "                <div id='graph" + id + "' style='/*display: block;*/ width: 100%; height: 600px;' class='center-block'></div>\n"
                        + "            </div>            \n"
                        + "            </div>            \n"
                        + "            </div>            \n"
                        + "            </div>            \n"
                        + "            </div>            \n").append("\n");
                builder.append("            <script type='text/javascript'>").append("\n");

                //if this is the first testcase, the object array must be initialized
                if (isFirstTime) {
                    //create object
                    builder.append("obj = {}").append("\n");
                    //insert key and value
                    builder.append("obj.graph" + id + " = " + new JSONObject(jsonReport.getString("extendedGraph")).toString()).append("\n");
                    //create array
                    builder.append("var arr = []").append("\n");
                    //push to array
                    builder.append("arr.push(obj)").append("\n");
                    //invert variable
                    isFirstTime = false;
                } else {
                    //create object
                    builder.append("obj = {}").append("\n");
                    //insert key and value
                    builder.append("obj.graph" + id + " = " + new JSONObject(jsonReport.getString("extendedGraph")).toString() + ";").append("\n");
                    //push to array
                    builder.append("arr.push(obj);").append("\n");
                }
//            builder.append("var graphId =").append(Integer.toString(id)).append(";").append("\n");
//            builder.append("data=").append(new JSONObject(jsonReport.getString("extendedGraph")).toString()).append("\n");
                builder.append("pathPrefix=").append(" '" + Jenkins.getInstance().getRootUrl()).append("' \n");
                builder.append("</script>\n"
                        + "            <script type='text/javascript' src='https://cdnjs.cloudflare.com/ajax/libs/cytoscape/2.4.6/cytoscape.js'></script>\n"
                        + "            <script type='text/javascript' src='"+ Jenkins.getInstance().getRootUrl() +"/plugin/Vstart-Plugin/dagre.js'></script>\n"
                        + "            <script type='text/javascript' src='" + Jenkins.getInstance().getRootUrl() +"/plugin/Vstart-Plugin/DesignGraph.js'></script>\n"
                        + "            <script type='text/javascript' src='" + Jenkins.getInstance().getRootUrl() + "/plugin/Vstart-Plugin/app.js'></script>").append("\n");
                
            //end of Graph
                //Table
                builder.append("<div class=\"container-fluid\">\n").append("\n");
                builder.append("<div class=\"row\">\n").append("\n");
                JSONArray jSteps = jsonReport.getJSONArray("steps");
                for (int j = 0; j < jSteps.length(); j++) {
                    JSONObject json = jSteps.getJSONObject(j);

                    builder.append("                <div class='col-lg-6 col-md-12'>\n"
                            + "                    <div class=\"panel panel-default\">\n"
                            + "                        <div class='panel-heading' style='overflow: auto;'> ").append("\n");
                    builder.append("<h3 " + "id='step" + i + j + "'>" + "Step #" + j +" : " + json.getString("scriptName").toString() + "</h3>"
                            + "</div>").append("\n");
                    builder.append("<table class='table'> \n"
                            + "                            <tbody>").append("\n");

                    //Script Language
                    builder.append("<tr> \n"
                            + "                                    <td class='field-names'>Language: </td>"
                            + "<td>" + json.getString("scriptLanguage").toString() + "</td>\n</tr>").append("\n");

                    //Test case status
                    if (json.getString("status").equals("PASSED")) {
                        builder.append("                                <tr> \n"
                                + "                                    <td class='field-names'>Status: </td>\n"
                                + "                                    <td class='PASSED'>PASSED</td>\n"
                                + "                                </tr>").append("\n");
                    } else if (json.getString("status").equals("FAILED")) {
                        builder.append("<tr> \n"
                                + "                                    <td class='field-names'>Status: </td>\n"
                                + "                                    <td class='FAILED'>FAILED</td>\n"
                                + "                                </tr>").append("\n");
                    }

                    //Script source code
                    builder.append("<tr> \n"
                            + "                                    <td class='field-names'> Source: </td>").append("\n");
                    builder.append("                                    <td> \n"
                            + "                                        <pre>"
                            + json.getString("scriptSource")
                            + "</pre> \n"
                            + "                                    </td> \n"
                            + "                                </tr>").append("\n");

                    //Expected Return Value
                    builder.append("<tr> \n"
                            + "                                    <td class='field-names'>Return Value (Expected): </td>").append("\n");
                    builder.append("                                    <td>" + json.getString("expectedValue").toString() + "</td>"
                            + "                                </tr>").append("\n");

                    //Print parameters table
                    JSONObject jParam = json.getJSONObject("scriptParameters"); //Parameters JSONObject for iteration
                    Iterator<?> keys = jParam.keys();

                    builder.append("                                    <td class='field-names'>Parameters: </td>\n"
                            + "                                    <td>\n"
                            + "                                        <table class='table table-striped'>\n"
                            + "                                            <tbody>").append("\n");
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (jParam.get(key) instanceof String) {
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
                    builder.append("                                <tr>\n"
                            + "                                    <td class="
                            + "'field-names'>Script Output: </td>").append("\n");
                    builder.append("<td><pre>" + json.getString("scriptOutput").toString() + "</pre>" + "</tr>\n").append("\n");

                    //Actual return value
                    builder.append("                                <tr>\n"
                            + "                                    <td class='field-names'>Return Value (Actual): </td>"
                            + "                                    <td>"
                            + json.getString("returnedValue").toString()
                            + "</td>"
                            + "                                </tr>").append("\n");

                    //Close element
                    builder.append("                            </tbody> \n"
                            + "                        </table> \n"
                            + "                    </div>\n"
                            + "                </div>").append("\n");

                    
                    
                }
                builder.append("            </div>\n").append("\n");
                builder.append("            </div>\n").append("\n");
                
                //add testbed panel
                builder.append("<div class=\"container-fluid\">\n").append("\n");
                builder.append("<div class=\"row\">\n").append("\n");
                builder.append("<div class=\"col-md-12\">\n").append("\n");
                builder.append("<div class='panel panel-default'>").append("");
                builder.append("<div class='panel-heading' id='testbed"+ i +"'> TESTBED </div>").append("\n");
                builder.append("<table class='table table-hover table-striped machines'>").append("\n");
                builder.append("<thead>").append("\n");
                builder.append("<tr class='step-headers'>").append("\n");
                builder.append("<th>Machine</th>").append("\n");
                builder.append("<th>Label</th>").append("\n");
                builder.append("</tr>").append("\n");
                builder.append("<tbody>").append("\n");

                //Loop through JSONObject
                JSONObject jTestBed = jsonReport.getJSONObject("testBed");
                Iterator<?> testKeys = jTestBed.keys();

                while (testKeys.hasNext()) {
                    String key = (String) testKeys.next();
                    if (jTestBed.get(key) instanceof String) {
                        builder.append("<tr>").append("\n");
                        builder.append("<td>").append("\n");
                        builder.append(key).append("</td>").append("\n");
                        builder.append("<td>").append("\n");
                        builder.append(jTestBed.get(key).toString()).append("</td>").append("\n");
                        builder.append("</tr>").append("\n");
                    }
                }
                builder.append("</tbody>").append("\n");
                builder.append("</table>").append("\n");
                builder.append("</div>").append("\n");
                builder.append("</div>").append("\n");
                builder.append("</div>").append("\n");
                builder.append("</div>").append("\n");
                
                
            }
            
            
            builder.append("</div>").append("\n");
            
            builder.append("<div class='col-md-3'>").append("\n");
            builder.append("<nav class='sidebar hidden-xs hidden-print hidden-sm affix'>").append("\n");
            //links to testcase information
            builder.append("<h4>VSTART Report #" + build.getNumber() +"</h4>");
            builder.append("<ul>").append("\n");
            for (int c = 0; c < jArrayReport.length(); c++) {
                JSONObject jsonReport = jArrayReport.getJSONObject(c);
                builder.append("<li>").append("\n");
                builder.append("<a href='#testcase" + c + "'>").append("\n");
                builder.append("<b>").append("\n");
                builder.append("Test Case: " + jsonReport.getString("testCaseName")).append("\n");
                builder.append("</b>").append("\n");
                builder.append("</a>").append("\n");

                //Steps
                builder.append("<ul>").append("\n");
                JSONArray jArray = jsonReport.getJSONArray("steps");
                for (int d = 0; d < jArray.length(); d++) {
                    builder.append("<li>").append("\n");
                    builder.append("<a href='#step" + c + d + "'>").append("\n");
                    builder.append("Step #"+ d + ":" + jArray.getJSONObject(d).getString("scriptName")).append("\n");
                    builder.append("</a>").append("\n");
                    builder.append("</li>").append("\n");
                }
                
                //testbed
                builder.append("<li>").append("\n");
                builder.append("<a href='#testbed" + c + "'>").append("\n");
                builder.append("Testbed").append("\n");
                builder.append("</a>").append("\n");
                builder.append("</li>").append("\n");
                    
                builder.append("</ul>").append("\n");
                builder.append("</li>").append("\n");
            }
            
            builder.append("<li>").append("\n");
            builder.append("<a href=\""
                    + build.getProject().getAbsoluteUrl()
                    +"\"> Back to Project </a></li>").append("\n");

            builder.append("                        </ul>").append("\n");
            builder.append("                    </nav>").append("\n");
            builder.append("                </div>").append("\n");
            
            //Finish document
            builder.append("            </div>\n"
                    + "        </div>\n"
                    + "\n"
                    + "    </body>\n"
                    + "</html>").append("\n");

            //Print to html file on project's workspace
            wp.write(builder.toString());

            wp.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (InterruptedException ex) {
            Logger.getLogger(VSPluginHtmlWriter.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
