var accordionConfig = {collapsible : true, active : 'none', clearStyle: true, autoHeight: false};

function setupMat() {
    $(document).mousemove(function(e){
        // var aMap=$("#map");
        $("#position").css("visibility",false);
        // $("#position").css("top",e.pageY+gridSize-(e.pageY%gridSize)-offsetY).css("left",e.pageX-(e.pageX%gridSize)-offsetX);
    });

    $(".token").draggable({
        grid: [gridSize,gridSize],
        stop: function (event, ui) {
            var aMap = $("#map");
            var posX = ($(this).offset().left - (aMap.offset().left + offsetX)) + 1;
            posX = (posX - posX % gridSize) / gridSize;
            var posY = ($(this).offset().top - (aMap.offset().top + offsetY)) + 1;
            posY = (posY - posY % gridSize) / gridSize;
            var name = $(this).attr("id");

            $.post(
                "/combat/" + combatName + "/move",
                {name: name, posx: posX, posy: posY},
                function (data) {
                    $("div#main").html(data);
                    setupMat();
                }
            );
        }
    });

    $(".accordion").accordion(accordionConfig);
}

$(document).ready(function (e) {
    $(".accordion").accordion(accordionConfig);

    var body = $('body');
    var nav = $('nav');
    var aMap = $("#map");

    if (!aMap.length) {
        $('head').prepend('<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1.0" />');

        return;
    }

    $('.menu .nav-trigger').click(function (e) {
        e.preventDefault();

        $('nav').toggleClass('active');
    });

    var toggleNav = function (e) {
        // Show nav if resolution allows it
        var navWidth = nav.width();
        var aMapWidth = aMap.width();
        var windowWidth = $(window).width();
        if (windowWidth > (navWidth + aMapWidth)) {
            nav.addClass('active');
        } else {
            nav.removeClass('active');
        }
    };

    aMap.load(function() {
        toggleNav();
    });

    var maxRatio = 2;

    var setScale = function() {
        var windowWidth = $(window).width();
        var screenWidth = screen.width;
        var ratio = (windowWidth / screenWidth);

        if (ratio > maxRatio) {
            ratio = maxRatio;
        }

        body.css('font-size', ratio + 'rem');

        if (screenWidth < 426) {
            nav.addClass('fixed');
            body.addClass('mobile');
            body.removeClass('mobile-xl tablet');
        } else if (screenWidth < 701) {
            nav.addClass('fixed');
            body.addClass('mobile-xl');
            body.removeClass('mobile tablet');
        } else if (screenWidth < 901) {
            nav.addClass('fixed');
            body.addClass('tablet');
            body.removeClass('mobile mobile-xl');
        } else {
            nav.removeClass('fixed');
            body.removeClass('mobile mobile-xl tablet');
        }

        var characterPortraits = $('#characters .character-name img');
        characterPortraits.each(function(index) {
            var width = parseInt($(this).attr('width'));
            var height = parseInt($(this).attr('height'));

            $(this).width(width * ratio);
            $(this).height(height * ratio);
        });

        var characterIcons = $('#characters .ui-icon-triangle-1-s, #characters .ui-icon-triangle-1-e');
        characterIcons.each(function(index) {
            var size = 21 * ratio;

            $(this).width(size);
            $(this).height(size);
            $(this).parent().find('> div').css('margin-left', (size + 5) + 'px');
        });
    };

    $(window).resize(function(){
        setScale();

        if (aMap.width() > 0) {
            toggleNav();
        }
    });

    setScale();
});
