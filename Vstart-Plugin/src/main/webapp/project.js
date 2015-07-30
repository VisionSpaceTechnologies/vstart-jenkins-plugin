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
        window.alert(t.responseObject());
//        document.getElementById("testCase").innerHTML = t.responseObject();
    });
}
