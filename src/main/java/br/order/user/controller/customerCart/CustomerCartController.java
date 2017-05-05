package br.order.user.controller.customerCart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.pojo.customer.cart.CustomerCart;
import br.crm.pojo.customer.cart.CustomerCartFeeItem;
import br.crm.pojo.customer.patient.CustomerPatient;
import br.crm.service.customer.cart.CustomerCartFeeItemService;
import br.crm.service.customer.cart.CustomerCartService;
import br.crm.vo.customer.cart.CustomerCartVo;
import br.order.user.vo.empUser.CustomerRegistVo;

/**
 * @ClassName: CustomerCartController
 * @Description: 用户预约平台购物车应用
 * @author server
 * @date 2016年11月3日 上午9:45:55
 */
@RequestMapping("/customerCart")
@Controller
public class CustomerCartController {
	@Autowired
	private CustomerCartService customerCartService;
	@Autowired
	private CustomerCartFeeItemService customerCartFeeItemService;

	@Autowired
	private HttpServletRequest request;

	@ApiOperation(value = "获取购物车列表", httpMethod = "GET", response = JSONObject.class, notes = "获取购物车列表")
	@RequestMapping("/getCustomerCartByePage")
	@ResponseBody
	public JSONObject getCustomerCartByePage() {
		JSONObject message = new JSONObject();
		try {
			HttpSession session = request.getSession();
			CustomerRegistVo customer = (CustomerRegistVo) session.getAttribute("loginUser");
			if (null != customer) {
				List<CustomerCartVo> pageInfo = customerCartService.getCustomerCartByPage(customer.getCustomerInfoId());
				message.put("data", pageInfo);
				return InterfaceResultUtil.getReturnMapSuccess(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: insertCustomerCart
	 * @Description: 添加套餐到购物车
	 * @param customerCart
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "添加购物车信息", httpMethod = "POST", response = JSONObject.class, notes = "添加购物车信息")
	@RequestMapping(value = "/insertToCustomerCart", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject insertToCustomerCart(@RequestBody CustomerCartVo customerCartVo) {
		JSONObject message = new JSONObject();
		try {
			HttpSession session = request.getSession();
			CustomerRegistVo customer = (CustomerRegistVo) session.getAttribute("loginUser");
			if (null != customer) {
				int count = customerCartService.getBranchSuiteCount(customerCartVo.getExamSuiteId(), customerCartVo.getBranchId(), customerCartVo.getExamTimeStr());
				if (customerCartVo.getCustomerList().size() > count) {
					message.put("data", -1);
					InterfaceResultUtil.getReturnMapSuccess(message);
				}
				customerCartVo.setCustomerId(customer.getCustomerInfoId());
				int i = customerCartService.insertCustomerCart(customer, customerCartVo);
				if (i > 0) {
					message.put("data", i);
					return InterfaceResultUtil.getReturnMapSuccess(message);
				}
			}
			message.put("data", 0);
			InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: updateCustomerCart
	 * @Description: 修改购物车（可以晒修改购物车的套餐）xu
	 * 
	 * @param customerCart
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "修改购物车信息", httpMethod = "POST", response = JSONObject.class, notes = "修改购物车信息")
	@RequestMapping("/updateCustomerCart")
	@ResponseBody
	public JSONObject updateCustomerCart(@ApiParam(required = true, name = "CustomerCart", value = "CustomerCart,购物车对象") CustomerCart customerCart, @ApiParam(required = true, name = "customerCartFeeItem", value = "customerCartFeeItem,收费项对象") CustomerCartFeeItem customerCartFeeItem, @ApiParam(required = true, name = "customerPatient", value = "customerPatient,用户对象") CustomerPatient customerPatient) {
		JSONObject message = new JSONObject();
		try {
			customerCart.setCartId(customerCart.getCartId());
			int i = customerCartService.updateCustomerCart(customerCart);
			if (i > 0) {
				// 修改购物车收费项
				List<CustomerCartFeeItem> list = customerCartFeeItemService.getCustomerCartFeeItemBycartId(customerCart.getCartId());
				if (CollectionUtils.isNotEmpty(list)) {
					for (CustomerCartFeeItem CartFeeItem : list) {
						CartFeeItem.setCartId(customerCart.getCartId());
						CartFeeItem.setExamSuiteId(customerCart.getExamSuiteId());
						CartFeeItem.setExamSuiteName(customerCart.getExamSuiteName());
						customerCartFeeItemService.updateCustomerCartFeeItem(CartFeeItem);
						message.put("data", i);
						return InterfaceResultUtil.getReturnMapSuccess(message);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: updateCustomerCart
	 * @Description: 删除购物车，根据用户id删除清空购物车
	 * @param customerCart
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "删除购物车信息", httpMethod = "GET", response = JSONObject.class, notes = "删除购物车信息")
	@RequestMapping("/delectCustomerCart")
	@ResponseBody
	public JSONObject delectCustomerCart(String cartId) {
		JSONObject message = new JSONObject();
		try {
			List<String> id = new ArrayList<String>();
			if (StringUtils.isNotEmpty(cartId)) {
				String[] str = cartId.split(",");
				for (String ids : str) {
					id.add(ids);
				}
			}
			int i = customerCartService.delectCustomerCart(id);
			message.put("data", i);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: getCustomerCart
	 * @Description: 根据用户id获取购物车对象
	 * @param customerId
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "根据购物车id查询信息", httpMethod = "GET", response = JSONObject.class, notes = "根据购物车id查询信息")
	@RequestMapping("/getCustomerCartById")
	@ResponseBody
	public JSONObject getCustomerCartById(String cartId) {
		JSONObject message = new JSONObject();
		try {
			HttpSession session = request.getSession();
			CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
			CustomerCartVo customerCart = customerCartService.getCustomerCartById(customerRegistVo.getCustomerInfoId(), cartId);
			if (null != customerCart) {
				message.put("data", customerCart);
			}
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);

	}

	@ApiOperation(value = "获取门店/套餐剩余份数", httpMethod = "GET", response = JSONObject.class, notes = "获取门店/套餐剩余份数")
	@RequestMapping("/getThreshold")
	@ResponseBody
	public JSONObject getThreshold(String suiteId, String branchId, String date) {
		JSONObject message = new JSONObject();
		try {
			int count = customerCartService.getBranchSuiteCount(suiteId, branchId, date);
			message.put("data", count);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);

	}
}
