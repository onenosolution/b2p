$.ajax({
		type : "POST",
		headers : {
			cdutb2p_shop_token : GLOBAL_TOKEN
		},
		url : getPath() + "/shop/comment/getAll",
		data : {
			goods_id : getQueryString("goods_id"),
		},
		dataType : "json",
		success : function(data) {
			$.each(data.model.ShopCommentList, function(index, item) {	
				$("#tbody").append(	'<div class="info_baby">'+
				'<div class="baby_talk">'+
					'<p class="baby_face  clearfix">'+
						'<span class="baby_face_img"> <img src=../..'+item.userImage+' id="seller_img" alt="">'+
						'</span> <span><i id="seller_nickname"></i>'+item.userNickname+'</span>'+
					'</p>'+
					'<div class="baby_kuang clearfix">'+
						'<span class="icon_png sanjiao"></span>'+
						'<p style="width:700px;height:50px;word-break:break-all;" id="goods_desc">'+item.commentMessage+'</p>'+
					'</div>'+
				'</div>'+
			'</div>');
			});
		},
		error : function(data) {
			//layer.alert('系统错误');
		}

});
function check(){
	var comment=$("#your_comment").val();
	if(comment.length>150){
		$("#warn").html("内容不超过150字");
		return false;
	}
	$("#warn").hide();
	return true;
}
function submit(){
	var comment=$("#your_comment").val();
	if(check()){
		if(GLOBAL_TOKEN == ""){
			window.location.href="./login.html";
		}
		$.ajax({
				type : "POST",
				headers : {
					cdutb2p_shop_token : GLOBAL_TOKEN
				},
				url : getPath() + "/shop/comment/addComment",
				data : {
					goods_id : getQueryString("goods_id"),
					message:comment
				},
				dataType : "json",
				success : function(data) {
					window.location.reload();
				},
				error : function(data) {
					//layer.alert('系统错误');
				}
	
		});
	}
}
