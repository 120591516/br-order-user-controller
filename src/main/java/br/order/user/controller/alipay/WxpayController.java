package br.order.user.controller.alipay;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jdom.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.common.utils.UUIDUtils;
import br.crm.common.wxpay.utils.ClientCustomSSL;
import br.crm.common.wxpay.utils.HttpUtil;
import br.crm.common.wxpay.utils.MatrixToImageWriter;
import br.crm.common.wxpay.utils.PayCommonUtil;
import br.crm.common.wxpay.utils.PayConfigUtil;
import br.crm.common.wxpay.utils.RequestHandler;
import br.crm.common.wxpay.utils.XMLUtil;
import br.crm.pojo.customer.order.CustomerOrder;
import br.crm.pojo.customer.order.CustomerOrderPayInfo;
import br.crm.pojo.customer.order.CustomerOrderRefund;
import br.crm.service.customer.order.CustomerOrderPayInfoService;
import br.crm.service.customer.order.CustomerOrderRefundService;
import br.crm.service.customer.order.CustomerOrderService;

@Controller
@RequestMapping("/wxpay")
public class WxpayController {

	@Autowired
	private CustomerOrderService customerOrderService;

	@Autowired
	private CustomerOrderPayInfoService customerOrderPayInfoService;
	
	@Autowired
	private CustomerOrderRefundService customerOrderRefundService;

