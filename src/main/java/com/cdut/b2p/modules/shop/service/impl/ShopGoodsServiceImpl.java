package com.cdut.b2p.modules.shop.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cdut.b2p.modules.shop.mapper.ShopGoodsMapper;
import com.cdut.b2p.modules.shop.po.ShopCollection;
import com.cdut.b2p.modules.shop.po.ShopCollectionExample;
import com.cdut.b2p.modules.shop.po.ShopGoods;
import com.cdut.b2p.modules.shop.po.ShopGoodsExample;
import com.cdut.b2p.modules.shop.po.ShopGoodsInfo;
import com.cdut.b2p.common.po.Page;
import com.cdut.b2p.common.task.ShopGoodsTaskHandler;
import com.cdut.b2p.common.utils.CacheUtils;
import com.cdut.b2p.common.utils.IdUtils;
import com.cdut.b2p.common.utils.StringUtils;
import com.cdut.b2p.modules.shop.mapper.ShopGoodsMapper;
import com.cdut.b2p.modules.shop.mapper.ShopUserMapper;
import com.cdut.b2p.modules.shop.po.ShopGoods;
import com.cdut.b2p.modules.shop.po.ShopUser;
import com.cdut.b2p.modules.shop.service.ShopGoodsService;
import com.cdut.b2p.modules.shop.service.ShopUserService;
import com.cdut.b2p.modules.sys.mapper.SysAreaMapper;
import com.cdut.b2p.modules.sys.mapper.SysDictMapper;
import com.cdut.b2p.modules.sys.po.SysArea;
import com.cdut.b2p.modules.sys.po.SysAreaExample;
import com.cdut.b2p.modules.sys.po.SysDict;
import com.cdut.b2p.modules.sys.po.SysDictExample;
import com.cdut.b2p.modules.sys.po.SysUser;
import com.cdut.b2p.modules.sys.service.SysAreaService;
import com.cdut.b2p.modules.sys.service.SysDictService;

@Service
@Transactional
public class ShopGoodsServiceImpl implements ShopGoodsService {
	@Autowired
	private ShopGoodsMapper shopGoodsMapper;
	@Autowired
	private SysAreaService sysAreaService;
	@Autowired
	private SysDictService sysDictService;
	@Autowired
	private ShopUserService shopUserService;

	/**
	 * @desc 查询上一个月每新增加的商品数量
	 * @author zsb
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@Transactional(readOnly = true)
	@Override
	public Integer addGoodsCountByMonth() {
		Date date = new Date();
		int year = date.getYear();
		int month = date.getMonth();
		int day = date.getDate();
		// 上一月的开始时间[默认为每月的时间为31天]
		Date startDate = new Date(year, month, 1);
		// 上一月的结束时间
		Date endDate = new Date(year, month, 31);

		ShopGoodsExample shopGoodsExample = new ShopGoodsExample();
		shopGoodsExample.or().andCreateDateBetween(startDate, endDate);
		List<ShopGoods> list = shopGoodsMapper.selectByExample(shopGoodsExample);
		return list.size();
	}

	private void preInsert(ShopGoods shopGoods) {
		shopGoods.setUpdateDate(new Date());
		shopGoods.setCreateDate(shopGoods.getUpdateDate());
	}

	@Transactional(readOnly = false)
	@Override
	public void save(ShopGoods shopGoods) {
		preInsert(shopGoods);
		shopGoodsMapper.insertSelective(shopGoods);
		CacheUtils.remove("goodslist");
		CacheUtils.remove("goods_id_" + shopGoods.getId());
		CacheUtils.remove("goods_recommend");
	}
	
	/**
	 * @desc 根据id，更新某一件商品
	 * @param shopGoods
	 * @return boolean
	 */
	@Transactional(readOnly = false)
	@Override
	public boolean updateGoods(ShopGoods shopGoods) {
		int count = shopGoodsMapper.updateByPrimaryKeyWithBLOBs(shopGoods);
		CacheUtils.remove("goodslist");
		CacheUtils.remove("goods_id_" + shopGoods.getId());
		CacheUtils.remove("goods_recommend");
		if (count > 0) {
			return true;
		}
		return false;
	}

