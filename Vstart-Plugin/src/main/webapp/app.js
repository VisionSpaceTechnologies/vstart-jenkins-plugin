var dGraph;

window.onload = function() {
    drawDirectedGraph("graph", data);
}

function drawDirectedGraph(id, data) {
    //dagredigraph[id] = new dagre.Digraph()
    data.id = id;
    dGraph = new DesignGraph(data);
    //graph("directedgraph" + id + "id");
    graph(id);
}

var cy;
function graph( containerId ) {
    cy = cytoscape({
        container: document.getElementById(containerId),
        style: cytoscape.stylesheet()
          .selector('node')
            .css({
              'content': '',        
              'width' : '50',
              'height' : '50',
            })
          .selector('edge')
            .css({
              'target-arrow-shape': 'triangle-backcurve',
              'curve-style' : 'bezier',
              'width': 4,
              'line-color': 'data(color)',
              'target-arrow-color': 'data(color)'
            })
            .selector('.TestStepRoot')
            .css({
              'content': '',
              'shape' : 'roundrectangle',
              'width' : '40',
              'height' : '40',
              'text-valign': 'center',
              'background-opacity':0,
              'background-fit': 'contain',
              'background-clip': 'none',
              /*'background-color': 'red'*/
              'background-image': 'assets/TestStepRoot.svg'
            })
            .selector('.TestStepLeaf')
            .css({
              'content': '',
              'shape' : 'roundrectangle',
              'width' : '40',
              'height' : '40',
              'text-valign': 'center',
              'background-opacity':0,
              'background-fit': 'contain',
              'background-clip': 'none',
              /*'background-color': 'blue'*/
              'background-image': 'assets/TestStepLeaf.svg'
            })
          .selector('.TestStepScript')
            .css({
              'content': 'data(name)',
              'shape' : 'roundrectangle',
              'width' : '250',
              'height' : '60',
              'text-halign': 'center',
              'text-valign': 'center',
              'background-color':'white',
              'border-width' : 2,
              'background-fit': 'contain',
              'background-clip': 'none',
              'background-position-x':'0px',              
              'background-image': 'assets/Grap  hNode.svg'
            })
          .selector('.highlighted')
            .css({
              'background-color': '#61bffc',
              'line-color': '#61bffc',
              'target-arrow-color': '#61bffc',
              'transition-property': 'background-color, line-color, target-arrow-color',
              'transition-duration': '0.5s'
            }),

        elements: {
            nodes: parseSteps(dGraph.steps),       
            edges: parseConnections(dGraph.connections)
          },

        layout: {
          name: 'dagre',
          nodeSep: 20,
          edgeSep: 20,
          rankSep: 50,
          rankDir: "TB",
          fit: true,
          avoidOverlap: true,
          roots: '#' + dGraph.steps[0].id,
          padding: 10
        }
      });
}
    
function parseSteps( dgraphSteps ) {
    var nodes=[];
    dgraphSteps.forEach( function(step) {
        console.log(step);
        nodes.push({ data: { 
                        id: step.id,
                        name: step.name
                        },
                     classes: step.type
                    });
    });
    return nodes;
}

function parseConnections(dgraphConnections) {
    console.log('PARSING CONNECTIONS');
    var conns=[];
    dgraphConnections.forEach( function(conn) {
        var colour;
        dGraph.groups.some( function(group) {
            if (conn.group === group.id) {
                colour = group.colour
                return true;
            }	     
            return false;
        });
        console.log(colour);
        conns.push({ data: { 
                        id: conn.from + '"' + conn.to,
                        weight: 1,
                        source: conn.from,
                        target: conn.to,
                        color: colour}
                    });
    });
    return conns;
}