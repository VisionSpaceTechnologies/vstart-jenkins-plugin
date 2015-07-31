/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function test(){
    window.alert("What? Is this a test?");
}

function getProjectId(id){
    select = document.getElementById(id);
    option = select.options[select.selectedIndex];
    projId = option.getAttribute("value");
//    window.alert(projId);
    desc.getTestCases(projId, function(t){
//        window.alert(t.responseObject());
        testCaseSelect = document.getElementById("testCase");
        testCaseSelect.options.length = 0;  //This guarantees that the dropdown doesn't have repeated options
        arr = JSON.parse(t.responseObject());
        for( var i = 0; i < arr.length; ++i) {
            option = document.createElement("option");
            option.setAttribute("value", arr[i]["id"]);
            option.innerHTML = arr[i]["name"];
            testCaseSelect.appendChild(option);
        }
//        document.getElementById("testCase").innerHTML = t.responseObject();
    });
}

function getTestId(id){
    select = document.getElementById(id);
    option = select.options[select.selectedIndex];
    testId = option.getAttribute("value");
    desc.setVstTestId(testId);
//    window.alert(testId);
}