	/**
	 * @desc 根据商品id，删除某一件商品
	 * @param id
	 * @return
	 */
	@Transactional(readOnly = false)
	@Override
	public boolean deleteGoods(String id) {
		ShopGoods shopGoods=new ShopGoods();
		shopGoods.setId(id);
		shopGoods.setDelFlag("1");
		int count = shopGoodsMapper.updateByPrimaryKeySelective(shopGoods);
		CacheUtils.remove("goodslist");
		CacheUtils.remove("goods_id_" + id);
		CacheUtils.remove("goods_recommend");

		if(count>0) {
			return true;
		}
		return false;

	}
	
	
	@Transactional(readOnly = true)
	@Override
	public List<ShopGoods> findAllGoods() {
		List<ShopGoods> goodslist = (List<ShopGoods>) CacheUtils.get("goodslist");
		if (goodslist == null) {
			ShopGoodsExample sge = new ShopGoodsExample();
			sge.or().andDelFlagEqualTo("0").andGoodsStatusEqualTo("0");
			goodslist = shopGoodsMapper.selectByExample(sge);
			CacheUtils.put("goodslist", goodslist);
		}
		return goodslist;
	}

	@Transactional(readOnly = true)
	@Override
	public Page<ShopGoodsInfo> findGoodsofOnePage(String type, String brand, Integer min_price, Integer max_price,
			String area, Integer pageNum, Integer pageSize, String keyword) {

		type = sysDictService.findAllDictLabelByDictType(type).get(0).getId();

		if (type == null || StringUtils.isBlank(type)) {
			return null;
		}

		ShopGoodsExample sge = new ShopGoodsExample();
		ShopGoodsExample.Criteria cri = sge.createCriteria();
		sge.setLimit(pageSize);
		sge.setOffset(pageNum.equals(0) ? 0 : (long) ((pageNum - 1) * pageSize));
		cri.andGoodsCategoryIdEqualTo(type);
		cri.andDelFlagEqualTo("0").andGoodsStatusEqualTo("0");
		if (area != null && !StringUtils.isBlank(area)) {
			List<SysArea> arealist = sysAreaService.findAllChildrenByParentId(area);
			List<String> areaIdList = new ArrayList<String>();
			for (SysArea sa : arealist) {
				areaIdList.add(sa.getId());
			}
			if (areaIdList != null && !areaIdList.isEmpty()) {
				cri.andGoodsAreaIdIn(areaIdList);
			} else {
				cri.andGoodsAreaIdEqualTo(area);
			}
		}
		if (brand != null && !StringUtils.isBlank(brand)) {
			cri.andGoodsBrandIdEqualTo(brand);
		}
		if (min_price != null && min_price > 0) {
			cri.andGoodsPresentPriceGreaterThanOrEqualTo(new BigDecimal(min_price));
		}
		if (max_price != null && max_price > 0) {
			cri.andGoodsPresentPriceLessThanOrEqualTo(new BigDecimal(max_price));
		}
		if (keyword != null && !StringUtils.isBlank(keyword)) {
			cri.andGoodsTitleLike("%" + keyword + "%");
		}

		List<ShopGoods> goodslist = shopGoodsMapper.selectByExampleWithBLOBs(sge);
		Long size_long = shopGoodsMapper.countByExample(sge);
		Integer size = size_long.intValue();
		Integer now_size = goodslist.size();

		List<ShopGoodsInfo> list1 = new ArrayList<ShopGoodsInfo>();

		for (ShopGoods goods : goodslist) {
			ShopGoodsInfo info = new ShopGoodsInfo();
			info.setId(goods.getId());
			info.setGoodsBrandModel(goods.getGoodsBrandModel());
			info.setGoodsClickTimes(goods.getGoodsClickTimes());
			info.setGoodsOldLevel(goods.getGoodsOldLevel());
			info.setGoodsOriginalPrice(goods.getGoodsOriginalPrice());
			info.setGoodsPics(goods.getGoodsPics());
			info.setGoodsPresentPrice(goods.getGoodsPresentPrice());
			info.setGoodsTitle(goods.getGoodsTitle());
			info.setGoodsNums(size);
			info.setGoodsStatus(goods.getGoodsStatus());
			//info.setRemarks(goods.getRemarks());
			if (goods.getGoodsDiscrible().length() > 120) {
				info.setGoodsDesc(goods.getGoodsDiscrible().substring(0, 120) + "...");
			} else {
				info.setGoodsDesc(goods.getGoodsDiscrible());
			}
			
			ShopUser seller = shopUserService.findUserById(goods.getGoodsSellerId());
			info.setGoodsSellerNickname(seller.getUserNickname());
			info.setGoodsSellerImg(seller.getUserImage());

			SysDict dict = sysDictService.findDictById(goods.getGoodsBrandId());
			info.setGoodsBrand(dict.getDictLabel());

			SysArea sysarea = sysAreaService.findAreaById(goods.getGoodsAreaId());
			info.setGoodsArea(sysarea.getAreaName());

			SysArea sysarea1 = sysAreaService.findAreaById(sysarea.getAreaParentId());
			info.setGoodsCity(sysarea1.getAreaName());

			SysArea sysarea2 = sysAreaService.findAreaById(sysarea1.getAreaParentId());
			info.setGoodsProvince(sysarea2.getAreaName());

			list1.add(info);
		}

		Page<ShopGoodsInfo> pages = new Page<ShopGoodsInfo>();
		pages.setList(list1);

		pages.setPageNum(pageNum);

		if ((size % pageSize) == 0) {
			pages.setPages(size / pageSize);

		} else {
			pages.setPages((size / pageSize) + 1);
		}

		pages.setPageSize(pageSize);
		pages.setTotal(size);
		pages.setSize(now_size);

		return pages;
	}

