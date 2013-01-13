$(document).ready(function () {
    $("#nav > li").hover(function () {
        $(this).removeClass("ui-state-default");
        $(this).addClass("ui-state-hover");
    }, function () {
        $(this).removeClass("ui-state-hover");
        $(this).addClass("ui-state-default");
    });
});