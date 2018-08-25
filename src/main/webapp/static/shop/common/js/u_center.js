/*设置我的信息*/
setMyInfo();
function setMyInfo(data){
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
    	if(data.model.Customer.userImage!=null||data.model.Customer.userImage!=""){
			$("#roundnessSquareImg").attr('src','../..'+data.model.Customer.userImage);
			$("#localFileImg").attr('src','../..'+data.model.Customer.userImage);
		}
		$("#userName").val(data.model.Customer.userName);
		$("#userNickname").val(data.model.Customer.userNickname);
		$("#userTelphone").val(data.model.Customer.userTelphone);
		$("#userEmail").val(data.model.Customer.userEmail);
		},
    error:function(data){
    	alert("请登录");
    	
    }
	});
}
/*显示上传图像按钮*/
function change(){
	var image=$("#userface").val();
	if(image==null||image==""){
		return false;
	}
	$("#submitButton").css("display","block");
	return true;
	
}
/**/
function upload(){
	var image=$("#userface").val();
	if(image==null||image==""){
		return false;
	}
	$("#submitButton").css("display","block");
	$("#subform").ajaxSubmit({
		url:"/b2p/shop/customerCenter/uploadImage",
		type:"post",
		headers: {
	        cdutb2p_shop_token: GLOBAL_TOKEN
	    },
		dataType:"json",
		async:false,
		success:function(data){
			alert("上传成功");
			window.location.reload();
			
			
		},
	    error:function(data){
	    	alert("上传失败");
	    }
		
	});
}
