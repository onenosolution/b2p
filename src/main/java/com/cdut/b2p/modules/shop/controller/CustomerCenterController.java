package com.cdut.b2p.modules.shop.controller;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.print.DocFlavor.STRING;
import javax.security.auth.message.callback.PrivateKeyCallback.Request;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.types.FileList.FileName;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * @title CustomerCenterController
 * @desc  CustomerCenterController是一个买家中兴处理类
 */
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.cdut.b2p.common.config.Global;
import com.cdut.b2p.common.controller.BaseController;
import com.cdut.b2p.common.utils.IdUtils;
import com.cdut.b2p.common.utils.SecurityUtils;
import com.cdut.b2p.modules.shop.po.ShopAddress;
import com.cdut.b2p.modules.shop.po.ShopCart;
import com.cdut.b2p.modules.shop.po.ShopCartVo;
import com.cdut.b2p.modules.shop.po.ShopCollection;
import com.cdut.b2p.modules.shop.po.ShopCollectionVo;
import com.cdut.b2p.modules.shop.po.ShopGoods;
import com.cdut.b2p.modules.shop.po.ShopGoodsInfo;
import com.cdut.b2p.modules.shop.po.ShopOrder;
import com.cdut.b2p.modules.shop.po.ShopOrderVo;
import com.cdut.b2p.modules.shop.po.ShopUser;
import com.cdut.b2p.modules.shop.po.ShopWorkorder;
import com.cdut.b2p.modules.shop.security.annotation.ShopAuth;
import com.cdut.b2p.modules.shop.service.ShopCartService;
import com.cdut.b2p.modules.shop.service.ShopCollectionService;
import com.cdut.b2p.modules.shop.service.ShopGoodsService;
import com.cdut.b2p.modules.shop.service.ShopOrderService;
import com.cdut.b2p.modules.shop.service.ShopUserService;
import com.cdut.b2p.modules.shop.service.ShopWorkorderService;

import jdk.internal.dynalink.support.RuntimeContextLinkRequestImpl;

