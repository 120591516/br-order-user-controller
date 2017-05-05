package br.order.user.controller.dict;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.common.utils.AddressUtils;
import br.crm.pojo.dict.DictArea;
import br.crm.service.dict.DictAreaService;
import br.order.common.utils.InterfaceResultUtil;

@Controller
@RequestMapping("/area")
public class AreaController {
	
	@Autowired
	private DictAreaService dictAreaService;

	@ApiOperation(value = "获取所有省份列表", httpMethod = "GET", response = JSONObject.class, notes = "获取所有省份列表")
	@RequestMapping("/province")
	@ResponseBody
	public JSONObject getAllProvince() {
		JSONObject message = new JSONObject();
		try {
			List<DictArea> list = dictAreaService.getAllProvince();
			message.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
	
	@ApiOperation(value = "根据省id获取所有市列表", httpMethod = "GET", response = JSONObject.class, notes = "根据省id获取所有市列表")
	@RequestMapping("/city")
	@ResponseBody
	public JSONObject getCityByProvinceId(
			@ApiParam(required = true, name = "provinceId", value = "provinceId,省id") Integer provinceId) {
		JSONObject message = new JSONObject();
		try {
			List<DictArea> list = dictAreaService.getCityByProvinceId(provinceId);
			message.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
	@ApiOperation(value = "根据市id获取所有区县列表", httpMethod = "GET", response = JSONObject.class, notes = "根据市id获取所有区县列表")
	@RequestMapping("/district")
	@ResponseBody
	public JSONObject getDistrictByCityId(
			@ApiParam(required = true, name = "cityId", value = "cityId,市id") Integer cityId) {
		JSONObject message = new JSONObject();
		try {
			List<DictArea> list = dictAreaService.getDistrictByCityId(cityId);
			message.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
	
	

	@ApiOperation(value = "定位城市", httpMethod = "GET", response = JSONObject.class, notes = "定位城市")
	@RequestMapping("/positioncity")
	@ResponseBody
	public JSONObject getCity(HttpServletRequest request) {
		JSONObject message = new JSONObject();
		try {
			AddressUtils addressUtils = new AddressUtils();
			String ip = AddressUtils.getIpAddr(request);//114.243.46.22
			System.out.println("ip:"+ip);
			if (StringUtils.isNotEmpty(ip)) {
				String city = addressUtils.getAddresses("ip="+ip);
				System.out.println("city:"+city);
				if (StringUtils.isNotEmpty(city)) {// 北京市
					
					DictArea dictArea = dictAreaService.getPositionCity(city);
			
					message.put("city", dictArea);//返回定位的城市
					
					LinkedList<String> list=dictAreaService.getPositionCity();
					
					List<DictArea> areaList=dictAreaService.getCitys(list);
					message.put("cityList", areaList);
					return message;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}
}
