package br.order.user.controller.customerOrder;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.pojo.customer.cart.CustomerCart;
import br.crm.pojo.customer.order.CustomerOrder;
import br.crm.service.customer.cart.CustomerCartService;
import br.crm.service.customer.order.CustomerOrderService;
import br.crm.vo.customer.order.CustomerOrderVo;
import br.crm.vo.order.CustomerOrderPayInfoVo;
import br.order.user.vo.empUser.CustomerRegistVo;
/**
 * @ClassName: CustomerOrderEnterpriseController
 * @Description: TODO(企业中心订单)
 * @author server
 * @date 2016年11月29日 下午1:50:27
 */
@Controller
@RequestMapping("/customerOrderEnterprise")
public class CustomerOrderEnterpriseController {
	@Autowired
	private CustomerOrderService customerOrderService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private CustomerCartService customerCartService;
	
	  /** 
	* @Title: getCustomerOrderByPage 
	* @Description: TODO(企业中心订单列表分页)
	* @param page
	* @param rows
	* @param customerOrderId
	* @return    设定文件 
	* @return JSONObject    返回类型 
	*/
	@ApiOperation(value = "分页获取订单信息", httpMethod = "POST", response = JSONObject.class, notes = "分页获取订单信息")
	  @RequestMapping(value = "/getCustomerOrderByPage")
	  @ResponseBody
	public JSONObject getCustomerOrderByPage(
			 @ApiParam(required = true, name = "page", value = "page,当前页") @RequestParam(value = "page", defaultValue = "1", required = true) Integer page,
	         @ApiParam(required = true, name = "rows", value = "rows,每页显示条数") @RequestParam(value = "rows", defaultValue = "10", required = true) Integer rows,
	         @ApiParam(required=true ,name="customerOrderVo",value="customerOrderVo,订单对象")CustomerOrderVo customerOrderVo){
			JSONObject message = new JSONObject();		
			try {
				HttpSession session = request.getSession();
				CustomerRegistVo  customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
				String customerCompanyId = customerRegistVo.getCustomerCompanyId();
				customerOrderVo.setEnterpriseId(customerCompanyId);
				PageInfo<CustomerOrderVo> pageInfo = customerOrderService.getCustomerOrderByPage(page, rows, customerOrderVo);
				message.put("data", pageInfo);
				return InterfaceResultUtil.getReturnMapSuccess(message);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return InterfaceResultUtil.getReturnMapError(message);
			
 }
	/** 
	* @Title: getCustomerOrder 
	* @Description: TODO(根据登录用户id查询企业订单详情)
	* @return    设定文件 
	* @return JSONObject    返回类型 
	*/
	  @ApiOperation(value = "获取订单信息详情", httpMethod = "post", response = JSONObject.class, notes = "获取订单信息详情")
	  @RequestMapping(value = "/getCustomerOrder")
	  @ResponseBody
	public JSONObject getCustomerOrder(
			@ApiParam(required = true, name = "examSuiteId", value = "examSuiteId,套餐id")String examSuiteId,
			@ApiParam(required = true, name = "customerPatientName", value = "customerPatientName,购买人姓名")String customerPatientName,
			@ApiParam(required = true, name = "examTime", value = "examTime,体检时间")String examTime){
		JSONObject message = new JSONObject();
		try {
			SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(new Date(Long.valueOf(examTime)));		
			CustomerOrderVo customerOrderVo = customerOrderService.getCustomerOrderById(customerPatientName,examSuiteId,time);
			message.put("data", customerOrderVo);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
}