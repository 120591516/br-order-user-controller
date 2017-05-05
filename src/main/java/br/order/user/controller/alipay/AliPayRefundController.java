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
 
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody; 

import br.crm.common.alipay.config.AlipayConfig;
import br.crm.common.alipay.util.AlipayNotify;
import br.crm.common.alipay.util.AlipaySubmit;
import br.crm.pojo.customer.order.CustomerOrderPayInfo;
import br.crm.pojo.customer.order.CustomerOrderRefund;
import br.crm.service.customer.order.CustomerOrderPayInfoService;
import br.crm.service.customer.order.CustomerOrderRefundService;
 
/**
 * @ClassName: AliPayController 支付宝订单页面
 * @Description: TODO
 * @author kangting
 * @date 2016年11月2日 下午3:33:24
 * 
 */
@Controller
@RequestMapping("/alipayRefund")
public class AliPayRefundController {
 
	@Autowired
	private CustomerOrderPayInfoService customerOrderPayInfoService;
	
	@Autowired
	private CustomerOrderRefundService customerOrderRefundService; 
	
	@Autowired
	private HttpServletRequest request;

	

	@RequestMapping("/refund")
	public void payOrder(String orderNO, HttpServletResponse response, HttpServletRequest request) {
		//订单是否存在
		if (orderNO.isEmpty()) {
			System.out.println("refundId 参数不存在");
			return;
		}
		CustomerOrderRefund customerOrderRefund= customerOrderRefundService.getCustomerOrderRefundByOrderNO(orderNO);
		System.out.println("customerOrderRefund 参数不存在");
		if(customerOrderRefund==null){
			return;
		}
		System.out.println("customerOrderRefund OrderRefundOrderNo"+customerOrderRefund.getOrderRefundOrderNo());
		//添加 
		String detailData ="";
		CustomerOrderPayInfo payInfo=customerOrderPayInfoService.getCustomerOrderPayInfoById(customerOrderRefund.getCustomerOrderPayInfoId());
		if(payInfo!=null&&payInfo.getOrderPayAmount()!=null){
				detailData=customerOrderRefund.getCustomerOrderPayInfoNo()+"^"+payInfo.getOrderPayAmount()+"^"+"协议退款";
			} 
		System.out.println("customerOrderRefund detailData"+detailData);
		
		String html = "";
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", AlipayConfig.refund_service);
        sParaTemp.put("partner", AlipayConfig.partner);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("notify_url", AlipayConfig.refund_notify_url);
		sParaTemp.put("seller_user_id", AlipayConfig.seller_id);
		sParaTemp.put("refund_date", AlipayConfig.refund_date);
		sParaTemp.put("batch_no", customerOrderRefund.getOrderRefundOrderNo());
		sParaTemp.put("batch_num", AlipayConfig.batch_num);
		sParaTemp.put("detail_data",detailData); /*必填(支付宝交易号^退款金额^备注)多笔请用#隔开*/
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
		System.out.println("alipay Refund param0: " + params.toString());
		// 交易状态 
		try {
			System.out.println("alipay Refund param1: " + params.toString());
			//获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
			//批次号
				
			String batch_no = new String(request.getParameter("batch_no").getBytes("ISO-8859-1"),"UTF-8"); 

			//批量退款数据中的详细信息
			String result_details = new String(request.getParameter("result_details").getBytes("ISO-8859-1"),"UTF-8");
			
			if (AlipayNotify.verify(params)) {// 验证成功 
				
				System.out.println("alipay Refund param2: " + params.toString());
				 //业务逻辑处理
				CustomerOrderRefund suCustomerOrderRefund=new CustomerOrderRefund();
				suCustomerOrderRefund.setOrderRefundDetails(result_details);
				suCustomerOrderRefund.setOrderRefundOrderNo(batch_no); 
				customerOrderRefundService.updateRefundInfo(suCustomerOrderRefund);
				System.out.println("alipay Refund param3: " + params.toString());	
					return "success";
				 
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