	/**
	 * @desc 查询某一时间段内，上架的商品
	 * @author zsb
	 * @param startDate
	 * @param endDate
	 * @return List<ShopGoods>
	 */
	@Transactional(readOnly = true)
	@Override
	public List<ShopGoods> findGoodsByDate(Date startDate, Date endDate) {
		ShopGoodsExample example = new ShopGoodsExample();
		example.or().andCreateDateBetween(startDate, endDate).andDelFlagEqualTo("0").andGoodsStatusEqualTo("0");
		List<ShopGoods> list = shopGoodsMapper.selectByExample(example);
		return list;
	}

	@Transactional(readOnly = true)
	@Override
	public ShopGoodsInfo findGoodsByGoodsId(String goods_id, boolean is_add) {
		
		ShopGoodsInfo info = (ShopGoodsInfo) CacheUtils.get("goods_id_" + goods_id);
		if(info == null) {
			ShopGoods goods = shopGoodsMapper.selectByPrimaryKey(goods_id);
			
			if(goods.getDelFlag().equals("1") || goods.getGoodsStatus().equals("1")) {
				return null;
			}
			
			info = new ShopGoodsInfo();
			info.setId(goods.getId());
			info.setGoodsBrandModel(goods.getGoodsBrandModel());
			info.setGoodsClickTimes(goods.getGoodsClickTimes());
			info.setGoodsOldLevel(goods.getGoodsOldLevel());
			info.setGoodsOriginalPrice(goods.getGoodsOriginalPrice());
			info.setGoodsPics(goods.getGoodsPics());
			info.setGoodsPresentPrice(goods.getGoodsPresentPrice());
			info.setGoodsTitle(goods.getGoodsTitle());
			info.setGoodsStatus(goods.getGoodsStatus());
			info.setGoodsDesc(goods.getGoodsDiscrible());
			info.setRemarks(goods.getRemarks());
			info.setUptime(goods.getGoodsUpTime());
			ShopUser seller = shopUserService.findUserById(goods.getGoodsSellerId());
			info.setGoodsSellerNickname(seller.getUserNickname());
			info.setGoodsSellerImg(seller.getUserImage());
			info.setGoodsSellerAutograph(seller.getUserAutograph());
			info.setGoodsSellerId(seller.getId());

			SysDict dict = sysDictService.findDictById(goods.getGoodsBrandId());
			info.setGoodsBrand(dict.getDictLabel());

			SysArea sysarea = sysAreaService.findAreaById(goods.getGoodsAreaId());
			info.setGoodsArea(sysarea.getAreaName());

			SysArea sysarea1 =sysAreaService.findAreaById(sysarea.getAreaParentId());
			info.setGoodsCity(sysarea1.getAreaName());

			SysArea sysarea2 = sysAreaService.findAreaById(sysarea1.getAreaParentId());
			info.setGoodsProvince(sysarea2.getAreaName());
			
			CacheUtils.put("goods_id_" + goods_id, info);
		}
	
		if(is_add) {
			info.setGoodsClickTimes(info.getGoodsClickTimes() + 1);
			ShopGoodsTaskHandler.andUpdate(info.getId());
		}
		
		//shopGoodsMapper.updateByPrimaryKeyWithBLOBs(info);
		return info;
	}

