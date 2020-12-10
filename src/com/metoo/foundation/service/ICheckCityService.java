package com.metoo.foundation.service;

import java.util.List;
import java.util.Map;

import com.metoo.foundation.domain.CheckCity;

public interface ICheckCityService {
	List<CheckCity> query(String query, Map params, int begin, int max);
}