@Controller
@RequestMapping("${shopPath}/customerCenter")
public class CustomerCenterController extends BaseController{
	@Autowired
	private ShopUserService shopUserService;
	@Autowired
	private ShopOrderService shopOrderService;
	@Autowired
	private ShopGoodsService shopGoodsService;
	@Autowired
	private ShopWorkorderService shopWorkorderService;
	@Autowired
	private ShopCollectionService shopCollectionService;
	@Autowired
	private ShopCartService shopCartService;
	/**
	 * @desc 查询个人信息
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping(value="/myInfo",method=RequestMethod.POST)
	public String getMyInfo(HttpServletResponse response,HttpServletRequest request) {
		ModelAndView model=new ModelAndView();
		String uid=(String) request.getAttribute("uid");
		ShopUser user=shopUserService.findUserById(uid);
		model.addObject("Customer", user);
		System.err.println(user);
		return renderString(response, model); 
	}

	/**
	 * @desc 修改个人密码
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping(value="/myPWD",method=RequestMethod.POST)
	public String myPWD(HttpServletResponse response,HttpServletRequest request ) {
		ModelAndView model=new ModelAndView();
		System.out.println("修改密码");
		String uid=(String) request.getAttribute("uid");
		String oldpwd=request.getParameter("oldPWD");
		String newpwd=request.getParameter("newPWD");
		System.out.println(uid+"\n"+oldpwd+"\n"+newpwd);
		ShopUser user=shopUserService.findUserById(uid);
		if(!SecurityUtils.getMD5(oldpwd).equals(user.getUserPassword())) {
			model.addObject("PWDMessage", "no");
			return renderString(response, model);
		}
		
		shopUserService.updatePWD(uid, newpwd);
		model.addObject("PWDMessage", "yes");
		return renderString(response, model);
	}
	@ShopAuth
	@RequestMapping(value="/uploadImage",method=RequestMethod.POST)
	public String updateImage(@RequestParam(value="file") MultipartFile file,HttpServletResponse response,HttpServletRequest request) throws IllegalStateException, IOException {
		
		ModelAndView model=new ModelAndView();
		String uid=(String) request.getAttribute("uid");
		String path = request.getContextPath();
		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
		// 获取文件上传的真实路径
		String uploadPath = request.getSession().getServletContext().getRealPath("/");
		// 上传文件过程

		String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);

		String destDir = Global.getUserfilesBaseDir();
		File destFile = new File(uploadPath + destDir);
		if (!destFile.exists()) {
			destFile.mkdirs();
		}
		String fileName =  uid + "." + suffix;
		File f = new File(destFile.getAbsoluteFile() + File.separator + fileName);
		
		file.transferTo(f);
		f.createNewFile();
		//进行数据更新
		shopUserService.updateImage(uid, destDir + fileName);
		model.addObject("Message", "yes");
		return renderString(response, model);
	}
	/**
	 * @desc 查询我的交易记录
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping(value="/myOrder",method=RequestMethod.POST)
	public String getMyOrder(HttpServletResponse response,HttpServletRequest request) {
		System.out.println("查询我的订单信息");
		ModelAndView model=new ModelAndView();
		String uid=(String) request.getAttribute("uid");
		List<ShopOrderVo> orderList=shopOrderService.myOrders(uid);
		System.out.println("查询我的订单信息"+orderList.size());
		model.addObject("OrderVoList", orderList);
		return renderString(response, model); 
	}
	/**
	 * @desc 买家催单
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/reminder",method=RequestMethod.POST)
	public String reminder(HttpServletResponse response,HttpServletRequest request) {
		String id=request.getParameter("oid");
		shopOrderService.reminder(id);
		return renderString(response, "催单成功");
	}
	/**
	 * @desc 确认商品到达
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/sureOrder",method=RequestMethod.POST)
	public String sureOrder(HttpServletResponse response,HttpServletRequest request) {
		String id=request.getParameter("oid");
		shopOrderService.suerOrder(id);
		return renderString(response, "确认到达");
	}
	/**
	 * @desc 删除我的订单[一或多]
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delMyOrder",method=RequestMethod.POST)
	public String delMyOrder(HttpServletResponse response,HttpServletRequest request) {
		ModelAndView model=new ModelAndView();
		String[]oids=request.getParameterValues("oid");
		shopOrderService.deleteOrder(oids);
		model.addObject("OrderMessage", "删除成功！");
		return renderString(response, model);
	}
	
	/**
	 * @desc 投诉
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping(value="/myComplaint",method=RequestMethod.POST)
	public String myComplaint(HttpServletResponse response,HttpServletRequest request,ShopWorkorder shopWorkorder) {
		String uid=(String) request.getAttribute("uid");
		String gid=request.getParameter("gid");//从订单上获取到的商品id
		String seller_id=shopGoodsService.findSellerId(gid);
		shopWorkorder.setWorkorderUserId(uid);
		shopWorkorder.setWorkorderReceiverId(seller_id);
		shopWorkorder.setCreateDate(new Date());
		shopWorkorder.setUpdateDate(new Date());
		shopWorkorderService.addShopWorkOrder(shopWorkorder);
		//添加提示信息
		return renderString(response, "投诉成功");
	}
	/**
	 * @desc 用户添加收藏物
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping(value="/myCollection",method=RequestMethod.POST)
	public String myFavorite(HttpServletResponse response,HttpServletRequest request) {
		String uid=(String) request.getAttribute("uid");
		String gid=request.getParameter("gid");
		shopCollectionService.addCollection(uid, gid);
		return renderString(response, "收藏成功！");
	}
	/**
	 * @desc 查询我的收藏物
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping(value="/selectMyCollection",method=RequestMethod.POST)
	public String selectMyFavorite(HttpServletResponse response,HttpServletRequest request) {
		System.out.println("查询我的收藏物");
		ModelAndView  model=new  ModelAndView();
		String uid=(String) request.getAttribute("uid");
		System.out.println(uid);
		List<ShopCollectionVo> list=shopCollectionService.selectMyCollection(uid);
		System.out.println(list);
		model.addObject("CollectionList", list);
		return renderString(response, model);
	}
	/**
	 * @desc 删除我的收藏物【一或多】
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delMyCollection",method=RequestMethod.POST)
	public String delMyFavorite(HttpServletResponse response,HttpServletRequest request,@RequestParam(value="array[]") String[]array) {
		System.out.println("修改收藏物记录");
		shopCollectionService.delCollection(array);
		return renderString(response, "删除成功");
	}
	
	
	
	/**
	 * @desc 查询用户所接受到的订单消息
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping(value="/selectMyMessage",method=RequestMethod.POST)
	public String selectMyMessage(HttpServletResponse response,HttpServletRequest request) {
		ModelAndView model=new ModelAndView();
		String uid=(String) request.getAttribute("uid");
		List<ShopWorkorder> list=shopWorkorderService.findWorkOrderByUser(uid);
		model.addObject("MessageList", list);
		return renderString(response, model);
	}
	/**
	 * @desc 删除某些消息
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delMyMessage",method=RequestMethod.POST)
	public String delMyMessage(HttpServletResponse response,HttpServletRequest request) {
		String id=request.getParameter("w_id");
		shopWorkorderService.delWorkOrderById(id);
		return renderString(response, "删除成功");
	}
	
	

	
	/**
	 * @desc 添加商品到购物车
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping(value="/myCart",method=RequestMethod.POST)
	public String myCart(HttpServletResponse response,HttpServletRequest request) {
		String uid=(String) request.getAttribute("uid");
		String gid=request.getParameter("goods_id");
		boolean rs = shopCartService.addCart(uid, gid);
		if(rs) {
			return renderString(response, "添加成功");
		}else {
			return renderString(response, "已经存在");
		}
		
	}
	/**
	 * @desc 查询我的购物车
	 * @param response
	 * @param request
	 * @return
	 */
	@ShopAuth
	@RequestMapping("/selectMyCart")
	public String selectMyCart(HttpServletResponse response,HttpServletRequest request) {
		String uid=(String) request.getAttribute("uid");
		ModelAndView model=new ModelAndView();
		List<ShopCartVo> list=shopCartService.findCartByUser(uid);
		model.addObject("CartList", list);
		return renderString(response, model);
	}
	/**
	 * @desc 删除购物车中的某些商品
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/delMyCart",method=RequestMethod.POST)
	public String delMyCart(HttpServletResponse response,HttpServletRequest request) {
		String id=request.getParameter("c_id");
		String[]ids= {id};
		System.out.println("删除购物车："+id);
		shopCartService.delCart(ids);
		return renderString(response, "删除成功");
	}
	/**
	 * @desc 对选中的物品，进行对比功能
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/compareMyCart",method=RequestMethod.POST)
	public String compareMyCart(HttpServletResponse response,HttpServletRequest request) {
		ModelAndView model=new ModelAndView();
		String id=request.getParameter("c_id");
		ShopCart cart=shopCartService.findCartById(id);
		//获取商品id
		ShopGoodsInfo goods=shopGoodsService.findGoodsByGoodsId(cart.getCartGoodsId(), false);
		//对比功能->价格
		List<ShopGoods> list=shopGoodsService.compareGoods(goods);
		System.out.println("对比商品:"+list.size());
		System.out.println(list.get(0));
		model.addObject("ShopGoodsList", list);
		return renderString(response, model);
	}
	@RequestMapping(value="/show",method=RequestMethod.POST)
	public String show(HttpServletResponse response,HttpServletRequest request) {
		System.out.println("进行第二种方式对比");
		ModelAndView model=new ModelAndView();
		String last_id=request.getParameter("last_id");
		String now_id=request.getParameter("now_id");
		ShopCart cart1=shopCartService.findCartById(last_id);
		ShopCart cart2=shopCartService.findCartById(now_id);
		System.out.println(last_id+"\n"+now_id);
		ShopGoodsInfo info1=shopGoodsService.findGoodsByGoodsId(cart1.getCartGoodsId(), false);
		ShopGoodsInfo info2=shopGoodsService.findGoodsByGoodsId(cart2.getCartGoodsId(), false);
		model.addObject("ShopGoodsInfo1", info1);
		model.addObject("ShopGoodsInfo2", info2);
		//对比功能->价格
		return renderString(response, model);
	}
}
