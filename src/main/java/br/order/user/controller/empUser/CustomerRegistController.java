package br.order.user.controller.empUser;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
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
import br.crm.common.utils.RedisConstant;
import br.crm.common.utils.StringTransCodeUtil;
import br.order.redis.redis.RedisService;
import br.order.user.pojo.empUser.CustomerInfo;
import br.order.user.pojo.empUser.CustomerRegist;
import br.order.user.service.empUser.CustomerInfoService;
import br.order.user.service.empUser.CustomerRegistService;
import br.order.user.vo.empUser.CustomerInfoVo;
import br.order.user.vo.empUser.CustomerRegistQu;
import br.order.user.vo.empUser.CustomerRegistVo;

/**
 * @ClassName: CustomerRegistController
 * @Description: TODO
 * @author 杨春阳
 * @date 2016年10月17日 上午10:39:12
 */
@Controller
@RequestMapping("/customersRegist")
public class CustomerRegistController {
	/**
	 * {客户信息service}
	 */
	@Autowired
	private CustomerInfoService customerInfoService;

	/**
	 * {客户注册service}
	 */
	@Autowired
	private CustomerRegistService customerRegistService;

	@Autowired
	private RedisService redisService;

	public RedisService getRedisService() {
		return redisService;
	}

	public void setRedisService(RedisService redisService) {
		this.redisService = redisService;
	}

