$(document).ready(function(){
    var ultimostato;
    var ap;
    var lvl;
    var t;
    var manMode;

    function updateVal(){
        $.getJSON("http://localhost:8080/api/data", function(result){
        for(i=0;i< result.length;i++){
            ultimostato=result[result.length-1]["stato"];
            ap=result[result.length-1]["percApertura"];
            lvl=result[result.length-1]["valLivello"];
            t=result[result.length-1]["time"];
            manMode=result[result.length-1]["manMode"];
            $("h1").html("Stato: "+ ultimostato);
            $("h2").html("Percentuale Apertura Diga: "+ap);
        }        
        if(ultimostato=="NORMALE"){
            $("canvas").hide();
            $("h2").hide();
            $("h3").hide();
        } else if(ultimostato=="PREALLARME"){
            $("canvas").show();
            $("h2").hide();
            $("h3").hide();
        }else if(ultimostato=="ALLARME"){
            $("canvas").show();
            $("h2").show();
            
            if(manMode=="on"){
                $("h3").show();
            }else{
                $("h3").hide();
            }
            
        }
        });
        
    }
    
    function onRefresh(chart) {
        updateVal();
        chart.config.data.datasets.forEach(function(dataset) {
            dataset.data.push({
                x: Date.now(),
                y: lvl
            });
        });
    }   

    var ctx = document.getElementById('myChart').getContext('2d');
    var chart = new Chart(ctx, {
    // The type of chart we want to create
    type: 'line',

    // The data for our dataset
    data: {
		datasets: [{
            label: "",
			backgroundColor: "lightblue",
            borderColor: 'rgb(255, 99, 132)',
            data: []
		}]
	},
    // Configuration options go here
    options: {
		title: {
			display: true,
			text: 'Valori Livello nel tempo'
		},
		scales: {
			xAxes: [{
				type: 'realtime',
				realtime: {
					duration: 20000,
					refresh: 1000,
					delay: 500,
					onRefresh: onRefresh
				}
			}],
			yAxes: [{
				scaleLabel: {
					display: true,
					labelString: 'value'
				}
			}]
		}
    }

});
});