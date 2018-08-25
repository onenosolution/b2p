if(GLOBAL_TOKEN == ""){
	window.location.href="./login.html";
}
$.ajax({
	type:"POST",
	headers: {
        cdutb2p_shop_token: GLOBAL_TOKEN
    },
    dataType: "json",
    url:"/b2p/shop/sellerCenter/goods/goods_list",
    data: {},
    success:function(data){
		if(data['success']){
			render(data['data']);
		}
    	
    },
    error:function(data){
    	alert("发生错误");
    	
    }
});

function render(data){
	html = "";
	data.sort(function(a,b){
		return  b['uptime'] - a['uptime'];//时间正序
	});
	for(var i in data){
		html += "<div class=\"my-item-item\">";
		html += "<table class=\"my-item-item-tb my-item-item-hover\">"
		html += "<thead class=\"my-item-item-tb-header\"><tr><th colspan=\"2\" class=\"my-item-item-tb-code\">";
		html += "<span class=\"my-item-item-header-code\">商品id: "+data[i]['id'].substr(0,10)+"</span></th></tr></thead>";
		html += "<tbody><tr class=\"my-item-item-tb-content disp\"><td class=\"my-item-item-tb-content-img-td width90\">";
		html += "<a href=\"goods.html?goods_id="+data[i]['id']+"\" target=\"_blank\"><img  src=\""+getPath()+data[i]['goodsPics'] +"\"></a></td>";
		html += "<td class=\"width223\"><div class=\"my-item-item-tb-content-titles\">";
		html += "<p class=\"my-item-item-content-left-title\"><a target=\"_blank\"  href=\"goods.html?goods_id="+data[i]['id']+"\" class=\"t\">"+data[i]['goodsTitle'].substr(0,30)+"</a></p>";
		html += "<p class=\"my-item-item-content-left-date\">"+formatDateTime(new Date(data[i]['uptime']))+"</p>";
		html += "<p class=\"my-item-item-content-left-detail\"><a target=\"_blank\" class=\"a_xq1\">"+data[i]['goodsProvince']+"";
		html += "</a>-<a target=\"_blank\" class=\"a_xq1\">"+data[i]['goodsCity']+"</a>- <a target=\"_blank\" class=\"a_xq1\">"+data[i]['goodsArea']+"</a></p>";
		html += "</div></td><td class=\"width243\"><div class=\"my-item-item-tb-content-states\">";
		html += "<p class=\"my-item-item-tb-content-states-p1\"><span>货架显示中</span></p>";
		html += "<p class=\"my-item-item-tb-content-states-p2\">浏览 "+data[i]['goodsClickTimes']+"</p>";
		//html += "<p class=\"xsitxt\" id=\"zdmsgts_35131990758322\" style=\"display: block;\">该信息正处于置顶中，把电话充好电准备接电话吧~</p>";
		html += "</div></td><td><div class=\"my-item-item-tb-content-option\" id=\"\">";
		html += "<span><a id=\"\" href=\"./goods_edit.html?goods_id="+data[i]['id']+"\" target=\"_blank\">修改</a></span>";
		html += "<span><a onclick=\"del_goods(\'"+data[i]['id']+"\')\" id=\"\">删除</a></span>";
		//html += "<span><a onclick=\"top_goods(\'"+data[i]['id']+"\')\" class=\"commercial_link commercial_link_hyt\" >置顶</a></span>";
		html += "</div></td></tr><tr class=\"infotdno\"></tr></tbody></table></div>";
	}
	$('#my-item').html(html);
}

function del_goods(goods_id){
	$.ajax({
	type:"POST",
	headers: {
        cdutb2p_shop_token: GLOBAL_TOKEN
    },
    dataType: "json",
    url:"/b2p/shop/sellerCenter/goods/goods_del",
    data: {
		goods_id:goods_id,
	},
    success:function(data){
		alert(data['msg']);
		window.location.reload();
    },
    error:function(data){
    	alert("发生错误");
    	
    }
});
}
function top_goods(goods_id){
	
}
var formatDateTime = function (date) {
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h=h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    minute = minute < 10 ? ('0' + minute) : minute;
    var second=date.getSeconds();
    second=second < 10 ? ('0' + second) : second;
	return y + '-' + m + '-' + d+' '+h+':'+minute+':'+second;
}