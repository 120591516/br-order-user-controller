package br.order.user.controller.highIncidenceDisease;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.pojo.org.DictHighIncidenceDisease;
import br.crm.service.dict.DictHighIncidenceDiseaseService;

@Controller
@RequestMapping("/hid")
public class HighIncidenceDiseaseController {
	
	@Autowired
	private  DictHighIncidenceDiseaseService dictHighIncidenceDiseaseService;
	
	/**
	 * 全部查询高发疾病
	 */
	@ApiOperation(value = "查询高发疾病列表", httpMethod = "GET", response = JSONObject.class, notes = "查询高发疾病列表")
	@RequestMapping("/hid")
	@ResponseBody
	public JSONObject getHighIncidenceDiseases() {
		JSONObject message = new JSONObject();
		try {
			List<DictHighIncidenceDisease> list = dictHighIncidenceDiseaseService.getHighIncidenceDiseases();
			message.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}


}
