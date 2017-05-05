/**   
* @Title: AliPayController.java 
* @Package br.order.user.contrller.alipay 
* @Description: TODO
* @author kangting   
* @date 2016年11月2日 下午3:33:24 
* @version V1.0   
*/
package br.order.user.controller.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import br.crm.common.alipay.config.AlipayConfig;
import br.crm.common.alipay.util.AlipayNotify;
import br.crm.common.alipay.util.AlipaySubmit;
import br.crm.common.utils.UUIDUtils;
import br.crm.pojo.customer.order.CustomerOrder;
import br.crm.pojo.customer.order.CustomerOrderPayInfo;
import br.crm.service.customer.order.CustomerOrderPayInfoService;
import br.crm.service.customer.order.CustomerOrderPayNoticeService;
import br.crm.service.customer.order.CustomerOrderService;
import br.order.common.utils.InterfaceResultUtil;
import br.order.user.pojo.empUser.CustomerInfo;
import br.order.user.service.empUser.CustomerInfoService;
import br.order.user.vo.empUser.CustomerRegistVo;

/**
 * @ClassName: AliPayController 支付宝订单页面
 * @Description: TODO
 * @author kangting
 * @date 2016年11月2日 下午3:33:24
 * 
 */
@Controller
@RequestMapping("/alipay")
public class AliPayController {
 
	@Autowired
	private CustomerInfoService customerInfoService;
	@Autowired
	private CustomerOrderPayNoticeService customerOrderPayNoticeService;
	@Autowired
	private CustomerOrderPayInfoService customerOrderPayInfoService;
	
	@Autowired
	private CustomerOrderService customerOrderService;
	
	@Autowired
	private HttpServletRequest request;

	

	@RequestMapping("/payOrder")
	public void payOrder(String orderNO, HttpServletResponse response, HttpServletRequest request) {
		//订单是否存在
		if (orderNO.isEmpty()) {
			System.out.println("orderNO 参数不存在");
			return;
		}
		//用户是否登陆
		/*HttpSession session = request.getSession();
		CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
		if(customerRegistVo==null){
			System.out.println("未登陆账户");
			return;
		}  */	
		CustomerOrder customerOrder=customerOrderService.getCustomerOrder(orderNO);
		//订单是否存在
		if(customerOrder==null){
			System.out.println("订单不存在");
			return;
		}else {
			System.out.println("tall"+customerOrder.getOrderPayAmount()+"feeString"+String.valueOf(customerOrder.getOrderPayAmount()));
			//订单是否是提交状态
			if(customerOrder.getOrderStatus()!=null&&customerOrder.getOrderStatus()!=1){
				System.out.println("订单 状态 不符合");
				return;
			}
			//订单金额是否满足要求
			if(customerOrder.getOrderPayAmount()!=null&&(customerOrder.getOrderPayAmount().compareTo(BigDecimal.valueOf(0))==0)){
				System.out.println("金额不对 是否是提交状态");
				return;
			}	
		}
		String html = "";
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service",AlipayConfig.service);
		sParaTemp.put("partner", AlipayConfig.partner);
		sParaTemp.put("seller_id", AlipayConfig.seller_id); 
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("qr_pay_mode", AlipayConfig.qr_pay_mode);
		sParaTemp.put("payment_type", AlipayConfig.payment_type);
		sParaTemp.put("notify_url",AlipayConfig.notify_url); 
		sParaTemp.put("return_url", AlipayConfig.return_url);
		sParaTemp.put("out_trade_no", orderNO);
		sParaTemp.put("total_fee", String.valueOf(customerOrder.getOrderPayAmount()));
		sParaTemp.put("subject", "标软体检套餐"); 
		html = AlipaySubmit.buildRequest(sParaTemp, "get", "确定");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		PrintWriter printWriter = null;
		try {
			printWriter = response.getWriter();
			printWriter.print(html);
			printWriter.flush();
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@RequestMapping(value = "/notify", method = RequestMethod.POST)
	@ResponseBody
	public String alipayNotify(HttpServletRequest request) {
		Map<String, String> params = getRequestParam(request);
		System.out.println("alipay notify param0: " + params.toString());
		// 交易状态
		String trade_status = null, out_trade_no = null, trade_no = null,buyer_email=null,buyer_id=null;
		try {
			System.out.println("alipay notify param1: " + params.toString());
			//支付状态
			trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
			// 商户订单号
			out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
			// 支付宝交易号
			trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
			if (AlipayNotify.verify(params)) {// 验证成功 
				System.out.println("alipay notify param2: " + params.toString());
				if (trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")) { 
					System.out.println("alipay notify param3: " + params.toString());
					// 判断该笔订单是否在商户网站中已经做过处理
					// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					// 如果有做过处理，不执行商户的业务程序  
					CustomerOrderPayInfo customerOrderPayInfo=new CustomerOrderPayInfo();
					customerOrderPayInfo.setOrderNo(out_trade_no);
					customerOrderPayInfo.setPayType(1);
					customerOrderPayInfo.setPayWayId(2);
					customerOrderPayInfo.setPayWayName("支付宝支付");  
					customerOrderPayInfo.setOrderPayNotify(params.toString());
					customerOrderPayInfo.setOrderPayOrderNo(trade_no); 
					customerOrderPayInfo.setOrderPayResult(2); 
					System.out.println("alipay notify param31: " + customerOrderPayInfo);
					System.out.println("alipay notify param31 out_trade_no: " + out_trade_no+"trade_no"+trade_no);
					customerOrderPayNoticeService.updateCustomerOrderPayInfo(customerOrderPayInfo);
					System.out.println("alipay notify param32: " + customerOrderPayInfo);
					return "success";
				} else {  
					System.out.println("alipay-notify" + (Throwable) params); 
					System.out.println("alipay notify param4 TRADE_SUCCESS fail" + params.toString());
				}
			}else {
				System.out.println("alipay notify param5 verify fail" + params.toString());
			} 
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(); 
		}  
		return "fail";

	} 

	// 获取请求返回的参数
	private Map<String, String> getRequestParam(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		return params;
	}
}
