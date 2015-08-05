/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function test(){
    window.alert("What? Is this a test?");
}

function getProjectId(id, testcasesId){
    select = document.getElementById(id);
    option = select.options[select.selectedIndex];
    
    projId = option.getAttribute("value");
    builder.setVstProjectId(projId);
//    window.alert(projId);
    desc.getTestCases(projId, function(t){
//        window.alert(t.responseObject());
        testCaseSelect = document.getElementById(testcasesId);
        testCaseSelect.options.length = 0;  //This guarantees that the dropdown doesn't have repeated options
        arr = JSON.parse(t.responseObject());
        for( var i = 0; i < arr.length; ++i) {
            option = document.createElement("option");
            option.setAttribute("value", arr[i]["id"]);
            option.innerHTML = arr[i]["name"];
            testCaseSelect.appendChild(option);
        }
        if (testCaseSelect.options.length !== 0) {
            testCaseSelect.selectedIndex = 0;
            getTestId(testCaseSelect.getAttribute("id"));
        }
//        document.getElementById("testCase").innerHTML = t.responseObject();
    });
}

function getTestId(id){
    select = document.getElementById(id);
    option = select.options[select.selectedIndex];
    
    testId = option.getAttribute("value");
    builder.setVstTestId(testId);
//    window.alert(testId);
}
