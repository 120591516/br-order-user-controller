package br.order.user.controller.alipay;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/wx")
public class WxpaySignatureController {

	@ApiOperation(value = "产生随机字符串", httpMethod = "GET", response = JSONObject.class, notes = "产生随机字符串")
	@RequestMapping("/signature")
	@ResponseBody
	public String signature(String signature, String timestamp, String nonce, String echostr) {
		System.out.println("signature:"+signature);
		System.out.println("timestamp:"+timestamp);
		System.out.println("nonce:"+nonce);
		System.out.println("echostr:"+echostr);
		return echostr;
	}

}
