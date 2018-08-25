var phoneid = "";
var pageNums = 1;
var startRow = 1;
var endRow = 12;
var pageSize = 1;
var G_brand;
var G_min_price;
var G_max_price;
var G_area;
var province_s = "all";
var city_s = "all";
var area_s = "all";
var brand_s = "all";
var price_s = "all";
var areaDict = null;
var G_t = 0;
var G_keyword = null;

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
				// from_id=data['data']['id'];
				// from_name=data['data']['userNickname'];
				// from_img=data['data']['userImage'];
				// user_id = data['data']['id'];
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

	});
}
function logout() {
	localStorage.cdutb2p_shop_token = ""
	sessionStorage.cdutb2p_shop_token = ""
	window.location.href = "./index.html";
}

function isPositiveInteger(s){//是否为正整数
     var re = /^[0-9]+$/ ;
     return re.test(s)
 }  
 
 function setKeyword(){
	 G_keyword = null;
	 var key = $('#keyword1').val();
	 if(key != null && key != ""){
		 G_keyword = key;
	 }
	 pageOChange(1);
 }
function setOPrice(){
	G_min_price = $('#price_start').val();
	G_max_price = $('#price_end').val();
	if(!isPositiveInteger(G_min_price) || !isPositiveInteger(G_max_price) || G_min_price > G_max_price) {
		alert('输入错误');
		G_max_price = null;
		G_max_price = null;
		return;
	}
	$('#price_' + price_s).attr("class","");
	$('#price_search').attr("class","select");
	price_s = "button";
	pageOChange(1);
}

function setPrice(x){
	if(x == price_s){
		return;
	}
	if(x == "all"){
		G_max_price = null;
		G_max_price = null;
	}
	if(x == 1){
		G_max_price = 100;
	}
	if(x == 2){
		G_min_price = 100;
		G_max_price = 200;
	}
	if(x == 3){
		G_min_price = 200;
		G_max_price = 500;
	}
	if(x == 4){
		G_min_price = 500;
		G_max_price = 1000;
	}
	if(x == 5){
		G_min_price = 1000;
		G_max_price = 2000;
	}
	if(x == 6){
		G_min_price = 2000
		G_max_price = 3500;
	}
	if(x == 7){
		G_min_price = 3500;
	}
	$('#price_' + price_s).attr("class","");
	x != "all" ? price_s = x : price_s = "all";
	$('#price_' + price_s).attr("class","select");
	pageOChange(1);
}

function setBrand(x){
	if(x == brand_s){
		return;
	}
	$('#keyword1').val("");
	G_keyword = null;
	$('#brand_' + brand_s).attr("class","");
	x != "all" ? brand_s = x : brand_s = "all";
	G_brand = x == "all" ? null : x;
	$('#brand_' + brand_s).attr("class","select");
	pageOChange(1);
}

function setProvince(x){
	if(x == province_s){
		return;
	}
	$('#province_' + province_s).attr("class","");
	$('#city_' + city_s).attr("class","");
	$('#city_all').attr("class","select");
	$('#area_' + area_s).attr("class","");
	$('#area_all').attr("class","select");
	city_s = "all";
	area_s = "all";
	x != "all" ? province_s = x : province_s = "all";
	G_area = x == "all" ? null : x;
	$('#province_' + province_s).attr("class","select");
	G_t = 2;
	genOSearch(G_area);
	pageOChange(1);
}

function setCity(x){
	if(x == city_s){
		return;
	}
	$('#city_' + city_s).attr("class","");
	$('#area_' + area_s).attr("class","");
	$('#area_all').attr("class","select");
	area_s = "all";
	x != "all" ? city_s = x : city_s = "all";
	G_area = x == "all" ? null : x;
	$('#city_' + city_s).attr("class","select");
	G_t = 3;
	genOSearch(G_area);
	pageOChange(1);
}

function setArea(x){
	if(x == area_s){
		return;
	}
	$('#area_' + area_s).attr("class","");
	x != "all" ? area_s = x : area_s = "all";
	G_area = x == "all" ? null : x;
	$('#area_' + area_s).attr("class","select");
	pageOChange(1);
}