	@Transactional(readOnly = true)
	@Override
	public List<ShopGoodsInfo> findRecommendGoods() {
		
		List<ShopGoodsInfo> list = (List<ShopGoodsInfo>) CacheUtils.get("goods_recommend");
		
		if(list == null) {
			ShopGoodsExample sge = new ShopGoodsExample();
			ShopGoodsExample.Criteria cri = sge.createCriteria();
			cri.andGoodsClickTimesGreaterThanOrEqualTo(300).andDelFlagEqualTo("0").andGoodsStatusEqualTo("0");
			sge.setLimit(30);
			List<ShopGoods> goodslist = shopGoodsMapper.selectByExampleWithBLOBs(sge);
			list = new ArrayList<ShopGoodsInfo>();

			for (ShopGoods goods : goodslist) {
				
				ShopGoodsInfo info = new ShopGoodsInfo();
				info.setId(goods.getId());
				info.setGoodsBrandModel(goods.getGoodsBrandModel());
				info.setGoodsClickTimes(goods.getGoodsClickTimes());
				info.setGoodsOldLevel(goods.getGoodsOldLevel());
				info.setGoodsOriginalPrice(goods.getGoodsOriginalPrice());
				info.setGoodsPics(goods.getGoodsPics());
				info.setGoodsPresentPrice(goods.getGoodsPresentPrice());
				info.setGoodsTitle(goods.getGoodsTitle());
				info.setGoodsStatus(goods.getGoodsStatus());
				if (goods.getGoodsDiscrible().length() > 120) {
					info.setGoodsDesc(goods.getGoodsDiscrible().substring(0, 120) + "...");
				} else {
					info.setGoodsDesc(goods.getGoodsDiscrible());
				}
				ShopUser seller = shopUserService.findUserById(goods.getGoodsSellerId());
				info.setGoodsSellerNickname(seller.getUserNickname());
				info.setGoodsSellerImg(seller.getUserImage());

				SysDict dict = sysDictService.findDictById(goods.getGoodsBrandId());
				info.setGoodsBrand(dict.getDictLabel());

				SysArea sysarea =sysAreaService.findAreaById(goods.getGoodsAreaId());
				info.setGoodsArea(sysarea.getAreaName());

				SysArea sysarea1 = sysAreaService.findAreaById(sysarea.getAreaParentId());
				info.setGoodsCity(sysarea1.getAreaName());

				SysArea sysarea2 = sysAreaService.findAreaById(sysarea1.getAreaParentId());
				info.setGoodsProvince(sysarea2.getAreaName());

				list.add(info);
			}
			CacheUtils.put("goods_recommend", list);
		}
		return list;
	}
	/**
	 * @desc 根据商品id,查询卖家id
	 */
	@Transactional(readOnly = false)
	@Override
	public String findSellerId(String id) {
		ShopGoods shopGoods=shopGoodsMapper.selectByPrimaryKey(id);
		if(shopGoods!=null && shopGoods.getDelFlag().equals("0")) {
			return shopGoods.getGoodsSellerId();
		}
		return null;
	}

	@Transactional(readOnly = false)
	@Override
	public boolean updateGoodsClickTimes(String id, int times) {
		ShopGoods shopGoods = shopGoodsMapper.selectByPrimaryKey(id);
		shopGoods.setGoodsClickTimes(shopGoods.getGoodsClickTimes() + times);
		boolean rs = updateGoods(shopGoods);
		
		return rs;
	}
	/**
	 * @desc 对比商品信息
	 * @param shopGoodsInfo
	 * @return
	 */
	@Override
	public List<ShopGoods> compareGoods(ShopGoodsInfo shopGoodsInfo) {
		List<ShopGoods>list=shopGoodsMapper.compareGoods(shopGoodsInfo);
		return list;
	}

	@Transactional(readOnly = false)
	@Override
	public List<ShopGoodsInfo> findGoodsBySellerId(String uid) {
		ShopGoodsExample example = new ShopGoodsExample();
		example.or().andGoodsSellerIdEqualTo(uid).andDelFlagEqualTo("0").andGoodsStatusEqualTo("0");
		List<ShopGoods> list = shopGoodsMapper.selectByExample(example);
		List<ShopGoodsInfo> list1 = new ArrayList<ShopGoodsInfo>();
		for(ShopGoods goods : list) {
			list1.add(findGoodsByGoodsId(goods.getId(), false));
		}
		return list1;
	}
	
	
	@Transactional(readOnly = false)
	@Override
	public ShopGoods findGoodsById(String goods_id) {
		ShopGoods goods = shopGoodsMapper.selectByPrimaryKey(goods_id);
		if(goods.getDelFlag().equals("0")) {
			return goods;
		}
		return null;
	}



}
