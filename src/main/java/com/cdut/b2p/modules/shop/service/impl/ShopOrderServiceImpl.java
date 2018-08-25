package com.cdut.b2p.modules.shop.service.impl;

import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.print.DocFlavor.STRING;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdut.b2p.common.utils.CacheUtils;
import com.cdut.b2p.common.utils.IdUtils;
import com.cdut.b2p.modules.shop.mapper.ShopCartMapper;
import com.cdut.b2p.modules.shop.mapper.ShopGoodsMapper;
import com.cdut.b2p.modules.shop.mapper.ShopOrderMapper;
import com.cdut.b2p.modules.shop.mapper.ShopUserMapper;
import com.cdut.b2p.modules.shop.mapper.ShopWalletMapper;
import com.cdut.b2p.modules.shop.po.ShopCart;
import com.cdut.b2p.modules.shop.po.ShopCartExample;
import com.cdut.b2p.modules.shop.po.ShopGoods;
import com.cdut.b2p.modules.shop.po.ShopGoodsExample;
import com.cdut.b2p.modules.shop.po.ShopOrder;
import com.cdut.b2p.modules.shop.po.ShopOrderExample;
import com.cdut.b2p.modules.shop.po.ShopOrderVo;
import com.cdut.b2p.modules.shop.po.ShopUser;
import com.cdut.b2p.modules.shop.po.ShopWallet;
import com.cdut.b2p.modules.shop.po.ShopWalletExample;
import com.cdut.b2p.modules.shop.service.ShopOrderService;
import com.sun.org.apache.xerces.internal.impl.dv.xs.DecimalDV;

