package br.order.user.controller.organization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.service.org.OrgLevelService;

@Controller
@RequestMapping("/org")
public class OrganizationController {
	
	@Autowired
	private OrgLevelService orgLevelService;
	
	
	@ApiOperation(value = "获取机构名称", httpMethod = "GET", response = JSONObject.class, notes = "获取机构名称")
	@RequestMapping("/list")
	@ResponseBody
	public JSONObject getOrgAll() {
		JSONObject jsonObject = new JSONObject();
		try {
			List<Map<String,String>> list = orgLevelService.getOrgAll();
			jsonObject.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(jsonObject);
	}
	@ApiOperation(value = "机构类型", httpMethod = "GET", response = JSONObject.class, notes = "机构类型")
	@RequestMapping("type")
	@ResponseBody
	public JSONObject getType() {
		JSONObject jsonObject = new JSONObject();
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("1", "体检机构");
			map.put("2", "医院");
			jsonObject.put("data", map);
			return InterfaceResultUtil.getReturnMapSuccess(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(jsonObject);
	}

}
