/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function test() {
    window.alert("What? Is this a test?");
}


var array;
function FillDropDowns(id, testcasesId, imgId, thisInstance) {
    select = document.getElementById(id);
    //Get Projects
    if(thisInstance !== null){
        //gets projects with instance
        callbackObject = new GetProjectsCallBack(thisInstance, id, testcasesId);
        thisInstance.getProjects(callbackObject.callback);
    } else {
        //gets projects with descriptor
        callbackObject = new GetProjectsFirstTimeCallBack(desc, id, testcasesId);
        desc.getProjects(callbackObject.callback);
    }
}

function getProjectId(projectSelectId, testcasesId, thisInstance){
    select = document.getElementById(projectSelectId);
    option = select.options[select.selectedIndex];
    projId = option.getAttribute("value");
    if(thisInstance !== null){
        callbackObject = new GetTestCasesFirstTimeCallBack(thisInstance, testcasesId);
        thisInstance.getTestCases(projId, callbackObject.callback);
    } else {
        callbackObject = new GetTestCasesFirstTimeCallBack(desc, testcasesId);
        desc.getTestCases(projId, callbackObject.callback);
    }     
}

var GetProjectsFirstTimeCallBack = function(descriptor, projectSelectId, testcasesId){
    this.descriptor = descriptor;
    this.projectSelectedId = projectSelectId;
    this.testcasesId = testcasesId;
    var self = this;
    GetProjectsFirstTimeCallBack.prototype.callback = function(t){
        projectSelect = document.getElementById(self.projectSelectedId);
        projectSelect.options.length = 0;
        array = JSON.parse(t.responseObject());
        for (var i = 0; i < array.length; ++i) {
            option = document.createElement("option");
            option.setAttribute("value", array[i]["id"]);
            option.innerHTML = array[i]["name"];
            projectSelect.appendChild(option);
        }
        projId = projectSelect.options[projectSelect.selectedIndex].getAttribute("value");
        callbackObject = new GetTestCasesFirstTimeCallBack(self.descriptor, self.testcasesId);
        self.descriptor.getTestCases(projId, callbackObject.callback);
    }
}

var GetProjectsCallBack = function(thisInstance, projectSelectId, testcasesId){
    this.thisInstance = thisInstance;
    this.projectSelectedId = projectSelectId;
    this.testcasesId = testcasesId;
    var self = this;
    GetProjectsCallBack.prototype.callback = function(t){
        projectSelect = document.getElementById(self.projectSelectedId);
        projectSelect.options.length = 0;
        array = JSON.parse(t.responseObject());
        for (var i = 0; i < array.length; ++i) {
            option = document.createElement("option");
            option.setAttribute("value", array[i]["id"]);
            option.innerHTML = array[i]["name"];
            projectSelect.appendChild(option);
        }
        //Check the latest stored project
        callBackObject = new ProjectSelectedCallBack(self.projectSelectedId, self.testcasesId, self.thisInstance);
        self.thisInstance.getVstProjectId(callBackObject.callback);
        
    }
}

var ProjectSelectedCallBack = function(projectSelectId, testcasesId, thisInstance){
    this.projectSelectId = projectSelectId;
    this.testcasesId = testcasesId;
    this.thisInstance = thisInstance;
    var self = this;
    ProjectSelectedCallBack.prototype.callback = function(t) {
        projectSelect = document.getElementById(self.projectSelectId);
        for (i = 0; i < projectSelect.options.length; i++){
            if (projectSelect.options[i]["value"] === t.responseObject()) {
                projectSelect.options[i].setAttribute("selected", true);
            }
        }
        //List respective tests
        projId = projectSelect.options[projectSelect.selectedIndex].getAttribute("value");
        callbackObject = new GetTestCasesCallBack(self.thisInstance, self.testcasesId);
        thisInstance.getTestCases(projId, callbackObject.callback);
    }
}

var GetTestCasesFirstTimeCallBack = function(descriptor, idTestCaseSelect) {
    this.descriptor = descriptor;
    this.idTestCaseSelect = idTestCaseSelect;
    var self = this;
    GetTestCasesFirstTimeCallBack.prototype.callback = function (t) {
        testCaseSelect = document.getElementById(self.idTestCaseSelect);
        testCaseSelect.options.length = 0; //This guarantees that the dropdown doesn't have repeated options
        array = JSON.parse(t.responseObject());
        for (var i = 0; i < array.length; ++i) {
            option = document.createElement("option");
            option.setAttribute("value", array[i]["id"]);
            option.innerHTML = array[i]["name"];
            testCaseSelect.appendChild(option);
        }
    }
}

var GetTestCasesCallBack = function(instance, idTestCaseSelect) {
    this.idTestCaseSelect = idTestCaseSelect;
    this.instance = instance;
    var self = this;
    GetTestCasesCallBack.prototype.callback = function (t) {
        testCaseSelect = document.getElementById(self.idTestCaseSelect);
        testCaseSelect.options.length = 0; //This guarantees that the dropdown doesn't have repeated options
        array = JSON.parse(t.responseObject());
        for (var i = 0; i < array.length; ++i) {
            option = document.createElement("option");
            option.setAttribute("value", array[i]["id"]);
            option.innerHTML = array[i]["name"];
            testCaseSelect.appendChild(option);
        }
        callBackObject = new TestCaseSelectedCallBack(self.idTestCaseSelect);
        self.instance.getTestCase(callBackObject.callback);
    }
}

var TestCaseSelectedCallBack = function(idTestCaseSelect) {     
    this.idTestCaseSelect = idTestCaseSelect;
    var self = this;
    TestCaseSelectedCallBack.prototype.callback = function(t){
        testCaseSelect = document.getElementById(self.idTestCaseSelect);
        for (i = 0; i < testCaseSelect.options.length; i++){
            if (testCaseSelect.options[i]["value"] == t.responseObject()) {
                testCaseSelect.options[i].setAttribute("selected", true);
            }
        }
    }
}