@Service
@Transactional
public class ShopOrderServiceImpl implements ShopOrderService{
	@Autowired
	private ShopOrderMapper shopOrderMapper;
	@Autowired
	private ShopGoodsMapper shopGoodsMapper;
	@Autowired
	private ShopWalletMapper shopWalletMapper;
	@Autowired
	private ShopUserMapper shopUserMapper;
	@Autowired
	private ShopCartMapper shopCartMapper;
	/**
	 * @desc 上一个月成交的订单数量
	 * @author zsb
	 */
	@SuppressWarnings("deprecation")
	@Transactional(readOnly=true)
	@Override
	public Integer addOrdersCountByMonth() {
		Date date=new Date();
		int year=date.getYear();
		int month=date.getMonth();
		//上一月的开始时间[默认为每月的时间为31天]
		Date startDate=new Date(year,month,1);
		//上一月的结束时间
		Date endDate=new Date(year,month,31);
		//开始查询
		ShopOrderExample shopOrderExample=new ShopOrderExample();
		shopOrderExample.or().andCreateDateBetween(startDate, endDate);
		List<ShopOrder> list=shopOrderMapper.selectByExample(shopOrderExample);
		return list.size();
	}
	/**
	 * @desc 查询某一时间段内的用户订单
	 * @author zsb
	 * @param startDate
	 * @param enDate
	 * @return
	 */
	@Transactional(readOnly=true)
	@Override
	public List<ShopOrder> findOrderByDate(Date startDate, Date enDate) {
		ShopOrderExample example=new ShopOrderExample();
		example.or().andCreateDateBetween(startDate, enDate).andDelFlagEqualTo("0");
		List<ShopOrder> list=shopOrderMapper.selectByExample(example);
		return list;
	}
	/**
	 * @desc 查询生成的所有订单
	 * @author zsb
	 * @return
	 */
	@Transactional(readOnly=true)
	@Override
	public List<ShopOrder> findAllOrder() {
		ShopOrderExample example=new ShopOrderExample();
		example.or().andDelFlagEqualTo("0");
		List<ShopOrder> list=shopOrderMapper.selectByExample(example);
		return list;
	}
	/**
	 * @desc 根据id，更新相应的订单信息
	 * @author zsb
	 * @param shopOrder
	 * @return
	 */
	@Transactional(readOnly=false)
	@Override
	public boolean updateOrder(ShopOrder shopOrder) {
		int count=shopOrderMapper.updateByPrimaryKey(shopOrder);
		if(count>0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 根据id，删除相应的订单
	 * @author zsb
	 * @param id
	 * @return
	 */
	@Transactional(readOnly=false)
	@Override
	public boolean deleteOrder(String id) {
		ShopOrder shopOrder=new ShopOrder();
		shopOrder.setId(id);
		shopOrder.setDelFlag("1");
		int count=shopOrderMapper.updateByPrimaryKeySelective(shopOrder);
		if(count>0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 通过买方id，查询订单
	 * @param id
	 * @return
	 */
	@Transactional(readOnly=true)
	@Override
	public List<ShopOrder> findOrderByCustomer(String id) {
		ShopOrderExample example=new ShopOrderExample();
		example.or().andOrderBuyerIdEqualTo(id).andDelFlagEqualTo("0").andOrderStatusEqualTo("1");
		List<ShopOrder> list=shopOrderMapper.selectByExample(example);
		return list;
	}
	/**
	 * @desc 通过卖方id，查询订单
	 * @param id
	 * @return
	 */
	@Transactional(readOnly=true)
	@Override
	public List<ShopOrder> findOrderBySeller(String id) {
		//通过卖家id，查询商品id
		ShopGoodsExample shopGoodsExample=new ShopGoodsExample();
		shopGoodsExample.or().andGoodsSellerIdEqualTo(id).andDelFlagEqualTo("0");
		List<ShopGoods> goodsList=shopGoodsMapper.selectByExample(shopGoodsExample);
		List<String> idList=new ArrayList<>();
		for(int i=0;i<goodsList.size();i++) {
			idList.add(goodsList.get(i).getId());
		}
		ShopOrderExample example=new ShopOrderExample();
		example.or().andOrderGoodsIdIn(idList).andDelFlagEqualTo("0");
		List<ShopOrder> list=shopOrderMapper.selectByExample(example);
		return list;
	}
	/**
	 * @desc 根据ids集，删除相应的订单
	 * @param ids
	 * @return
	 */
	@Override
	public boolean deleteOrder(String[] ids) {
		List<String> idList=new ArrayList<>();
		for(int i=0;i<ids.length;i++) {
			idList.add(ids[i]);
		}
		ShopOrder shopOrder=new ShopOrder();
		shopOrder.setDelFlag("1");
		ShopOrderExample example=new ShopOrderExample();
		example.or().andIdIn(idList);
		int count=shopOrderMapper.updateByExampleSelective(shopOrder, example);
		if(count>=0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 确认id对应的商品已经达到
	 * @param id
	 * @return
	 */
	@Override
	public boolean suerOrder(String id) {
		ShopOrder shopOrder=new ShopOrder();
		shopOrder.setOrderStatus("3");
		shopOrder.setUpdateDate(new Date());
		ShopOrderExample example=new ShopOrderExample();
		example.or().andIdEqualTo(id);
		int count=shopOrderMapper.updateByExampleSelective(shopOrder, example);
		if(count>0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 用户催单
	 * @param id
	 * @return
	 */
	@Override
	public boolean reminder(String id) {
		ShopOrder shopOrder=new ShopOrder();
		shopOrder.setOrderStatus("2");
		shopOrder.setUpdateDate(new Date());
		ShopOrderExample example=new ShopOrderExample();
		example.or().andIdEqualTo(id);
		int count=shopOrderMapper.updateByExampleSelective(shopOrder, example);
		if(count>0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 展示用户的 订单信息
	 * @param uid
	 * @return
	 */
	@Override
	public List<ShopOrderVo> myOrders(String uid) {
		List<ShopOrderVo> list=shopOrderMapper.myOrders(uid);
		return list;
	}
	/**
	 * @desc 添加一个订单
	 * @param uid
	 * @param gid
	 * @return
	 */
	@Override
	public ShopOrder addOrder(String uid, String gid) {
		//根据商品id，查询商品价格
		ShopGoods goods=shopGoodsMapper.selectByPrimaryKey(gid);
		ShopOrder order=new ShopOrder();
		order.setId(IdUtils.uuid());
		order.setOrderBuyerId(uid);
		order.setOrderGoodsId(gid);
		order.setCreateDate(new Date());
		order.setOrderStatus("0");
		order.setDelFlag("0");
		order.setOrderPrice(goods.getGoodsPresentPrice());
	
		int count=shopOrderMapper.insertSelective(order);
		
		return order;
	}
	/**
	 * @desc 增加一个订单
	 */
	@Override
	public boolean addOrder(ShopOrder shopOrder) {
		shopOrder.setCreateDate(new Date());
		shopOrder.setUpdateDate(new Date());
		shopOrder.setDelFlag("0");
		shopOrder.setOrderStatus("0");
		int count=shopOrderMapper.insertSelective(shopOrder);
		if(count>0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 根据订单id，前往支付
	 */
	@Override
	public boolean pay(String id,String uid) {
		//根据订单查询，支付总价
		ShopOrder shopOrder=shopOrderMapper.selectByPrimaryKey(id);
		BigDecimal price=shopOrder.getOrderPrice();
		//根据用户id，查询钱包
		ShopUser user=shopUserMapper.selectByPrimaryKey(uid);
		String w_id=user.getUserWalletId();
		if(w_id==null||w_id=="") {
			return false;
		}
		//进行余额查询
		ShopWallet wallet=shopWalletMapper.selectByPrimaryKey(w_id);
		if(wallet.getBalance().longValue()<shopOrder.getOrderPrice().longValue()) {
			return false;
		}
		//更新订单
		ShopOrderExample example=new ShopOrderExample();
		example.or().andIdEqualTo(id);
		ShopOrder order=new ShopOrder();
		order.setOrderStatus("1");
		order.setUpdateDate(new Date());
		shopOrderMapper.updateByExampleSelective(order, example);
		
		//更新商品
		ShopOrder o = shopOrderMapper.selectByPrimaryKey(id);
		ShopGoodsExample sge=new ShopGoodsExample();
		ShopGoods goods = new ShopGoods();
		sge.or().andIdEqualTo(o.getOrderGoodsId());
		goods.setGoodsStatus("1");
		goods.setUpdateDate(new Date());
		shopGoodsMapper.updateByExampleSelective(goods, sge);
		CacheUtils.remove("goodslist");
		CacheUtils.remove("goods_id_" + o.getOrderGoodsId());
		CacheUtils.remove("goods_recommend");
		
		//更新购物车
		ShopCartExample sce=new ShopCartExample();
		ShopCart cart = new ShopCart();
		sce.or().andCartUserIdEqualTo(o.getOrderBuyerId()).andCartGoodsIdEqualTo(o.getOrderGoodsId());
		cart.setDelFlag("1");
		cart.setUpdateDate(new Date());
		shopCartMapper.updateByExampleSelective(cart, sce);
				
		
		//更新钱包表
		ShopWalletExample shopWalletExample=new ShopWalletExample();
		shopWalletExample.or().andIdEqualTo(w_id);
		ShopWallet shopWallet=new ShopWallet();
		shopWallet.setBalance(new BigDecimal(wallet.getBalance().longValue()-shopOrder.getOrderPrice().longValue()));
		shopWalletMapper.updateByExampleSelective(shopWallet, shopWalletExample);
		return true;
	}

}
