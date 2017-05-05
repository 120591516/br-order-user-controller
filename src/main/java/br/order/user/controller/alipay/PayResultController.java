package br.order.user.controller.alipay;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.pojo.customer.order.CustomerOrder;
import br.crm.pojo.customer.order.CustomerOrderPayInfo;
import br.crm.service.customer.order.CustomerOrderPayInfoService;
import br.crm.service.customer.order.CustomerOrderService;

@Controller
@RequestMapping("/payResult")
public class PayResultController {
	@Autowired
	private CustomerOrderService customerOrderService;
	@Autowired
	private CustomerOrderPayInfoService customerOrderPayInfoService;
	/** 
	* @Title: getPayResult 
	* @Description: 支付宝接口通知
	* @param orderNo
	* @param orderPayCallback
	* @return    设定文件 
	* @return JSONObject    返回类型 
	*/
	@ApiOperation(value = "接口通知", httpMethod = "GET", response = JSONObject.class, notes = "接口通知")
	@RequestMapping(value = "/getPayResult")
	@ResponseBody
	public JSONObject getPayResult(String orderNo,String orderPayCallback){
		JSONObject message = new JSONObject();
		try {
			CustomerOrder customerOrder = customerOrderService.getCustomerOrder(orderNo);
			if(customerOrder != null){
				Integer payWayId = customerOrder.getPayWayId();
				Integer orderStatus = customerOrder.getOrderStatus();
				if(payWayId == 2 && orderStatus == 2){
					CustomerOrderPayInfo customerOrderPayInfo = customerOrderPayInfoService.getCustomerOrderPayInfo(orderNo);
					customerOrderPayInfo.setOrderPayCallback(orderPayCallback);
					int i = customerOrderPayInfoService.updateCustomerOrderPayInfo(customerOrderPayInfo);
					if(i>0){
						message.put("data", 0);
						return InterfaceResultUtil.getReturnMapSuccess(message);
					}					
				}
				message.put("data", 1);
				return InterfaceResultUtil.getReturnMapSuccess(message);
			}		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
		
	}

}
