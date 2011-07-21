$(function(){
    $(document).mousemove(function(e){
        var aMap=$("#map");
	$("#position").css("visibility",false);
       // $("#position").css("top",e.pageY+gridSize-(e.pageY%gridSize)-offsetY).css("left",e.pageX-(e.pageX%gridSize)-offsetX);
               });
    $(".token").draggable({grid: [gridSize,gridSize]});});