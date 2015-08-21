/**
 * DesignGraph parsing library: 
 */

 function DesignGraph(data) {
	 this.id = data.id;
	 this.name = data.name;
	 this.type = data.type;
	 this.steps = data.steps;
	 this.connections = data.connections;
	 this.groups = data.groups;

	 //Assign colours to the new groups:
	 this.colourize();
	 
	 //assign groups to connections:
	 this.assignGroupsToConnections();
 }
 
 DesignGraph.prototype.colourize = function() {
	 var pallete = [ 
	                 "rgb(41, 128, 185)",
	                 "rgb(230, 126, 34)",	                 
	                 "rgb(44, 62, 80)",
	                 "rgb(22, 160, 133)",
	                 "rgb(142, 68, 173)",
	                 "rgb(241, 196, 15)",	                 
	                 ];
     this.groups.forEach(function(group) {		    	 
         group['colour'] = pallete[group.id % pallete.length];
         console.log(group);
     });
 };
 
 DesignGraph.prototype.assignGroupsToConnections = function() {
     var graph = this;
     this.connections.forEach(function(connection) { 
	 var fromStep;
	 var toStep;
	 graph.steps.some( function(step) {
	     if (fromStep === undefined && step.id == connection.from) {
		 fromStep = step;
		 return true;
	     }	     
	     return false;
	 });
	 
	 connection['group'] = fromStep.group;
     }); 
 };
 
 DesignGraph.prototype.generateGraph = function (container) {     
     this.generateGraphNodes(container);
     this.generateGraphConnections();
 }
 
 DesignGraph.prototype.generateGraphNodes = function(container) {
     var graph = this;
     
     this.steps.forEach(function(step) {
	 
	 // The DIV:
	 var div = document.createElement('div');
	 
	 $(div).attr('id', 'node'+step.id).attr('key', step.id);
	 
	 $(div).addClass("w");
	 if(step.type === "TestStepScript" || step.type === "TestStepBuildingBlock") {
	     $(div).addClass("step");
	 } else {
	     $(div).addClass("control");
	 }
	 $(div).addClass("boxclass" + step.id);
	 graph.updateGraphNode(div);
	 
	 $(div).html("<img src='" + pathPrefix + "plugin/Vstart-Plugin/" + step.type + ".svg'>" + step.name);
	 	 
	 $(container).append(div);
	 
	 if(graph.onNodeCreated !== undefined) {
	 	graph.onNodeCreated(step, div);
     	 }
	 
	 // The NODE:
	 dagredigraph[graph.id].addNode('node' + step.id, {
	     label: step.id,
	     width:115,
	     height: 55
	 });
     });
 };
 
 
 DesignGraph.prototype.generateGraphConnections = function() {     
     var graph = this;     
     plumbGraphConns = new Array();
     this.connections.forEach(function(connection) {
	 var con = {
		 source: 'node'+connection.from,
		 target: 'node'+connection.to,
		 paintStyle: { strokeStyle: graph.groups[connection.group].colour, lineWidth:2 }
	 }
	 
	 plumbGraphConns.push(con);
     });
     //initPlumb(this.id, this.id);
 };
 
 DesignGraph.prototype.insertEvent = function(event) {
     var graph = this;
     this.steps.some( function (step) {
	 if (step.id === event.id) {
	     step[event.type] = new Date(event.timestamp);
	     step['status'] = event.status;
	     graph.updateTimestamps();
	     graph.updateGraphNode($('#node' + step.id), event.status);
	     graph.updateTimelineEntry(step);
	     return true;
	 }
	 return false;
     });
 };
 
 DesignGraph.prototype.updateTimestamps = function() {
     var graph = this;
     var dataset = new Array();
     this.steps.forEach( function(step) {
	 if (step.started !== undefined) {
	     if (step.status === "RUNNING") {
		 step['stopped'] = vis.moment();
		 dataset.push(graph.constructTimelineDataFromStep(step));
	     }
	     
	 }
     });
     visdata.update(dataset);
     timeline.fit([animate=100]);
 };

DesignGraph.prototype.generateCssBorderStatus = function (status, thickness) {
    if ( thickness === undefined ) thickness = 10;
    var colour;
    switch(status) {
    	case 'RUNNING':
    	    colour = 'blue';
    	    break;
    	case 'PASSED':
    	    colour = '#8ac007';
    	    break;
    	case 'FAILED':
    	    colour = "red";
    	    break;
        case 'ERROR':
    	    colour = "#610B0B"
    	    break;
        case 'INCONCLUSIVE':
    	    colour = "#D8D8D8";
    	    break;
        case 'NOT_EXECUTED':
    	    colour = "black";
    	    break;        
    	default:
    	    colour = '#222222';
    }
    
    return (thickness + 'px solid ' + colour);    
} 

DesignGraph.prototype.updateGraphNode = function(container, status) {    
    if ($(container).hasClass('step')) {
    	$(container).css('border-left', this.generateCssBorderStatus(status) );
    }
}

DesignGraph.prototype.resetNodes = function() {
    var graph = this;
    this.steps.forEach( function (step) {
	step['status'] = '';
	step['started'] = undefined;
	step['stopped'] = undefined;
	graph.updateGraphNode($('#node'+step.id), step.status);
    });
}

DesignGraph.prototype.constructTimelineDataFromStep = function(step) {
    var data;
    var graph = this;
    if (step.type === "TestStepRoot" || step.type === "TestStepLeaf") {
	var targetGroup;
        var targetGroupId;
        //if root the id is the one on the step:
        if ( step.type === "TestStepRoot" ) {
            targetGroupId = step.group;
        } else { //otherwise use the group of the step that connects to this leaf:
            var connectionToLeaf;
            graph.connections.some ( function(conn) {
               if ( conn.to === step.id) {
                   connectionToLeaf = conn;
                   return true;
               }
               return false;
            });
            var previousStep;
            graph.steps.some( function(step) {
                if (step.id === connectionToLeaf.from) {
                    previousStep = step;
                    return true;
                }
                return false;	     
            });
            targetGroupId = previousStep.group;
        }
	this.groups.some( function(group){
	    if ( group.id === targetGroupId) {
		targetGroup = group;
		return true;
	    }
	    return false;
	});
	
	if(step.type === "TestStepRoot") {
	    var style = 'background-color: ' + targetGroup.colour.replace("rgb(", "rgba(").replace(")", ",0.2)") + ';';
	    style += 'z-index:' + targetGroup.id + ';';
            data = {	id: targetGroup.id,
        		type: 'background',
        		start: step.started,
        		style: style 
            };
    	} else {
    	    data = {	id: targetGroup.id };
    	    if (step.stopped !== undefined) data['end'] = step.stopped;
    	}
    } else {
        var style = 'z-index: ' + this.groups.length + ';';
        style += 'border-left:' + this.generateCssBorderStatus(step.status, 5);
        data = {	id: step.id,
        		style: style,
    	    		content: step.name,
    	    		start: step.started
    	    	};
        if (step.stopped !== undefined) data['end'] = step.stopped;
    }
    return data;
}

DesignGraph.prototype.updateTimelineEntry = function(step) {
    var data = this.constructTimelineDataFromStep(step);
    visdata.update(data);    
}