	@ApiOperation(value = "生成二维码图片", httpMethod = "GET", response = JSONObject.class, notes = "生成二维码图片")
	@RequestMapping(value = "/createQRCode", method = RequestMethod.GET)
	@ResponseBody
	public synchronized void createOrderInfo(String orderNo, HttpServletResponse response) {

		int totalMoney = 0;
		CustomerOrder customerOrder = customerOrderService.getCustomerOrder(orderNo);
		if (null != customerOrder && customerOrder.getOrderStatus() == 7) {
			BigDecimal moneyStr = customerOrder.getOrderAmount();
			BigDecimal money = moneyStr.multiply(new BigDecimal(100));
			totalMoney = money.intValue();
			SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
			packageParams.put("appid", PayConfigUtil.APP_ID);// 公众账号appid
			packageParams.put("mch_id", PayConfigUtil.mch_id);// 商户号
			packageParams.put("nonce_str", PayCommonUtil.getRandomStringByLength(32)); // 随机字符串
			packageParams.put("body", PayConfigUtil.body);// 商品名称
			packageParams.put("out_trade_no", orderNo); // 商户订单号
			packageParams.put("total_fee", totalMoney); // 标价金额(默认是分)
			packageParams.put("spbill_create_ip", PayCommonUtil.localIp()); // 终端IP:114.215.222.233
			packageParams.put("notify_url", PayConfigUtil.NOTIFY_URL); // 通知地址
			packageParams.put("trade_type", PayConfigUtil.trade_type); // 交易类型
			String sign = PayCommonUtil.createSign("UTF-8", packageParams, PayConfigUtil.API_KEY);
			packageParams.put("sign", sign);
			String requestXML = PayCommonUtil.getRequestXml(packageParams);// 将请求参数转换为xml格式的string
			System.out.println(requestXML);

			String resXml = HttpUtil.postData(PayConfigUtil.PAY_API, requestXML);

			try {
				Map map = XMLUtil.doXMLParse(resXml);
				String urlCode = (String) map.get("code_url");
				encodeQrcode(urlCode, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 生成二维码图片并直接以流的形式输出到页面
	 * 
	 * @param code_url
	 * @param response
	 */
	/*
	 * public BufferedImage getQRCode(String code_url) {
	 * 
	 * try { return encodeQrcode(code_url, 300, 300); } catch (Exception e) {
	 * e.printStackTrace(); } return null; }
	 */

	/**
	 * 生成二维码图片 不存储 直接以流的形式输出到页面
	 * 
	 * @param content
	 * @param response
	 */
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

	/**
	 * 获取二维码链接
	 * 
	 * @param xmlData
	 * @return
	 */
	/*
	 * private static String QRfromGoogle(String chl) throws Exception { int
	 * widhtHeight = 300; String EC_level = "L"; int margin = 0; chl =
	 * UrlEncode(chl); String QRfromGoogle =
	 * "http://chart.apis.google.com/chart?chs=" + widhtHeight + "x" +
	 * widhtHeight + "&cht=qr&chld=" + EC_level + "|" + margin + "&chl=" + chl;
	 * 
	 * return QRfromGoogle;
	 * 
	 * }
	 * 
	 * 
	 * public static String UrlEncode(String src) throws
	 * UnsupportedEncodingException { //特殊字符处理 return URLEncoder.encode(src,
	 * "UTF-8").replace("+", "%20"); }
	 */

	@ApiOperation(value = "回调方法", httpMethod = "GET", response = JSONObject.class, notes = "回调方法")
	@RequestMapping("/notifyURL")
	@ResponseBody
	public String weixin_notify(HttpServletRequest request, HttpServletResponse response) throws Exception {

		SortedMap<Object, Object> returnParams = new TreeMap<Object, Object>();
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
				String appid = (String) packageParams.get("appid"); // 公众账号ID//
																	// 如：wx8888888888888888
				String mch_id = (String) packageParams.get("mch_id"); // 商户号//
																		// 如：1900000109
				String nonce_str = (String) packageParams.get("nonce_str"); // 随机字符串//
																			// 如：5K8264ILTKCH16CQ2502SI8ZNMTM67VS
				String sign = (String) packageParams.get("sign"); // 签名 //
																	// 如：C380BEC2BFD727A4B6845133519F3AD6
				String result_code = (String) packageParams.get("result_code"); // 业务结果//
																				// 如：SUCCESS
				String openid = (String) packageParams.get("openid"); // 用户标识 //
																		// 如：wxd930ea5d5a258f4f
				String trade_type = (String) packageParams.get("trade_type"); // 交易类型
																				// //
																				// 如：JSAPI
				String bank_type = (String) packageParams.get("bank_type"); // 付款银行//
																			// 如：
				String total_fee = (String) packageParams.get("total_fee"); // 订单总金额//
																			// 如：单位为分
				String transaction_id = (String) packageParams.get("transaction_id"); // 微信支付订单号//
																						// 如：1217752501201407033233368018
				String out_trade_no = (String) packageParams.get("out_trade_no"); // 商户订单号//
																					// 如：1212321211201407033568112322
				String time_end = (String) packageParams.get("time_end"); // 支付完成时间//
																			// 如：20141030133525//
																			// 格式为yyyyMMddHHmmss

				System.out.println("......." + result_code + "...................");
				System.out.println("......." + bank_type + "...................");
				System.out.println("......." + total_fee + "...................");
				System.out.println("......." + transaction_id + "...................");
				System.out.println("......." + out_trade_no + "...................");

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
				CustomerOrder customerOrder = customerOrderService.getCustomerOrder(out_trade_no);
				if (null != customerOrder) {
					customerOrderPayInfo.setCustomerId(customerOrder.getCustomerId());// 用户id
					// 修改订单状态
					customerOrder.setOrderStatus(40);
					customerOrder.setCustomerOrderEditTime(new Date());
					customerOrderService.updateCustomerOrder(customerOrder);
					customerOrderService.updateOrderStatu(out_trade_no);
				}
				customerOrderPayInfo.setCustomerOrderPayInfoCreateTime(new Date());
				customerOrderPayInfo.setCustomerOrderPayInfoEditTime(new Date());
				customerOrderPayInfoService.insertCustomerOrderPayInfo(customerOrderPayInfo);

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
	}

	@ApiOperation(value = "申请退款", httpMethod = "POST", response = JSONObject.class, notes = "申请退款")
	@RequestMapping(value="/refund",method=RequestMethod.POST)
	@ResponseBody
	public JSONObject  refund(String orderNo,HttpServletResponse response) {
		JSONObject message = new JSONObject();
		try {
			CustomerOrder customerOrder = customerOrderService.getCustomerOrder(orderNo);
			CustomerOrderPayInfo orderPayInfo = customerOrderPayInfoService.getCustomerOrderPayInfo(orderNo);
			if(null!=orderPayInfo&&orderPayInfo.getOrderPayResult().intValue()==2&&StringUtils.isNotEmpty(orderPayInfo.getOrderPayOrderNo())){
				if(null!=customerOrder&&StringUtils.isNotEmpty(customerOrder.getCustomerId())&&customerOrder.getOrderStatus().intValue()==40){
					customerOrder.setOrderStatus(33);//33----退款申请
					customerOrderService.updateCustomerOrder(customerOrder);
					int totalMoney = 0;
					int refundMoney=0;
					BigDecimal moneyStr = customerOrder.getOrderAmount();
					BigDecimal money = moneyStr.multiply(new BigDecimal(100));
					totalMoney = money.intValue();
					BigDecimal orderPayAmount = orderPayInfo.getOrderPayAmount();
					refundMoney=orderPayAmount.intValue();
					if(totalMoney==refundMoney){
						//定义参数
						String out_refund_no = UUIDUtils.getId();// 退款单号
						String out_trade_no = orderNo;// 订单号
						String total_fee = totalMoney+"";// 总金额
						String refund_fee = refundMoney+"";// 退款金额
						String nonce_str = PayCommonUtil.getRandomStringByLength(32);// 随机字符串
						String appid = PayConfigUtil.APP_ID; //微信公众号apid
						String appsecret = PayConfigUtil.AppSecret; //微信公众号appsecret
						String mch_id = PayConfigUtil.mch_id;  //微信商户id
						String op_user_id = PayConfigUtil.mch_id;//就是MCHID
						String partnerkey = PayConfigUtil.API_KEY;//商户平台上的那个KEY		
					SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();
							packageParams.put("appid", appid);
							packageParams.put("mch_id", mch_id);
							packageParams.put("nonce_str", nonce_str);
							packageParams.put("out_trade_no", out_trade_no);//商户订单号
							packageParams.put("out_refund_no", out_refund_no);
							packageParams.put("total_fee", total_fee);
							packageParams.put("refund_fee", refund_fee);
							packageParams.put("op_user_id", op_user_id);
							RequestHandler reqHandler = new RequestHandler(null, null);	
							reqHandler.init(appid, appsecret, partnerkey);
							String sign = reqHandler.createSign(packageParams);
							//退款表
							CustomerOrderRefund customerOrderRefund =new CustomerOrderRefund();
							customerOrderRefund.setCustomerOrderRefundId(out_refund_no);//退款单号
							customerOrderRefund.setOrderNo(orderNo);//订单号
							customerOrderRefund.setOrderRefundResult(0);//退款结果  0--正在处理
							customerOrderRefund.setCustomerId(customerOrder.getCustomerId());//用户id
							customerOrderRefund.setCustomerOrderRefundCreateTime(new Date());
							customerOrderRefund.setCustomerOrderRefundEditTime(new Date());
							customerOrderRefundService.insertCustomerOrderRefund(customerOrderRefund);
							String xml = "<xml>" + "<appid>" + appid + "</appid>" + "<mch_id>"
									+ mch_id + "</mch_id>" + "<nonce_str>" + nonce_str
									+ "</nonce_str>" + "<sign><![CDATA[" + sign + "]]></sign>"
									+ "<out_trade_no>" + out_trade_no + "</out_trade_no>"
									+ "<out_refund_no>" + out_refund_no + "</out_refund_no>"
									+ "<total_fee>" + total_fee + "</total_fee>"
									+ "<refund_fee>" + refund_fee + "</refund_fee>"
									+ "<op_user_id>" + op_user_id + "</op_user_id>" + "</xml>";
							//请求微信退款
							String resultRefund = ClientCustomSSL.doRefund(PayConfigUtil.payrefund, xml);
							System.out.println("resultRefund....."+resultRefund);
							Map<String, String> getMap = parseXml(new String(resultRefund.toString().getBytes(), "utf-8"));
							System.out.println("getMap"+getMap);
							//申请微信退款成
							if("SUCCESS".equals(getMap.get("return_code"))&&"OK".equals(getMap.get("return_msg"))){
								
								String result_code = (String) getMap.get("result_code"); //SUCCESS退款申请接收成功，结果通过退款查询接口查询 
								String app_id = (String) getMap.get("appid"); //公众账号ID
								String mchid = (String) getMap.get("mch_id"); //商户号
								String noncestr = (String) getMap.get("nonce_str"); //随机字符串
								String si_gn = (String) getMap.get("sign"); //签名
								String transaction_id = (String) getMap.get("transaction_id"); //微信订单号
								String outtradeno = (String) getMap.get("out_trade_no"); //商户订单号
								String outrefundno = (String) getMap.get("out_refund_no"); //商户退款单号
								String refund_id = (String) getMap.get("refund_id"); //微信退款单号
								String refundfee = (String) getMap.get("refund_fee"); //微信退款金额
								String totalfee = (String) getMap.get("total_fee"); //标价金额
								String cash_fee = (String) getMap.get("cash_fee"); //现金支付金额
								System.out.println("result_code........."+result_code);
								System.out.println("app_id........."+app_id);
								System.out.println("mchid........."+mchid);			
								System.out.println("noncestr........."+noncestr);			
								System.out.println("si_gn........."+si_gn);			
								System.out.println("transaction_id........."+transaction_id);			
								System.out.println("outtradeno........."+outtradeno);			
								System.out.println("outrefundno........."+outrefundno);			
								System.out.println("refund_id........."+refund_id);			
								System.out.println("refundfee........."+refundfee);			
								System.out.println("totalfee........."+totalfee);			
								System.out.println("cash_fee........."+cash_fee);
								
								if(StringUtils.isNotEmpty(refund_id)){//微信退款单号不为空
									//修改订单状态
									CustomerOrder order = customerOrderService.getCustomerOrder(outtradeno);
									if(null!=order){
										order.setOrderStatus(35);
										order.setOrderRefundStatus(3);//3成功
									}
									//更新退款表
									CustomerOrderRefund cor = customerOrderRefundService.getCustomerOrderRefundById(outrefundno);
									if(null!=cor){
										cor.setOrderNo(outtradeno);//订单号
										cor.setOrderRefundOrderNo(refund_id);//退款交易号
										cor.setOrderRefundDetails("&"+result_code+"&"+app_id+"&"+mchid+"&"+noncestr+"&"+si_gn+
												"&"+transaction_id+"&"+outtradeno+"&"+outrefundno+"&"+refund_id+"&"+refundfee+"&"+totalfee+"&"+cash_fee+"&");//退款详情
									
										cor.setOrderRefundResult(1);//1成功
										cor.setCustomerOrderRefundEditTime(new Date());
										customerOrderRefundService.updateCustomerOrderRefund(cor);
										message.put("status", "0");
										return message;
									}
								}else{
									//失败
									message.put("status", "1");
									return message;
								}
								}
								
						}
						}
			              }
				}catch (Exception e) {
						e.printStackTrace();
					}
		

		return null;			

	}
	
    public static Map<String, String> parseXml(String xml) throws Exception {  
        Map<String, String> map = new HashMap<String, String>();  
        try {
        	Document document = DocumentHelper.parseText(xml);  
            Element root = document.getRootElement(); 
            List<Element> elementList = root.elements();
            for (Element e : elementList){
            	map.put(e.getName(), e.getText());
            	System.out.println(e.getName()+":"+e.getText());
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
        return map;  
    }  
	
}
				
			
	
