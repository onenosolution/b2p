function getQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	var r = window.location.search.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}

function want(){

	var flag = $('#want_img').attr("flag");
	if(flag == "0"){
		$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/collection/add",
		data : {
			goods_id : getQueryString("goods_id"),
		},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				$('#want_text').html("不想要");
				$('#want_img').attr("flag","1");
				$('#want_img').attr("src", "common/img/unwant.png");
				if(data['data'] < 1){
					$('#want_no').html("宝贝还没有被收藏，快来抢沙发");
				}else{
					$('#want_no').html("宝贝被收藏了"+data['data']+"次");
				}
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

		});
	}else{
		$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/collection/del",
		data : {
			goods_id : getQueryString("goods_id"),
		},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				$('#want_img').attr("flag","0");
				$('#want_text').html("想要");
				$('#want_img').attr("src", "common/img/want.png");
				if(data['data'] < 1){
					$('#want_no').html("宝贝还没有被收藏，快来抢沙发");
				}else{
					$('#want_no').html("宝贝被收藏了"+data['data']+"次");
				}
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

		});
	}
	
}
function want_init(){
	if(token != ""){
		$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/collection/nums",
		data : {
			goods_id : getQueryString("goods_id"),
		},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				if(data['data'][0] < 1){
					$('#want_no').html("宝贝还没有被收藏，快来抢沙发");
				}else{
					$('#want_no').html("宝贝被收藏了"+data['data'][0]+"次");
				}
				if(data['data'][1]){
					$('#want_text').html("不想要");
					$('#want_img').attr("flag","1");
					$('#want_img').attr("src","common/img/unwant.png");
				}else{
					$('#want_img').attr("flag","0");
					$('#want_text').html("想要");
					$('#want_img').attr("src","common/img/want.png");
				}
				
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

		});
	}else{
		//$('#want').hide();
		$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/collection/nums_no",
		data : {
			goods_id : getQueryString("goods_id"),
		},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				if(data['data'][0] < 1){
					$('#want_no').html("宝贝还没有被收藏，快来抢沙发");
				}else{
					$('#want_no').html("宝贝被收藏了"+data['data'][0]+"次");
				}
				if(token == ""){
					$('#want_text').html("想要");
					$('#want_img').attr("src","common/img/want.png");
					$('#want').click(function(){
						window.location.href = "./login.html";
					});
					
				}
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

		});
	}
	
	
}

function logout() {
	localStorage.cdutb2p_shop_token = ""
	sessionStorage.cdutb2p_shop_token = ""
	window.location.href = "./index.html";
}
function genUserInfo() {
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/user/islogin",
		data : {},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				$('#login').html("<a onclick=\"logout()\">登出</a>");
				from_id=data['data']['id'];
				from_name=data['data']['userNickname'];
				from_img=data['data']['userImage'];
				user_id = data['data']['id'];
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

	});
}
function initGoods() {
	if (getQueryString("goods_id") == null
			|| getQueryString("goods_id").length != 32) {
		window.location.href = "./index.html";
	}
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/info",
		data : {
			goods_id : getQueryString("goods_id"),
		},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				renderInitGoods(data['data']);
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

	});
}
function renderInitGoods(data) {
	$('#goods_title').html(data['goodsTitle']);
	$('#goods_img1').attr("src", getPath() + data['goodsPics']);
	$('#goods_price_pre').html(data['goodsOriginalPrice']);
	$('#goods_price_now').html(data['goodsPresentPrice']);
	$('#goods_read_times').html(data['goodsClickTimes']);
	$('#seller_nickname').html(data['goodsSellerNickname']);
	$('#seller_img').attr("src", getPath() + data['goodsSellerImg']);
	$('#seller_img2').attr("src", getPath() + data['goodsSellerImg']);
	$('#goods_desc').html(data['goodsDesc']);
	
	html = "";
	if(data['remarks'] == null){
		html += "<img src=\""+ getPath() + data['goodsPics'] +"\">";
		$('#goods_img2').html(html);
	}else{
		var pics = data['remarks'].split(",");
		for(var i in pics){
			if(pics[i] != ""){
				html += "<img src=\""+ getPath() + pics[i] +"\">";
			}
		}
		$('#goods_img2').html(html);
	}
	
	
	$('#seller_autograph').html(data['goodsSellerAutograph']);
	$('#seller_nickname1').html(data['goodsSellerNickname']);
	$('#goods_area').html(
			data['goodsProvince'] + "-" + data['goodsCity'] + "-"
					+ data['goodsArea']);
	$('#goods_img1').jqthumb({
		classname : 'jqthumb',
		width : '370px',
		height : '280px',
		position : {
			x : '50%',
			y : '50%'
		},
		source : 'src',
		show : true,
	});
	$('#contact').click(function(){
		openChat(data['goodsSellerId'],data['goodsSellerNickname'],data['goodsSellerImg']);
	});
}
function initRecommend(){
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/recommend",
		data : {},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				renderRecommend(data['data']);
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

	});
}
/*将物品加入购物车*/
function buy(){
	if( GLOBAL_TOKEN==""){
		window.location.href="./login.html";
	}
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/customerCenter/myCart",
		data : {
			goods_id : getQueryString("goods_id"),
		},
		dataType : "json",
		success : function(data) {
			if(data=="0"){
				alert("添加成功");
				window.location.href="./c_cart.html";
			}else{
				alert("商品已经存在");
			}
			
		},
		error : function(data) {
			//layer.alert('系统错误');
		}

	});
	
}
function renderRecommend(data){
	html = "";
	for(var i in data){
		html += "<li class=\"clearfix jingzhun\">"
        html += "<a href=\"./goods.html?goods_id="+data[i]['id'] +"\">"
        html += "<dl class=\"clearfix\">"
        html += "<dt><img style=\"width:142px;height:97px;display:block;margin: 0 auto;\"src=\""+getPath()+data[i]['goodsPics']+"\"></dt>"
        html += "<dd class=\"info_title\">"+data[i]['goodsTitle']+"</dd>"
        html += "<dd class=\"info_price\"><i>"+data[i]['goodsPresentPrice']+"</i>元</dd>"
        html += "</dl>"
        html += "</a>"
        html += "</li>"
	}
	$('#recomend').html(html);

}
$(document).ready(function() {
	genUserInfo();
	initGoods();
	initRecommend();
	want_init();
			
});