	/**
	 * @Title: getCustomerRegistByPage @Description: TODO(分页注册用户列表) @param page
	 * 页数 @param rows 行数 @param customerRegistQu 条件查询对象 @return
	 * JSONObject @throws
	 */
	@ApiOperation(value = "分页注册用户列表", httpMethod = "GET", response = JSONObject.class, notes = "分页查询用户注册列表")
	@RequestMapping("/getCustomerRegistByPage")
	@ResponseBody
	public JSONObject getCustomerRegistByPage(
			@ApiParam(required = true, name = "page", value = "page,当前页") @RequestParam(value = "page", defaultValue = "1", required = true) Integer page,
			@ApiParam(required = true, name = "rows", value = "rows,每页显示条数") @RequestParam(value = "rows", defaultValue = "10", required = true) Integer rows,
			@ApiParam(required = true, name = "customerRegist", value = "customerRegist,客户注册信息") CustomerRegistQu customerRegistQu) {
		JSONObject message = new JSONObject();
		try {
			customerRegistQu = (CustomerRegistQu) StringTransCodeUtil.transCode(customerRegistQu);
			PageInfo<CustomerInfoVo> customerRegistByPage = customerRegistService.getCustomerRegistByPage(page, rows,
					customerRegistQu);
			message.put("data", customerRegistByPage);
			return InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: getCustomerRegistById
	 * @Description: 根据注册id查詢客戶注册信息
	 * @param customerRegistId
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "根据注册id查詢客戶注册信息", httpMethod = "GET", response = JSONObject.class, notes = "查詢客户注册信息")
	@RequestMapping(value = "/getCustomerRegistById", method = RequestMethod.GET)
	@ResponseBody
	public JSONObject getCustomerRegistById(
			@ApiParam(required = true, name = "customerRegistId", value = "customerRegistId,注册用户Id") String customerRegistId) {
		JSONObject message = new JSONObject();
		try {
			CustomerRegistVo customerRegist = customerRegistService.getCustomerRegist(customerRegistId);
			message.put("data", customerRegist);
			return InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);

	}

	/**
	 * @Title: insertCustomerRegist @Description: TODO(添加客户注册信息) @param
	 * customerRegist 客户注册对象 @param customerInfoPhone 客户信息对象 @return
	 * JSONObject @throws
	 */
	@ApiOperation(value = "添加客户注册信息", httpMethod = "POST", response = JSONObject.class, notes = "客户注册信息")
	@RequestMapping(value = "/insertCustomerRegist", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject insertCustomerRegist(
			@ApiParam(required = true, name = "customerRegist", value = "customerRegist,客户注册对象") CustomerRegist customerRegist,
			@ApiParam(required = true, name = "customerInfo", value = "customerInfo,客户信息对象") CustomerInfo customerInfo,
			@ApiParam(required = true, name = "ind_code", value = "ind_code,验证码") String ind_code) {
		JSONObject message = new JSONObject();
		try {
			String customerInfoId = customerInfoService.insertCustomerInfo(customerInfo);
			if (customerInfoId.length() > 0) {
				customerRegist.setCustomerInfoId(customerInfoId);
				customerRegist.setCustomerPerson(1);
				customerRegist.setCustomerPersonId(customerInfoId);
				customerRegist.setCustomerRegistSms(1);
				String customerRegistId = customerRegistService.insertCustomerRegist(customerRegist);
				return InterfaceResultUtil.getReturnMapSuccess(null);
			}else {
				message.put("message", "注册失败！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			message.put("message", "注册失败！");
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: updateCustomerRegist
	 * @Description: 编辑客户注册信息
	 * @param customerInfo
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "修改客户注册信息", httpMethod = "POST", response = JSONObject.class, notes = "修改客户注册信息")
	@RequestMapping(value = "/updateCustomerRegist", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject updateCustomerRegist(
			@ApiParam(required = true, name = "customerInfo", value = "customerInfo,用户信息") CustomerInfo customerInfo,
			@ApiParam(required = true, name = "customerRegist", value = "customerRegist,注册用户对象") CustomerRegist customerRegist) {
		JSONObject message = new JSONObject();

		try {
			customerInfo.setCustomerInfoId(customerInfo.getCustomerInfoId());
			customerInfo.setCustomerInfoEditTime(new Date());
			int updateCustomerInfo = customerInfoService.updateCustomerInfo(customerInfo);
			if (updateCustomerInfo > 0) {
				CustomerRegist customerRegist1 = new CustomerRegist();
				customerRegist1.setCustomerInfoId(customerInfo.getCustomerInfoId());
				customerRegist1.setCustomerRegistId(customerRegist.getCustomerRegistId());
				customerRegist1.setCustomerRegistEditTime(new Date());
				customerRegistService.updateCustomerRegist(customerRegist1);
			}
			message.put("data", updateCustomerInfo);
			return InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);

	}

	/**
	 * @Title: deleteCustomerRegist
	 * @Description: 根据注册id删除客户信息
	 * @param customerRegistId
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "刪除客户注册信息", httpMethod = "GET", response = JSONObject.class, notes = "刪除客户注册信息")
	@RequestMapping(value = "/deleteCustomerRegist")
	@ResponseBody
	public JSONObject deleteCustomerRegist(
			@ApiParam(required = true, name = "customerRegistId", value = "customerRegistId,客戶註冊信息Id") String customerRegistId) {
		JSONObject message = new JSONObject();
		try {
			CustomerRegist customerRegist = customerRegistService.getCustomerRegistById(customerRegistId);
			customerRegist.setCustomerRegistId(customerRegistId);
			customerRegist.setCustomerRegistEditTime(new Date());
			customerRegist.setCustomerRegistStatus(1);
			int updateCustomerRegist = customerRegistService.updateCustomerRegist(customerRegist);
			message.put("data", updateCustomerRegist);
			return InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);

	}

	/**
	 * @Title: validateCode
	 * @Description: 校验手机验证码
	 * @param customerRegistId
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "校验手机验证码", httpMethod = "GET", response = JSONObject.class, notes = "校验手机验证码")
	@RequestMapping(value = "/validateCode")
	@ResponseBody
	public JSONObject validateCode(
			@ApiParam(required = true, name = "customerInfoPhone", value = "customerInfoPhone,手机号") String customerInfoPhone,
			@ApiParam(required = true, name = "verifyCode", value = "verifyCode,手机验证码") String verifyCode) {
		JSONObject message = new JSONObject();
		try {
			if (StringUtils.isNotEmpty(verifyCode) && StringUtils.isNotEmpty(customerInfoPhone)) {
				String verifyCodeRedis = redisService
						.get(RedisConstant.br_order_dict_SMSRecord_smsPhone.concat(customerInfoPhone));
				if (StringUtils.isNotEmpty(verifyCodeRedis)) {
					if (verifyCode.equals(verifyCodeRedis)) {
						return InterfaceResultUtil.getReturnMapSuccess(null);
					} else {
						message.put("message", "验证码错误！");
						return InterfaceResultUtil.getReturnMapError(message);
					}
				} else {
					message.put("message", "验证码为空或失效！");
					return InterfaceResultUtil.getReturnMapError(message);
				}

			}
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
	/** 
	* @Title: validateEmailCode 
	* @Description: TODO(校验邮箱验证码)
	* @param customerInfoPhone
	* @param verifyCode
	* @return    设定文件 
	* @return JSONObject    返回类型 
	*/
	@ApiOperation(value = "校验邮箱验证码", httpMethod = "GET", response = JSONObject.class, notes = "校验邮箱验证码")
	@RequestMapping(value = "/validateEmailCode")
	@ResponseBody
	public JSONObject validateEmailCode(
			@ApiParam(required = true, name = "customerInfoEmail", value = "customerInfoEmail,邮箱号") String customerInfoEmail,
			@ApiParam(required = true, name = "verifyCode", value = "verifyCode,邮箱验证码") String verifyCode
			){
		JSONObject message = new JSONObject();
		try {
			if (StringUtils.isNotEmpty(verifyCode) && StringUtils.isNotEmpty(customerInfoEmail)) {
				String verifyCodeRedis = redisService
						.get(RedisConstant.br_order_dict_EMAILRecord_dictEmailTo.concat(customerInfoEmail));
				if (StringUtils.isNotEmpty(verifyCodeRedis)) {
					if (verifyCode.equals(verifyCodeRedis)) {
						return InterfaceResultUtil.getReturnMapSuccess(null);
					} else {
						message.put("message", "验证码错误！");
						return InterfaceResultUtil.getReturnMapError(message);
					}
				} else {
					message.put("message", "验证码为空或失效！");
					return InterfaceResultUtil.getReturnMapError(message);
				}

			}
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
}
