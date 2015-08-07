/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function test() {
window.alert("What? Is this a test?");
        }


var array;
function getProjectId(id, testcasesId, imgId) {
    select = document.getElementById(id);
    option = select.options[select.selectedIndex];
    window.testCaseSelect = document.getElementById(testcasesId);
    if (option !== undefined) {
        projId = option.getAttribute("value");
        if (instance !== null) {
            instance.setVstProjectId(projId);
      //    window.alert(projId);
            instance.getTestCases(projId, function (t) {
  //        window.alert(t.responseObject());
            window.testCaseSelect.options.length = 0; //This guarantees that the dropdown doesn't have repeated options
            array = JSON.parse(t.responseObject());
            for (var i = 0; i < array.length; ++i) {
                option = document.createElement("option");
//                if(arr[i]["id"] == instance.getTestCase()){
//                    option.setAttribute("selected", true);
//                }
                option.setAttribute("value", array[i]["id"]);
                option.innerHTML = array[i]["name"];
                window.testCaseSelect.appendChild(option);
            }
            instance.getTestCase(function(t){
                    
                    for(i = 0; i < testCaseSelect.options.length; i++)
                        if (testCaseSelect.options[i]["value"] == t.responseObject()) {
                            testCaseSelect.options[i].setAttribute("selected", true);
                        }
            });
//            instance.getTestCase(function (t) {
//                option = testCaseSelect.options;
//                for (var i = 0; i < array.length; i++) {
//                    if (array[i]["id"] == t.responseObject())
//                        option[i].setAttribute("selected", true);
//                }
//            });
                //        document.getElementById("testCase").innerHTML = t.responseObject();
            });
            if (window.testCaseSelect.options.length !== 0) {
                window.testCaseSelect.selectedIndex = 0;
                getTestId(window.testCaseSelect.getAttribute("id"));
            }
        }
        else {
                desc.getTestCases(projId, function (t) {
        //        window.alert(t.responseObject());

                    window.testCaseSelect.options.length = 0; //This guarantees that the dropdown doesn't have repeated options
                    array = JSON.parse(t.responseObject());
                    for (var i = 0; i < array.length; ++i) {
                        option = document.createElement("option");
        //                if(arr[i]["id"] == instance.getTestCase()){
        //                    option.setAttribute("selected", true);
        //                }
                        option.setAttribute("value", array[i]["id"]);
                        option.innerHTML = array[i]["name"];
                        window.testCaseSelect.appendChild(option);
                    }
                });
            }
        }
        else {
                select.remove();
                desc.setStat = false;
//        document.getElementById(imgId).innerHTML = '<img src="http://localhost:8080/jenkins/plugin/Vstart-Plugin/images/red-icon.png">'
                document.getElementById(imgId).remove();
                document.getElementById(testcasesId).remove();
                window.alert("Connection with VSTART server is compromised.");
        }
}

function getTestId(id) {
    select = document.getElementById(id);
    option = select.options[select.selectedIndex];

    if (option !== undefined) {
        testId = option.getAttribute("value");
        if (instance !== null){
            instance.setTestCase(testId);
        }    
//    window.alert(testId);
    }
}

