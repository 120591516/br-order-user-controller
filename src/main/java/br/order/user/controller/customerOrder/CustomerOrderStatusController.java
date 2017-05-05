package br.order.user.controller.customerOrder;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.service.customer.order.CustomerOrderStatusService;

@Controller
@RequestMapping("/customerOrderStatus")
public class CustomerOrderStatusController {
	
	
	@Autowired
	private CustomerOrderStatusService customerOrderStatusService;
	
	
	
	@ApiOperation(value = "查询所有状态", httpMethod = "GET", response = JSONObject.class, notes = "查询所有状态")
	@RequestMapping(value = "/getCustomerOrderStatusList")
	@ResponseBody
	public JSONObject getCustomerOrderStatusList(){
		JSONObject message = new JSONObject();
		List<Map<String,String>> map =customerOrderStatusService.getCustomerOrderStatusList();
		message.put("data", map);
		return InterfaceResultUtil.getReturnMapSuccess(message);
	}

}
