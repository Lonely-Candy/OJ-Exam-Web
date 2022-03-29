$(function(){
    // 菜单点击
    $(".J_menuItem").on('click',function(){
        var url = $(this).attr('href');
        console.log(url);
        $("#J_iframe").attr('src',url);
        return false;
    });
});