function genSearch() {
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/area/list",
		data : {},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				renderSearch(data['data']);
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

	});
}
function genOSearch(areaid) {
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/area",
		data : {
			areaid:areaid,
		},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				renderSearch(data['data']);
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

	});
}
function renderSearch(data) {
	one = []
	two = []
	three = []
	four = []
	var p;
	var c;
	var a;
	if(G_t == 0){
		one.push(data[0]);
		for(var i in data[0]['children']){
			two.push(data[0]['children'][i]);
		}
		p = true;
		c = true;
		a = true;
	}
	if(G_t == 2){
		for ( var i in data) {
			three.push(data[i]);
		}
		p = false;
		c = true;
		a = true;
		
	}
	if(G_t == 3){
		for ( var i in data) {
			four.push(data[i]);
		}
		p = false;
		c = false;
		a = true;
		
	}
	
	html = ""
	if(p){
		html += "<a class=\"select\" onclick=\"setProvince(\'all\')\" id=\"province_all\" onhref=\"#\" >全部</a>";
		for ( var i in two) {
			html += "<a href=\"#\" onclick=\"setProvince(\'"+two[i]['id']+"\')\" id=\"province_"+two[i]['id']+"\">" + two[i]['areaName'] + "</a>";
		}
		$('#searchtwo').html(html);
	}
	
	
	html = ""
	if(c){
		html += "<a class=\"select\" onclick=\"setCity(\'all\')\" id=\"city_all\" href=\"#\" >全部</a>";
		for ( var j in three) {
			html += "<a href=\"#\" onclick=\"setCity(\'"+three[j]['id']+"\')\" id=\"city_"+three[j]['id']+"\">" + three[j]['areaName'] + "</a>";
		}
		$('#searchthree').html(html);
	}
	
	
	html = ""
	if(a){
		html += "<a class=\"select\" onclick=\"setArea(\'all\')\" id=\"area_all\" href=\"#\" >全部</a>";
		for ( var k in four) {
			html += "<a href=\"#\" onclick=\"setArea(\'"+four[k]['id']+"\')\" id=\"area_"+four[k]['id']+"\">" + four[k]['areaName'] + "</a>";
		}
		$('#searchfour').html(html);
	}
}

function genBrand() {
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/brand",
		data : {
			type : "phone_brand"
		},
		dataType : "json",
		success : function(data) {
			if (data['success']) {
				renderBrand(data['data']);
			}
		},
		error : function(data) {
			layer.alert('系统错误');
		}

	});
}
function renderBrand(data) {
	one = []
	for ( var a in data) {
		one.push(data[a])
	}
	html = ""
	html += "<a class=\"select\" onclick=\"setBrand(\'all\')\" id=\"brand_all\" href=\"#\" >全部</a>";
	for ( var i in one) {
		html += "<a href=\"#\" onclick=\"setBrand(\'"+one[i]['id']+"\')\" id=\"brand_"+one[i]['id']+"\">" + one[i]['dictLabel'] + "</a>";
	}
	$('#searchbrand').html($('#searchbrand').html() + html);

}

