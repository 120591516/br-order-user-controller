package br.order.user.controller.dict;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.common.utils.InterfaceResultUtil;
import br.order.user.pojo.dict.DictRelationship;
import br.order.user.service.dict.DictRelationshipService;

@Controller
@RequestMapping("/rs")
public class RelationShipController {
	
	@Autowired
	private DictRelationshipService dictRelationshipService;

	@ApiOperation(value = "查询可用人际关系字典列表", httpMethod = "GET", response = JSONObject.class, notes = "查询可用人际关系字典列表")
	@RequestMapping("/list")
	@ResponseBody
	public JSONObject getValidRelationship() {
		JSONObject jsonObject = new JSONObject();
		try {
			List<DictRelationship> cusInfoListByStatus = dictRelationshipService.cusInfoListByStatus();
			jsonObject.put("data", cusInfoListByStatus);
			return InterfaceResultUtil.getReturnMapSuccess(jsonObject);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(jsonObject);

	}
}
