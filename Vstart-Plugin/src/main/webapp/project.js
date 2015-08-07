/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function test() {
    window.alert("What? Is this a test?");
}


var array;
function getProjectId(id, testcasesId, imgId, thisInstance) {
    select = document.getElementById(id);
    option = select.options[select.selectedIndex];
    if (option !== undefined) {
        projId = option.getAttribute("value");
        if (thisInstance !== null) {
            testCaseSelect = document.getElementById(testcasesId);
            if (testCaseSelect.options.length !== 0) {
                testCaseSelect.selectedIndex = 0;
            }
            callbackObject = new GetTestCasesCallBack(thisInstance, testcasesId);
            thisInstance.getTestCases(projId, callbackObject.callback);
        }
        else {
            callbackObject = new GetTestCasesFirstTimeCallBack(desc, testcasesId);
            
            desc.getTestCases(projId, callbackObject.callback);
        }
    }
    else {
        select.remove();
        desc.setStat = false;
        document.getElementById(imgId).remove();
        document.getElementById(testcasesId).remove();
        window.alert("Connection with VSTART server is compromised.");
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
        for (i = 0; i < testCaseSelect.options.length; i++)
            if (testCaseSelect.options[i]["value"] == t.responseObject()) {
                testCaseSelect.options[i].setAttribute("selected", true);
            }
    }
}