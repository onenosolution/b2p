package com.cdut.b2p.modules.shop.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdut.b2p.common.utils.IdUtils;
import com.cdut.b2p.modules.shop.mapper.ShopCartMapper;
import com.cdut.b2p.modules.shop.po.ShopCart;
import com.cdut.b2p.modules.shop.po.ShopCartExample;
import com.cdut.b2p.modules.shop.po.ShopCartVo;
import com.cdut.b2p.modules.shop.service.ShopCartService;
import com.sun.corba.se.impl.presentation.rmi.IDLTypesUtil;

@Service
@Transactional
public class ShopCartServiceImpl  implements ShopCartService{
	@Autowired
	private ShopCartMapper shopCartMapper;
	/**
	 * @desc 在购物车中添加一件物品
	 * @param shopCart
	 * @return
	 */
	@Transactional(readOnly=false)
	@Override
	public boolean addCart(ShopCart shopCart) {
		shopCart.setId(IdUtils.uuid());
		shopCart.setCreateDate(new Date());
		shopCart.setUpdateDate(new Date());
		shopCart.setDelFlag("0");
		int count=shopCartMapper.insertSelective(shopCart);
		if(count>0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 在购物车中添加一件物品
	 * @param uid 用户id
	 * @param gid 商品id
	 * @return
	 */
	@Transactional(readOnly=false)
	@Override
	public boolean addCart(String uid, String gid) {
		//ShopCart c = shopCartMapper.selectByPrimaryKey(gid)
		ShopCartExample sce = new ShopCartExample();
		sce.or().andCartGoodsIdEqualTo(gid).andCartUserIdEqualTo(uid);
		List<ShopCart> c= shopCartMapper.selectByExample(sce);
		if(c.size() > 0) {
			return false;
		}
		
		ShopCart shopCart=new ShopCart();
		shopCart.setId(IdUtils.uuid());
		shopCart.setCartUserId(uid);
		shopCart.setCartGoodsId(gid);
		shopCart.setCreateDate(new Date());
		shopCart.setUpdateDate(new Date());
		shopCart.setDelFlag("0");
		int count=shopCartMapper.insertSelective(shopCart);
		if(count>0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 根据用户id,查询购物车 
	 * @param uid
	 * @return
	 */
	@Transactional(readOnly=true)
	@Override
	public List<ShopCartVo> findCartByUser(String uid) {
		ShopCartExample example=new ShopCartExample();
		example.or().andCartUserIdEqualTo(uid).andDelFlagEqualTo("0");
		List<ShopCartVo> list=shopCartMapper.selectMyCart(uid);
		return list;
	}
	/**
	 * @desc 根据ids集，删除对应的购物车记录
	 * @param ids
	 * @return
	 */
	@Transactional(readOnly=false)
	@Override
	public boolean delCart(String[] ids) {
		List<String> id=new ArrayList<>();
		for(int i=0;i<ids.length;i++) {
			id.add(ids[i]);
		}
		ShopCartExample example=new ShopCartExample();
		example.or().andIdIn(id);
		ShopCart shopCart=new ShopCart();
		shopCart.setDelFlag("1");
		shopCart.setUpdateDate(new Date());
		int count=shopCartMapper.updateByExampleSelective(shopCart, example);
		if(count>0) {
			return true;
		}
		return false;
	}
	/**
	 * @desc 根据购物车的id，查询相应的记录信息
	 * @param id
	 * @return
	 */
	@Override
	public ShopCart findCartById(String id) {
		ShopCart cart=shopCartMapper.selectByPrimaryKey(id);
		return cart;
	}

}
