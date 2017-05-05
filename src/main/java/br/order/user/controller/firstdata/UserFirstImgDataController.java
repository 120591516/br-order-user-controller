package br.order.user.controller.firstdata;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.service.firstdata.FirstImgDataService;
import br.order.common.utils.InterfaceResultUtil;

@Controller
@RequestMapping("/userFirstImgData")
public class UserFirstImgDataController {

	@Autowired
	private FirstImgDataService firstImgDataService;

	@ApiOperation(value = "首页轮播图展示", httpMethod = "GET", response = JSONObject.class, notes = "首页轮播图展示")
	@RequestMapping("/showUserFirstImgData")
	@ResponseBody
	public JSONObject showUserFirstImgData() {
		JSONObject message = new JSONObject();
		try {
			List<Map<String, String>> list = firstImgDataService.getFirstImgs();
			message.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

}
