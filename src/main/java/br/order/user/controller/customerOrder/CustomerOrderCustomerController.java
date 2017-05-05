package br.order.user.controller.customerOrder;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.common.utils.InterfaceResultUtil;
import br.crm.common.utils.UUIDUtils;
import br.crm.common.wxpay.utils.MatrixToImageWriter;
import br.crm.common.wxpay.utils.PayCommonUtil;
import br.crm.common.wxpay.utils.PayConfigUtil;
import br.crm.common.wxpay.utils.XMLUtil;
import br.crm.pojo.customer.cart.CustomerCart;
import br.crm.pojo.customer.order.CustomerOrder;
import br.crm.pojo.customer.order.CustomerOrderPayInfo;
import br.crm.pojo.dict.Dictreceipttype;
import br.crm.service.customer.cart.CustomerCartService;
import br.crm.service.customer.order.CustomerOrderCartService;
import br.crm.service.customer.order.CustomerOrderPayInfoService;
import br.crm.service.customer.order.CustomerOrderPayNoticeService;
import br.crm.service.customer.order.CustomerOrderService;
import br.crm.service.customer.order.wxpay.WxpayService;
import br.crm.service.dict.DictreceipttypeService;
import br.crm.vo.customer.order.CustomerOrderVo;
import br.order.user.vo.empUser.CustomerRegistVo;

/**
 * @ClassName: CustomerOrderCustomerController
 * @Description: TODO(订单表)
 * @author server
 * @date 2016年11月28日 下午4:53:54
 */
@Controller
@RequestMapping("/customerOrderCustomer")
public class CustomerOrderCustomerController {

	@Autowired
	private CustomerOrderService customerOrderService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private DictreceipttypeService dictreceipttypeService;

	@Autowired
	private CustomerCartService customerCartService;

	@Autowired
	private CustomerOrderPayInfoService customerOrderPayInfoService;

	@Autowired
	private CustomerOrderPayNoticeService customerOrderPayNoticeService;
	@Autowired
	private CustomerOrderCartService customerOrderCartService;

	@Autowired
	private WxpayService wxpayService;

