package br.order.user.controller.organizationLevel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.pojo.org.OrganizationLevel;
import br.crm.service.org.OrgLevelService;

@Controller
@RequestMapping("/orglevel")
public class OrganizationLevelController {

	@Autowired
	private OrgLevelService orgLevelService;
	
	@ApiOperation(value = "获取等级", httpMethod = "GET", response = JSONObject.class, notes = "获取等级")
	@RequestMapping("/orglevel")
	@ResponseBody
	public JSONObject getOrganizationLevelAll() {

		JSONObject jsonObject = new JSONObject();
		try {
			List<OrganizationLevel> list = orgLevelService.getAllOrgLevel();
			jsonObject.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(jsonObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(jsonObject);
	}
}
