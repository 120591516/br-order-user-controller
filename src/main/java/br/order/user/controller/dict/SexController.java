package br.order.user.controller.dict;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.pojo.dict.Dictsex;
import br.crm.service.dict.DictsexService;

@Controller
@RequestMapping("/sex")
public class SexController {
	
	@Autowired
	private DictsexService dictsexService;

	
	
	@ApiOperation(value = "可用性别列表", httpMethod = "GET", response = JSONObject.class, notes = "可用性别列表")
	@RequestMapping("/list")
	@ResponseBody
	public JSONObject dictSexListByStatus() {
		JSONObject jsonObject = new JSONObject();
		try {
			List<Dictsex> list = dictsexService.dictSexListByStatus();
			jsonObject.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return InterfaceResultUtil.getReturnMapError(jsonObject);
	}

}