	@ApiOperation(value = "提交订单", httpMethod = "POST", response = JSONObject.class, notes = "提交订单")
	@RequestMapping(value = "/addCustomerOrder")
	@ResponseBody
	public JSONObject addCustomerOrder(@RequestBody CustomerOrderVo customerOrderVo) {
		JSONObject message = new JSONObject();
		HttpSession session = request.getSession();
		CustomerRegistVo customer = (CustomerRegistVo) session.getAttribute("loginUser");
		if (StringUtils.isEmpty(customerOrderVo.getCartIds())) {
			return InterfaceResultUtil.getReturnMapValidValue(message);
		}
		try {
			List<Map<String, String>> list = customerOrderService.addCustomerOrder(customerOrderVo, customer);
			message.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: getCustomerOrderByPage
	 * @Description: TODO(个人中心订单分页)
	 * @param page
	 * @param rows
	 * @param enterpriseDeptId
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "分页获取订单信息", httpMethod = "POST", response = JSONObject.class, notes = "分页获取订单信息")
	@RequestMapping(value = "/getCustomerOrderByPage")
	@ResponseBody
	public JSONObject getCustomerOrderByPage(@ApiParam(required = true, name = "page", value = "page,当前页") @RequestParam(value = "page", defaultValue = "1", required = true) Integer page, @ApiParam(required = true, name = "rows", value = "rows,每页显示条数") @RequestParam(value = "rows", defaultValue = "10", required = true) Integer rows, @ApiParam(required = true, name = "customerOrderVo", value = "customerOrderVo,订单对象") CustomerOrderVo customerOrderVo) {
		JSONObject message = new JSONObject();
		try {
			HttpSession session = request.getSession();
			CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
			String customerInfoId = customerRegistVo.getCustomerInfoId();
			customerOrderVo.setCustomerId(customerInfoId);
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
	 * @Description: TODO(根据登录用户id查询用户订单详情)
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "获取订单信息详情", httpMethod = "post", response = JSONObject.class, notes = "获取订单信息详情")
	@RequestMapping(value = "/getCustomerOrder")
	@ResponseBody
	public JSONObject getCustomerOrder(@ApiParam(required = true, name = "examSuiteId", value = "examSuiteId,套餐id") String examSuiteId, @ApiParam(required = true, name = "customerPatientName", value = "customerPatientName,购买人姓名") String customerPatientName, @ApiParam(required = true, name = "examTime", value = "examTime,体检时间") String examTime) {
		JSONObject message = new JSONObject();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = format.format(new Date(Long.valueOf(examTime)));
			CustomerOrderVo customerOrderVo = customerOrderService.getCustomerOrderById(customerPatientName, examSuiteId, time);
			message.put("data", customerOrderVo);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: getDictreceipttypeList @Description: TODO(查询发票类型列表) @return
	 *         JSONObject @throws
	 */

	@ApiOperation(value = "查询发票类型列表", httpMethod = "GET", response = JSONObject.class, notes = "查询发票类型列表")
	@RequestMapping("/getDictreceipttypeList")
	@ResponseBody
	public JSONObject getDictreceipttypeList() {
		JSONObject jsonObject = new JSONObject();
		try {
			List<Dictreceipttype> list = dictreceipttypeService.getDictreceipttypeList();
			jsonObject.put("data", list);
			return InterfaceResultUtil.getReturnMapSuccess(jsonObject);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return InterfaceResultUtil.getReturnMapError(jsonObject);
	}

	/**
	 * @Title: getParametersBycustomerId
	 * @Description: 根据用户id获取体检机构、门店、套餐
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "根据用户id获取体检机构、门店、套餐", httpMethod = "GET", response = JSONObject.class, notes = "根据用户id获取体检机构、门店、套餐")
	@RequestMapping("/getParametersBycustomerId")
	@ResponseBody
	public JSONObject getParametersBycustomerId() {
		JSONObject message = new JSONObject();
		try {
			HttpSession session = request.getSession();
			CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
			String customerInfoId = customerRegistVo.getCustomerInfoId();
			List<CustomerCart> list = customerCartService.getCustomerCart(customerInfoId);
			Map<String, Object> orgMap = null;
			Map<String, Object> branchMap = null;
			Map<String, Object> suitMap = null;
			List<Map<String, Object>> orgList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> branchList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> suitList = new ArrayList<Map<String, Object>>();
			if (CollectionUtils.isNotEmpty(list)) {
				for (CustomerCart customerCart : list) {
					orgMap = new HashMap<String, Object>();
					orgMap.put("orgId", customerCart.getOrgId());
					orgMap.put("orgName", customerCart.getOrgName());
					branchMap = new HashMap<String, Object>();
					branchMap.put("branchId", customerCart.getBranchId());
					branchMap.put("branchName", customerCart.getBranchName());
					suitMap = new HashMap<String, Object>();
					suitMap.put("suiteId", customerCart.getExamSuiteId());
					suitMap.put("suiteName", customerCart.getExamSuiteName());
					orgList.add(orgMap);
					branchList.add(branchMap);
					suitList.add(suitMap);
				}
				message.put("orgData", orgList);
				message.put("branchData", branchList);
				message.put("suitData", suitList);
				return InterfaceResultUtil.getReturnMapSuccess(message);
			} else {
				message.put("data", 0);
				return InterfaceResultUtil.getReturnMapSuccess(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	@ApiOperation(value = "定时器获取订单号的状态", httpMethod = "GET", response = JSONObject.class, notes = "定时器获取订单号的状态")
	@RequestMapping(value = "/getOrderState")
	@ResponseBody
	public JSONObject getOrderState(String orderNo) {
		int flag = 0;
		HttpSession session = request.getSession();
		CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
		JSONObject message = new JSONObject();
		CustomerOrder customerOrder = customerOrderService.getCustomerOrder(orderNo);
		if (null != customerOrder) {
			if (customerRegistVo.getCustomerInfoId().equals(customerOrder.getCustomerId())) {
				flag = customerCartService.getOrderState(orderNo);
				message.put("state", flag);
			}
		}
		message.put("state", flag);
		return message;
	}

	@ApiOperation(value = "生成付款二维码图片", httpMethod = "GET", response = JSONObject.class, notes = "生成付款二维码图片")
	@RequestMapping(value = "/createQRCode", method = RequestMethod.GET)
	@ResponseBody
	public synchronized void createOrderInfo(String orderNo, HttpServletResponse response) {
		try {
			CustomerOrder customerOrder = customerOrderService.getCustomerOrder(orderNo);
			if (null != customerOrder && customerOrder.getOrderStatus() == 1) {
				String urlCode = wxpayService.createOrderInfo(customerOrder);
				if (StringUtils.isNotEmpty(urlCode)) {
					encodeQrcode(urlCode, response);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void encodeQrcode(String content, HttpServletResponse response) {
		if (content == null || "".equals(content))
			return;
		MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		Map hints = new HashMap();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 设置字符集编码类型
		BitMatrix bitMatrix = null;

		try {
			bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);
			BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
			// 输出二维码图片流
			ImageIO.write(image, "png", response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@ApiOperation(value = "微信回调方法", httpMethod = "GET", response = JSONObject.class, notes = "微信回调方法")
	@RequestMapping("/notifyURL")
	@ResponseBody
	public String weixin_notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		SortedMap<Object, Object> returnParams = new TreeMap<Object, Object>();
		try {
			// 读取参数
			InputStream inputStream;
			StringBuffer sb = new StringBuffer();
			inputStream = request.getInputStream();
			String s;
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			while ((s = in.readLine()) != null) {
				sb.append(s);
			}
			in.close();
			inputStream.close();
			// 解析xml成map
			Map<String, String> m = new HashMap<String, String>();
			m = XMLUtil.doXMLParse(sb.toString());
			// 过滤空 设置 TreeMap
			SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
			Iterator it = m.keySet().iterator();
			while (it.hasNext()) {
				String parameter = (String) it.next();
				String parameterValue = m.get(parameter);

				String v = "";
				if (null != parameterValue) {
					v = parameterValue.trim();
				}
				packageParams.put(parameter, v);
			}
			// 账号信息
			String key = PayConfigUtil.API_KEY; // key
			// 判断签名是否正确
			if (PayCommonUtil.isTenpaySign("UTF-8", packageParams, key)) {

				String resXml = "";

				if ("SUCCESS".equals(packageParams.get("result_code").toString())) {
					// 这里是支付成功 可以返回很多信息
					String appid = (String) packageParams.get("appid"); // 公众账号ID////
																		// 如：wx8888888888888888
					String mch_id = (String) packageParams.get("mch_id"); // 商户号////
																			// 如：1900000109
					String nonce_str = (String) packageParams.get("nonce_str"); // 随机字符串////
																				// 如：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
					String sign = (String) packageParams.get("sign"); // 签名 ////
																		// 如：C380BEC2BFD727A4B6845133519F3AD6
					String result_code = (String) packageParams.get("result_code"); // 业务结果////
																					// 如：SUCCESS
					String openid = (String) packageParams.get("openid"); // 用户标识
																			// ////
																			// 如：wxd930ea5d5a258f4f
					String trade_type = (String) packageParams.get("trade_type"); // 交易类型//
																					// 如：JSAPI
					String bank_type = (String) packageParams.get("bank_type"); // 付款银行////
																				// 如：
					String total_fee = (String) packageParams.get("total_fee"); // 订单总金额////
																				// 如：单位为分
					String transaction_id = (String) packageParams.get("transaction_id"); // 微信支付订单号////
																							// 如：1217752501201407033233368018
					String out_trade_no = (String) packageParams.get("out_trade_no"); // 商户订单号////
																						// 如：1212321211201407033568112322
					String time_end = (String) packageParams.get("time_end"); // 支付完成时间////
																				// 如：20141030133525////
																				// 格式为yyyyMMddHHmmss
					// 订单明细
					CustomerOrderPayInfo customerOrderPayInfo = new CustomerOrderPayInfo();
					customerOrderPayInfo.setCustomerOrderPayInfoId(UUIDUtils.getId());
					customerOrderPayInfo.setOrderNo(out_trade_no);// 订单号
					customerOrderPayInfo.setPayType(1);// 1线上支付
					customerOrderPayInfo.setPayWayId(1);// 1微信支付
					customerOrderPayInfo.setPayWayName("微信支付");
					customerOrderPayInfo.setOrderPayAmount(new BigDecimal(total_fee));// 订单支付金额
					customerOrderPayInfo.setOrderPayCallback(appid + "&" + mch_id + "&" + nonce_str + "&" + sign + "&" + result_code + "&" + openid + "&" + trade_type + "&" + bank_type + "&" + total_fee + "&" + transaction_id + "&" + out_trade_no + "&" + time_end);
					customerOrderPayInfo.setOrderPayNotify("SUCCESS");
					customerOrderPayInfo.setOrderPayResult(2);// 支付成功
					customerOrderPayInfo.setOrderPayOrderNo(transaction_id);// 微信支付订单号
					customerOrderPayNoticeService.updateCustomerOrderPayInfo(customerOrderPayInfo);
					// 修改Redis缓冲中的订单
					customerOrderService.updateOrderStatu(out_trade_no);
					resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>" + "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
				} else {
					// 失败
					resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>" + "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";

				}
				BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
				out.write(resXml.getBytes());
				out.flush();
				out.close();
				return resXml;
			} else {
				// 签名失败
				return "签名失败";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@ApiOperation(value = "申请退款", httpMethod = "GET", response = JSONObject.class, notes = "申请退款")
	@RequestMapping(value = "/refund_order")
	@ResponseBody
	public JSONObject refundOrder(String orderNo) {

		HttpSession session = request.getSession();
		CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
		JSONObject message = new JSONObject();
		try {
			if (null != customerRegistVo && StringUtils.isNotEmpty(orderNo)) {
				CustomerOrder customerOrder = customerOrderService.getCustomerOrder(orderNo);
				if (null != customerOrder && customerOrder.getOrderStatus().intValue() == 40) {
					// 调用订单退款方法
					int flag = customerOrderService.refundCustomerOrder(customerOrder);
					message.put("status", flag);// 0是失败 1是成功
					return message;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return message;
	}

	/**
	 * @Title: getCustomerOrderByOrderNo
	 * @Description: 根据订单号获取订单详情
	 * @param orderNo
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "根据订单号获取订单详情", httpMethod = "GET", response = JSONObject.class, notes = "根据订单号获取订单详情")
	@RequestMapping(value = "/getCustomerOrderByOrderNo")
	@ResponseBody
	public JSONObject getCustomerOrderByOrderNo(String orderNo) {
		JSONObject message = new JSONObject();
		try {
			CustomerOrder customerOrder = customerOrderService.getCustomerOrder(orderNo);
			List<String> cartIdList = customerOrderCartService.getCartIdByCustomerOrderId(customerOrder.getOrderNo());
			List<CustomerCart> customerCartList = null;
			if (CollectionUtils.isNotEmpty(cartIdList)) {
				for (String cartId : cartIdList) {
					customerCartList = new ArrayList<CustomerCart>();
					CustomerCart customerCart = customerCartService.getCustomerCartByCartId(cartId);
					customerCartList.add(customerCart);
					if (CollectionUtils.isNotEmpty(customerCartList)) {
						message.put("data", customerCartList);
						return InterfaceResultUtil.getReturnMapSuccess(message);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
}
