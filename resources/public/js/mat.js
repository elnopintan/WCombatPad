$(".accordion").accordion();
var mapPos=null;
function reallocatePlayers () {
    $(".token").offset( function (i, pos) { return {left: pos.left +mapPos.left, 
                                                top:  pos.top + mapPos.top };});}
$("document").ready(function() { 
    var aMap=$("#map");   
    if (mapPos==null)
    {
	mapPos=aMap.offset();
    }
    reallocatePlayers();
    
});
//return {left: pos.left +$("#map").offset().left; 
//				      top: pos.top + $("#map").offset().top; }; });
function setupMat() {
    var aMap=$("#map");
     $(document).mousemove(function(e){
        var aMap=$("#map");
	$("#position").css("visibility",false);
       // $("#position").css("top",e.pageY+gridSize-(e.pageY%gridSize)-offsetY).css("left",e.pageX-(e.pageX%gridSize)-offsetX);
               });
    $(".token").draggable({ grid: [gridSize,gridSize],
                          stop: function (event, ui) {
			      var aMap=$("#map");
			      var posX=($(this).offset().left-aMap.offset().left);
			      posX=(posX-posX%gridSize)/gridSize;
			      var posY=($(this).offset().top-aMap.offset().top);
			      posY=(posY-posY%gridSize)/gridSize;
			      var name=$(this).attr("id");
			      $.post("/combat/"+combatName+"/move",
				     {name: name, posx: posX, posy: posY},
				     function (data) { $("div#main").html(data);
						       setupMat();
						       reallocatePlayers();})

}})
    $(".accordion").accordion();

}


