package br.order.user.controller.customerOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.pojo.customer.order.CustomerOrderPayInfo;
import br.crm.service.customer.order.CustomerOrderPayInfoService;
import br.crm.service.customer.order.CustomerOrderService;
import br.crm.vo.order.CustomerOrderPayInfoVo;
import br.order.user.vo.empUser.CustomerRegistVo;

@Controller
@RequestMapping("customerOrderPayInfoEnterprise")
public class CustomerOrderPayInfoEnterpriseController {
	@Autowired
	private CustomerOrderPayInfoService customerOrderPayInfoService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private CustomerOrderService customerOrderService; 

	/**
	 * @Title: getCustomerOrderPayInfoByPage
	 * @Description: 查询企业的消费记录
	 * @param page
	 * @param rows
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "查询企业的消费记录", httpMethod = "POST", response = JSONObject.class, notes = "查询企业的消费记录")
	@RequestMapping(value = "/getCustomerOrderPayInfoByPage", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getCustomerOrderPayInfoByPage(@ApiParam(required = true, name = "page", value = "page,当前页") @RequestParam(value = "page", defaultValue = "1", required = true) Integer page, @ApiParam(required = true, name = "rows", value = "rows,每页显示条数") @RequestParam(value = "rows", defaultValue = "10", required = true) Integer rows, @ApiParam(required = true, name = "customerOrderPayInfo", value = "customerOrderPayInfo,订单支付明细对象") CustomerOrderPayInfoVo customerOrderPayInfoVo) {
		JSONObject message = new JSONObject();
		try {
			HttpSession session = request.getSession();
			CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
			String customerCompanyId = customerRegistVo.getCustomerCompanyId();
			customerOrderPayInfoVo.setEnterpriseId(customerCompanyId);
			PageInfo<CustomerOrderPayInfoVo> pageInfo = customerOrderPayInfoService.getCustomerOrderPayInfoByPage(page, rows, customerOrderPayInfoVo);

			message.put("data", pageInfo);
			return InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: getCustomerOrderPayInfo
	 * @Description: 查询企业的消费记录
	 * @param customerOrderPayInfoId
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "查询企业的消费记录", httpMethod = "GET", response = JSONObject.class, notes = "查询企业的消费记录")
	@RequestMapping(value = "/getCustomerOrderPayInfo", method = RequestMethod.GET)
	@ResponseBody
	public JSONObject getCustomerOrderPayInfo(@ApiParam(required = true, name = "customerOrderPayInfoId", value = "customerOrderPayInfoId,订单支付明细id") String customerOrderPayInfoId) {
		JSONObject message = new JSONObject();
		try {
			CustomerOrderPayInfo customerOrderPayInfo = customerOrderPayInfoService.getCustomerOrderPayInfoById(customerOrderPayInfoId);
			message.put("data", customerOrderPayInfo);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

}
