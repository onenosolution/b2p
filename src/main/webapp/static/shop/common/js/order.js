var area_stage=0;
var one=[];
var two=[];
var three=[];
var four=[];
var area_select_id;
var oder_price;
$(document).ready(function() {
	initAccount();
	init();
	getMyAddress();
	genArea();
			
});
function init() {
	var goods_id=getQueryString("goods_id");
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		dataType : "json",
		url : "/b2p/shop/order/findGoodsById",
		data : {
			goods_id : goods_id,
		},
		success : function(data) {
			setData(data);
		},
		error : function(data) {
			window.location.href = "./login.html";
		}
	});
}
function initAccount(){
	$.ajax({
		type:"POST",
		headers: {
	        cdutb2p_shop_token: GLOBAL_TOKEN
	    },
	    dataType: "json",
	    url:"/b2p/shop/customerCenter/myInfo",
	    data: {},
	    success:function(data){
			if(GLOBAL_TOKEN == ""){
				window.location.href="./login.html";
			}
	    	if(data.model.Customer.userNickname!=null||data.model.Customer.userNickname!=""){
	    		$("#userNameOne").html(data.model.Customer.userNickname);
	    	}
	    },
	    error:function(data){
	    }
	});
}
function getQueryString(name) {
	var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
	var r = window.location.search.substr(1).match(reg);
	if (r != null)
		return unescape(r[2]);
	return null;
}
/*获取我的已有地址*/
function getMyAddress(){
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		dataType : "json",
		url : "/b2p/shop/address/getAll",
		data : {},
		success : function(data) {
			$.each(data.model.AddressList, function(index, item) {	
				$("#address").append('<div class="address-add" onclick="choose('+index+')">'+
						'<div class="address-new">'+
						'<i class="fa fa-chain" id="chain'+index+'"></i>'+
						'<i class="fa fa-check" syle="display:none;" id="check'+index+'"></i>'+
						'<div class="text">'+item.addressReceiverName+'<span style="width:10px;">&&<span>'+item.addressDetail+'</div>'+
					    '<input type="hidden" value="'+item.id+'" id="id'+index+'">'+
						'</div>'+
				    '</div>'
		    );
			});
		},
		error : function(data) {
			window.location.href = "./login.html";
		}
	});
}
/*设置相应的值*/
function setData(data){
	$("#goods_img").attr('src',"../.."+data.model.ShopGoods.goodsPics);
	$("#goods_title").html(data.model.ShopGoods.goodsTitle);
	$("#goods_price").html("￥"+data.model.ShopGoods.goodsPresentPrice);
	$("#goods_total").html("￥"+data.model.ShopGoods.goodsPresentPrice);
	$("#order_price").html("￥"+data.model.ShopGoods.goodsPresentPrice);
	$("#order_id").val(data.model.ShopGoods.id);
	order_price=data.model.ShopGoods.goodsPresentPrice;
}
/*添加地址*/
function address(){
	$("#increAddress").css("display","block");
}
/*设置一个全局变量*/
var clickCount=0;
/*设置选择地址的id*/
var address_id;
/*选择某个地址*/
function choose(index){
	if(clickCount==index){
		$("#chain"+clickCount).hide();
		$("#check"+clickCount).css({"display":"block","color":"red"});
		address_id=$("#id"+clickCount).val();
	}
	else{
		/*覆盖*/
		$("#chain"+clickCount).css("display","block");
		$("#check"+clickCount).hide();
		/*设置*/
		clickCount=index;
		$("#chain"+clickCount).hide();
		$("#check"+clickCount).css({"display":"block","color":"red"});
		address_id=$("#id"+clickCount).val();
	}
}
/*关闭新增的地址模拟框*/
function closeAddress(){
	$("#increAddress").css("display","none");
}
function sureAddress(){
	var flag=false;
	var id;
	//alert("保存新地址");
	//alert(area_select_id);
	var addressDetail=$("#addressDetail").val();
	var addressReceiverName=$("#addressReceiverName").val();
	var addressTelphone=$("#addressTelphone").val();
	var addressZipcode=$("#addressZipcode").val();
	if(addressDetail==""||addressReceiverName==""||addressTelphone==""){
		return false;
	}
	if(area_select_id!=null||area_select_id!=""){
		$.ajax({
			type : "POST",
			headers : {
				cdutb2p_shop_token : GLOBAL_TOKEN
			},
			dataType : "json",
			url : "/b2p/shop/address/add",
			data : {
				addressAreaId : area_select_id,
				addressDetail:addressDetail,
				addressReceiverName:addressReceiverName,
				addressTelphone:addressTelphone,
				addressZipcode,addressZipcode,	
			},
			success : function(data) {
				alert("地址添加成功");
				id=data;
				closeAddress();
				$("#address").append('<div class="address-add">'+
								'<div class="address-new">'+
								'<i class="fa fa-chain"></i>'+
								'<div class="text">'+addressReceiverName+'<span style="width:10px;">&&<span>'+addressDetail+'</div>'+
							    '<input type="hidden" value="'+id+'">'+
								'</div>'+
						    '</div>'
				);
					
				
			},
			error : function(data) {
				window.location.href = "./login.html";
			}
		});
	}
	
}
/*确认订单*/
function sureOrder(){
	
	var order_id=$("#order_id").val();
	$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		dataType : "json",
		url : "/b2p/shop/order/pay",
		data : {
			orderGoodsId:order_id,
			orderAddress:address_id,
			orderPrice:order_price,
		},
		success : function(data) {
			//alert("购买成功");
			if(data=="yes"){
				$("#isSuccess").css("display","block");
				$("#tishi").css("color","blue");
				$("#tishi").html("支付成功");
			}
			else if(data=="no"){
				$("#isSuccess").css("display","block");
				$("#tishi").css("color","red");
				$("#tishi").html("余额不足");
			}
		},
		error : function(data) {
			window.location.href = "./login.html";
		}
	});
	
}

