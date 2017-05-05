package br.order.user.controller.suitetype;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.pojo.org.DictExamSuiteType;
import br.crm.service.dict.DictExamSuiteTypeService;
import br.crm.service.suite.OrgExamSuiteService;
import br.order.common.utils.InterfaceResultUtil;

@Controller
@RequestMapping("/suitetype")
public class SuiteTypeController {
	
	@Autowired
	private DictExamSuiteTypeService dictExamSuiteTypeService;
	
	@ApiOperation(value="获取套餐类型名称",httpMethod="GET",notes="获取套餐类型")
	@RequestMapping("/suitetype")
	@ResponseBody
	public JSONObject getExamSuiteTypes() {
		JSONObject message = new JSONObject();
		try {
			List<DictExamSuiteType> list= dictExamSuiteTypeService.getExamSuiteTypes();
			message.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

}
