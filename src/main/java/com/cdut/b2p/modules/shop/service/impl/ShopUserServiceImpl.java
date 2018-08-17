package com.cdut.b2p.modules.shop.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdut.b2p.common.utils.IdUtils;
import com.cdut.b2p.common.utils.SecurityUtils;
import com.cdut.b2p.common.utils.StringUtils;
import com.cdut.b2p.modules.shop.mapper.ShopUserMapper;
import com.cdut.b2p.modules.shop.mapper.ShopWalletMapper;
import com.cdut.b2p.modules.shop.po.ShopUser;
import com.cdut.b2p.modules.shop.po.ShopUserExample;
import com.cdut.b2p.modules.shop.po.ShopWallet;
import com.cdut.b2p.modules.shop.service.ShopUserService;
import com.cdut.b2p.modules.sys.po.SysDict;
import com.cdut.b2p.modules.sys.po.SysDictExample;
import com.cdut.b2p.modules.sys.po.SysUser;

@Service
@Transactional
public class ShopUserServiceImpl implements ShopUserService{
	
	@Autowired
	private ShopUserMapper shopUserMapper;
	@Autowired
	private ShopWalletMapper shopWalletMapper;
	
	private void preInsertWallet(ShopWallet shopWallet) {
		shopWallet.setUpdateDate(new Date());
		shopWallet.setCreateDate(shopWallet.getUpdateDate());
	}
	
	private void preInsertUser(ShopUser shopUser) {
		shopUser.setUpdateDate(new Date());
		shopUser.setCreateDate(shopUser.getUpdateDate());
	}
	
	@Transactional(readOnly = false)
	@Override
	public void saveUser(ShopUser shopUser) {
		preInsertUser(shopUser);
		shopUserMapper.insertSelective(shopUser);
	}

	@Transactional(readOnly = false)
	@Override
	public void saveWallet(ShopWallet shopWallet) {
		preInsertWallet(shopWallet);
		shopWalletMapper.insertSelective(shopWallet);
	}

	@Transactional(readOnly = true)
	@Override
	public ShopUser findUserByUsername(String username) {
		ShopUserExample sue = new ShopUserExample();
		sue.or().andUserNameEqualTo(username);
		List<ShopUser> list = shopUserMapper.selectByExample(sue);
		return (list == null || list.isEmpty()) ? null : list.get(0);
	}
	
	@Transactional(readOnly = true)
	@Override
	public ShopUser findUserByEmail(String email) {
		ShopUserExample sue = new ShopUserExample();
		sue.or().andUserEmailEqualTo(email);
		List<ShopUser> list = shopUserMapper.selectByExample(sue);
		return (list == null || list.isEmpty()) ? null : list.get(0);
	}
	
	@Transactional(readOnly = true)
	@Override
	public ShopUser findUserByNickname(String nickname) {
		ShopUserExample sue = new ShopUserExample();
		sue.or().andUserNicknameEqualTo(nickname);
		List<ShopUser> list = shopUserMapper.selectByExample(sue);
		return (list == null || list.isEmpty()) ? null : list.get(0);
	}
	
	/**
	 * @desc 查询上一个月增加的用户数
	 */
	@Override
	public Integer addUserCountByMonth() {
		Date date=new Date();
		int year=date.getYear()>100?2000+date.getYear()%100:date.getYear();
		int month=date.getMonth();
		int day=date.getDate();
		//上一月的开始时间[默认为每月的时间为31天]
		Date startDate=new Date(year,month,1);
		//上一月的结束时间
		Date endDate=new Date(year,month,31);
		//
		ShopUserExample example=new ShopUserExample();
		example.or().andCreateDateBetween(startDate, endDate);
		List<ShopUser> list=shopUserMapper.selectByExample(example);
		return list.size();
	}
	/**
	 * @desc 查询上一个月内，浏览网站的游客数量【只注册了，为进行商品买卖行为】
	 * @author zsb
	 * @return
	 */
	@Override
	public Integer addVistorCountByMonth() {
		Date date=new Date();
		int year=date.getYear()>100?2000+date.getYear()%100:date.getYear();
		int month=date.getMonth();
		int day=date.getDate();
		//上一月的开始时间[默认为每月的时间为31天]
		Date startDate=new Date(year,month,1);
		//上一月的结束时间
		Date endDate=new Date(year,month,31);
		//进行查询(仔细思考)
		
		return 100;
	}

	@Transactional(readOnly = false)
	@Override
	public void regUser(String username, String password, String nickname, String email) {
		String uid = IdUtils.uuid();
		ShopWallet wallet = new ShopWallet();
		ShopUser user = new ShopUser();
		wallet.setAccount(IdUtils.uuid());
		wallet.setId(uid);
		wallet.setBalance(new BigDecimal(10000));
		saveWallet(wallet);
		
		user.setId(IdUtils.uuid());
		user.setUserName(username);
		user.setUserEmail(email);
		user.setUserPassword(SecurityUtils.getMD5(password));
		user.setUserImage("/userfiles/user/0000001534320708940.jpg");
		user.setUserNickname(nickname);
		user.setUserScore(0);
		user.setUserStatus("0");
		user.setUserWalletId(uid);
		saveUser(user);
		
	}

}