function initGoods(type, brand, min_price, max_price, area,pageNum, pageSize,keyword) {
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/goods/list",
		data : {
			type : type,
			brand : brand,
			min_price : min_price,
			max_price : max_price,
			area : area,
			pageNum : pageNum,
			pageSize : pageSize,
			keyword : keyword,
			
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

	html = ""
	html1 = ""
	pageNums = data['pageNum'];
	pageSize = data['pages']; 
	$('#all_goods_nums').html(data['total']);
	data = data['list'];

	for (var i = 0; i < data.length; i++) {
		if (data[i]['goodsStatus'] == "2") {
			html += "<tr class=\"zzinfo\">";
			html += "<td class=\"img\">";
			html += "<a>";
			html += "<img lazy_src=\"#\" src=\"" + getPath()
					+ data[i]['goodsPics']
					+ "\" height=\"130\" width=\"130\" style=\"top: 0px; left: 0px;\" />";
			html += "</a>";
			html += "</td>";
			html += "<td class=\"t\">";
			html += "<a href=\"goods.html?goods_id=" + data[i]['id']
					+ "\" class=\"t\">" + data[i]['goodsTitle'];
			html += "<spanclass=\"zhiding_icon\">顶</span></a> <i class=\"clear\"></i> ";
			html += "<span class=\"pricebiao\">￥<span class=\"price\">"
					+ data[i]['goodsPresentPrice'] + "</span></span>";
			html += "<i class=\"clear\"></i> ";
			html += "<div class=\"quot\"></div> <span class=\"desc\">"
					+ data[i]['goodsDesc'] + "</span>";
			html += "<i class=\"clear\"></i>";
			html += "<span class=\"fl\"> ";
			html += "<span>" + data[i]['goodsCity']
					+ "</span><span>- </span><span>" + data[i]['goodsArea']
					+ "</span>";
			html += "</span> <i class=\"clear\"></i>";
			html += "</td>";
			html += "<td></td>";
			html += "<td class=\"tc\">";
			html += "<div class=\"qq_attest\">";
			html += "<p class=\"img_attest\">";
			html += "<img lazy_src=\"\" src=\"" + getPath()
					+ data[i]['goodsSellerImg'] + "\" />";
			html += "</p>";
			html += "<p class=\"name_add\">" + data[i]['goodsSellerNickname']
					+ "</p>";
			html += "<p class=\"zhijian\"></p></div>";
			html += "</td>";
			html += "<td></td>";
			html += "</tr>";

		} else if (data[i]['goodsStatus'] == "0") {
			html1 += "<tr class=\"zzinfo\">";
			html1 += "<td class=\"img\">";
			html1 += "<a>";
			html1 += "<img lazy_src=\"#\" src=\"" + getPath()
					+ data[i]['goodsPics']
					+ "\" height=\"130\" width=\"130\" style=\"top: 0px; left: 0px;\" />";
			html1 += "</a>";
			html1 += "</td>";
			html1 += "<td class=\"t\">";
			html1 += "<a href=\"goods.html?goods_id=" + data[i]['id']
					+ "\" class=\"t\">" + data[i]['goodsTitle'];
			html1 += "</a> <i class=\"clear\"></i> ";
			html1 += "<span class=\"pricebiao\">￥<span class=\"price\">"
					+ data[i]['goodsPresentPrice'] + "</span></span>";
			html1 += "<i class=\"clear\"></i> ";
			html1 += "<div class=\"quot\"></div> <span class=\"desc\">"
					+ data[i]['goodsDesc'] + "</span>";
			html1 += "<i class=\"clear\"></i>";
			html1 += "<span class=\"fl\"> ";
			html1 += "<span>" + data[i]['goodsCity']
					+ "</span><span>- </span><span>" + data[i]['goodsArea']
					+ "</span>";
			html1 += "</span> <i class=\"clear\"></i>";
			html1 += "</td>";
			html1 += "<td></td>";
			html1 += "<td class=\"tc\">";
			html1 += "<div class=\"qq_attest\">";
			html1 += "<p class=\"img_attest\">";
			html1 += "<img lazy_src=\"\" src=\"" + getPath()
					+ data[i]['goodsSellerImg'] + "\" />";
			html1 += "</p>";
			html1 += "<p class=\"name_add\">" + data[i]['goodsSellerNickname']
					+ "</p>";
			html1 += "<p class=\"zhijian\"></p></div>";
			html1 += "</td>";
			html1 += "<td></td>";
			html1 += "</tr>";
		}
	}
	$('#toptbody').html(html);
	$('#tbody').html(html1);
	renderPage();
	
}

function renderPage() {
	if(endRow > pageSize) endRow = pageSize;
	html = "";
	var fix = 0;
	if(pageNums - startRow > 6 && endRow - pageNums <  6){
		startRow += 1;
		endRow += 1;
	}
	if(pageNums != 1){
		html += "<a class=\"last\"><span onclick=\"pageChange("+(pageNums-1)+")\">上一页</span></a>";
	}
	for(var i = startRow; i < endRow; i++){
		if(i == pageNums){
			html += "<strong><span onclick=\"pageChange("+(i)+")\">"+ (i) +"</span></strong>";
		}else{
			html += "<a><span onclick=\"pageChange("+(i)+")\">"+ (i) +"</span></a>";
		}
	}
	if(pageNums != pageSize){
		html += "<a class=\"next\"><span onclick=\"pageChange("+(pageNums+1)+")\">下一页</span></a>";
	}
	$('#pager').html(html);
}
function pageChange(pageNum) {
	initGoods("phone",G_brand,G_min_price,G_max_price,G_area,pageNum,30,G_keyword);
}

function pageOChange(pageNum) {
	endRow = 12;
	startRow = 1;
	initGoods("phone",G_brand,G_min_price,G_max_price,G_area,pageNum,30,G_keyword);
}

$(document).ready(function() {
	genUserInfo();
	genSearch();
	genBrand();
	initGoods("phone",null,null,null,null,1, 30,null);
});