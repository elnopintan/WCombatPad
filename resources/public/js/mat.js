$(".accordion").accordion();

function setupMat() {
    $(document).mousemove(function(e){
        var aMap=$("#map");
$("#position").css("visibility",false);
       // $("#position").css("top",e.pageY+gridSize-(e.pageY%gridSize)-offsetY).css("left",e.pageX-(e.pageX%gridSize)-offsetX);
               });
    $(".token").draggable({ grid: [gridSize,gridSize],
                          stop: function (event, ui) {
			      var aMap=$("#map");
			      var posX=($(this).offset().left-aMap.offset().left)+1;
			      posX=(posX-posX%gridSize)/gridSize;
			      var posY=($(this).offset().top-aMap.offset().top)+1;
			      posY=(posY-posY%gridSize)/gridSize;
			      var name=$(this).attr("id");
			      $.post("/combat/"+combatName+"/move",
				     {name: name, posx: posX, posy: posY},
				     function (data) { $("div#main").html(data);
						       setupMat();})

			  }})
    $(".accordion").accordion();
}

