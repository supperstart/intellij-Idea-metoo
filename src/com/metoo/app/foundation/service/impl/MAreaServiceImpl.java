package com.metoo.app.foundation.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.metoo.app.buyer.domain.Result;
import com.metoo.app.foundation.service.MIAreaService;
import com.metoo.core.dao.IGenericDAO;
import com.metoo.foundation.domain.Area;
import com.metoo.foundation.service.ISysConfigService;
import com.metoo.foundation.service.IUserConfigService;
import com.metoo.foundation.service.IUserService;
@Service
@Transactional
public class MAreaServiceImpl implements MIAreaService{
	
	@Resource(name = "AreaMetooDao")
	private IGenericDAO<Area> areaMetooDao;
	@Autowired
	private ISysConfigService configService;
	@Autowired
	private IUserConfigService userConfigService;
	@Autowired
	private IUserService userService;
	
	@Override
	public Area getObjById(Long id) {
		// TODO Auto-generated method stub
		Area area = this.areaMetooDao.get(id);
		if(area != null){
			return area;
		}
		return null;
	}
	
	@Override
	public List<Area> query(String query, Map params, int begin, int max) {
		// TODO Auto-generated method stub
		return this.areaMetooDao.query(query, params, begin, max);
	}
	
	public String areaParent(HttpServletRequest request,
			HttpServletResponse response, String currentPage){
		Result result= null;
			List<Area> areas = this.query(
						"select obj from Area obj where obj.parent.id is null", null,
						-1, -1);
			Map<String, Object> areamap = new HashMap<String, Object>();
			List<Map<String, Object>> areaList = new ArrayList<Map<String, Object>>();
			for(Area area:areas){
				Map<String, Object> areaMap = new HashMap<String, Object>();
				areaMap.put("areaId", area.getId());
				areaMap.put("areaName", area.getAreaName());
				areaList.add(areaMap);
			}
			areamap.put("areaMap", areaList);
			areamap.put("currentPage", currentPage);
			result = new Result(0, "Successfully", areamap);
		return Json.toJson(result, JsonFormat.compact());
	}
}