/*获取区域地址*/
function genArea() {
	
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
				renderArea(data['data']);
			}
		},
		error : function(data) {
			//layer.alert('系统错误');
		}

	});
	$('#select_area_province').change(function(){
		area_stage = 1;
		renderArea();
	});
	$('#select_area_city').change(function(){
		area_stage = 2;
		renderArea();
	});
	$('#select_area_area').change(function(){
		area_select_id = $('#select_area_area').find("option:selected").attr("value");
	});
}

function renderArea(data) {
	if(area_stage == 0){
		one.push(data[0]);
		for(var i in data[0]['children']){
			two.push(data[0]['children'][i]);
			for(var j in data[0]['children'][i]['children']){
				three.push(data[0]['children'][i]['children'][j]);
				for(var k in data[0]['children'][i]['children'][j]['children']){
					four.push(data[0]['children'][i]['children'][j]['children'][k]);
				}
			}
		}
		html = "";
		for(var i in two){
			if(i == 0){
				html += "<option selected value=\""+two[i]['id']+"\">"+two[i]['areaName']+"</option>";
			}else{
				html += "<option value=\""+two[i]['id']+"\">"+two[i]['areaName']+"</option>";
			}
			
		}
		$('#select_area_province').html(html);
		html = "";
		for(var i in three){
			if(three[i]['areaParentId'] == $('#select_area_province').find("option:selected").attr("value")){
				if(i == 0){
					html += "<option selected value=\""+three[i]['id']+"\">"+three[i]['areaName']+"</option>";
				}else{
					html += "<option value=\""+three[i]['id']+"\">"+three[i]['areaName']+"</option>";
				}
				
			}
		}
		$('#select_area_city').html(html);
		html = "";
		for(var i in four){
			if(four[i]['areaParentId'] == $('#select_area_city').find("option:selected").attr("value")){
				if(i == 0){
					html += "<option selected value=\""+four[i]['id']+"\">"+four[i]['areaName']+"</option>";
					area_select_id = four[i]['id'];
				}else{
					html += "<option value=\""+four[i]['id']+"\">"+four[i]['areaName']+"</option>";
				}
				
			}
		}
		$('#select_area_area').html(html);
		
		
	}
	if(area_stage == 1){
		html = "";
		for(var i in three){
			if(three[i]['areaParentId'] == $('#select_area_province').find("option:selected").attr("value")){
				html += "<option value=\""+three[i]['id']+"\">"+three[i]['areaName']+"</option>";
			}
		}
		$('#select_area_city').html(html);
		html = "";
		for(var i in four){
			if(four[i]['areaParentId'] == $('#select_area_city').find("option:selected").attr("value")){
				html += "<option value=\""+four[i]['id']+"\">"+four[i]['areaName']+"</option>";
			}
		}
		$('#select_area_area').html(html);
	}
	if(area_stage == 2){
		html = "";
		for(var i in four){
			if(four[i]['areaParentId'] == $('#select_area_city').find("option:selected").attr("value")){
				html += "<option value=\""+four[i]['id']+"\">"+four[i]['areaName']+"</option>";
			}
		}
		$('#select_area_area').html(html);
	}
}	
	
	
	