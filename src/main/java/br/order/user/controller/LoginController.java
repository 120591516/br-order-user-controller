package br.order.user.controller;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.config.xml.ObjectToJsonTransformerParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.common.utils.JsonUtils;
import br.order.common.utils.InterfaceResultUtil;
import br.order.user.service.empUser.CustomerRegistService;
import br.order.user.vo.empUser.CustomerRegistVo;

/**
 * @ClassName: LoginController
 * @Description: 用户预约登录接口
 * @author 杨春阳
 * @date 2016年11月16日 下午4:54:19
 */
@Controller
@RequestMapping("/login")
public class LoginController {

	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private CustomerRegistService customerRegistService;

	/**
	 * @Title: login
	 * @Description: 用户预约登录
	 * @param username
	 * @param password
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "用户预约个人登录", httpMethod = "GET", response = JSONObject.class, notes = "用户预约登录")
	@RequestMapping(value = "/customerInfoLogin")
	@ResponseBody
	public JSONObject customerInfoLogin(
			@ApiParam(required = true, name = "username", value = "用户名") String username, 
			@ApiParam(required = true, name = "password", value = "密码") String password, 
			@ApiParam(required = true, name = "type", value = "登录方式") Integer type, 
			@ApiParam(required = true, name = "validCode", value = "验证码") String validCode, 
			@ApiParam(required = true, name = "auto", value = "是否自动登录") String auto) {
		JSONObject message = new JSONObject();
		try {

			// 图片验证码校验
			if (StringUtils.isEmpty(validCode)) {
				message.put("message", "验证码为空");
				return InterfaceResultUtil.getReturnMapValidValue(message);
			}
			if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
				return InterfaceResultUtil.getReturnMapValidUser();
			}
			if (null == request.getSession().getAttribute("validateCode") || StringUtils.isEmpty(request.getSession().getAttribute("validateCode").toString())) {
				message.put("message", "非法请求");
				return InterfaceResultUtil.getReturnMapValidValue(message);
			}
			if (!validCode.equalsIgnoreCase(request.getSession().getAttribute("validateCode").toString())) {

				message.put("message", "验证码错误");
				return InterfaceResultUtil.getReturnMapValidValue(message);
			}
			Md5Hash md5 = new Md5Hash(password);
			String passWord = md5.toString();
			// 手机号的正则表达式
			String regexPhone = "^[1][0-9]{10}$";
			// 邮箱的正则表达式
			String regexEmail = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			// 手机登录
			if (type == 0) {
				if (username.matches(regexPhone)) {
					CustomerRegistVo loginByPhone = customerRegistService.getLoginByPhone(username, type);
					if (loginByPhone != null) {
						// 判断当前用户是否开通了个人
						if (1 == loginByPhone.getCustomerPerson()) {
							if (passWord.equals(loginByPhone.getCustomerRegistPassword())) {
								HttpSession session = request.getSession();
								session.setAttribute("loginUser", loginByPhone);
								message.put("data", "登录成功");
								message.put("value", loginByPhone);

								// 登陆成功 保存cookie
								// 接收用户姓名
								// 将用户信息放到session
								// 判断auto是否是-1
								if (auto.equals("0")) {
									// int day = Integer.parseInt(auto);//自动保存天数
									int seconds = 60 * 60 * 24 * 7;
									Cookie c = new Cookie("autoLogin",JsonUtils.objectToJson(loginByPhone));// 声明cookie
									c.setMaxAge(seconds); // 设置天数
									c.setPath(request.getContextPath());// 根路径
									System.out.println("Cookie setPath"+request.getContextPath()); 
									response.addCookie(c); // 保存cookie
									System.out.println("Cookie setPath"+c.getName());
								}
								 
								return InterfaceResultUtil.getReturnMapSuccess(message);
							} else {
								message.put("data", "手机号或密码错误，请重新登录！");
								return InterfaceResultUtil.getReturnMapError(message);
							}
						} else {
							message.put("data", "该用户没有开通个人注册！");
							return InterfaceResultUtil.getReturnMapError(message);
						}

					} else {
						message.put("data", "该用户不存在，请重新登录或注册！");
						return InterfaceResultUtil.getReturnMapError(message);
					}
				} else {
					message.put("data", "手机号格式不符合要求，请重新输入！");
					return InterfaceResultUtil.getReturnMapError(message);
				}
			} else {
				// 邮箱登登录
				if (username.matches(regexEmail)) {
					CustomerRegistVo loginByEmail = customerRegistService.getLoginByPhone(username, type);
					if (loginByEmail != null) {
						if (1 == loginByEmail.getCustomerPerson()) {
							if (passWord.equals(loginByEmail.getCustomerRegistPassword())) {
								HttpSession session = request.getSession();
								session.setAttribute("loginUser", loginByEmail);
								message.put("data", "登录成功！");
								message.put("value", loginByEmail);
								return InterfaceResultUtil.getReturnMapSuccess(message);
							} else {
								message.put("data", "邮箱或密码错误，请重新登录！");
								return InterfaceResultUtil.getReturnMapError(message);
							}
						} else {
							message.put("data", "该用户没有开通个人注册！");
							return InterfaceResultUtil.getReturnMapError(message);
						}

					} else {
						message.put("data", "该用户不存在，请重新登录或注册！");
						return InterfaceResultUtil.getReturnMapError(message);
					}

				} else {
					message.put("data", "邮箱格式不符合要求，请重新输入！");
					return InterfaceResultUtil.getReturnMapError(message);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: enterpriseLogin
	 * @Description: 用户预约企业登录
	 * @param username
	 * @param password
	 * @param type
	 * @param validCode
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "企业登录", httpMethod = "GET", response = JSONObject.class, notes = "企业登录")
	@RequestMapping(value = "/enterpriseLogin")
	@ResponseBody
	public JSONObject enterpriseLogin(
			@ApiParam(required = true, name = "username", value = "用户名") String username, 
			@ApiParam(required = true, name = "password", value = "密码") String password, 
			@ApiParam(required = true, name = "type", value = "登录方式") Integer type, 
			@ApiParam(required = true, name = "validCode", value = "验证码") String validCode, 
			@ApiParam(required = true, name = "auto", value = "是否自动登录", defaultValue = "1") String auto) {
		JSONObject message = new JSONObject();
		try {

			// 图片验证码校验
			if (StringUtils.isEmpty(validCode)) {
				message.put("message", "验证码为空");
				return InterfaceResultUtil.getReturnMapValidValue(message);
			}
			if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
				return InterfaceResultUtil.getReturnMapValidUser();
			}
			if (null == request.getSession().getAttribute("validateCode") || StringUtils.isEmpty(request.getSession().getAttribute("validateCode").toString())) {
				message.put("message", "非法请求");
				return InterfaceResultUtil.getReturnMapValidValue(message);
			}
			if (!validCode.equalsIgnoreCase(request.getSession().getAttribute("validateCode").toString())) {

				message.put("message", "验证码错误");
				return InterfaceResultUtil.getReturnMapValidValue(message);
			}
			Md5Hash md5 = new Md5Hash(password);
			String passWord = md5.toString();
			// 手机号的正则表达式
			String regexPhone = "^[1][0-9]{10}$";
			// 邮箱的正则表达式
			String regexEmail = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			// 手机登录
			if (type == 0) {
				if (username.matches(regexPhone)) {
					CustomerRegistVo loginByPhone = customerRegistService.getLoginByPhone(username, type);
					if (loginByPhone != null) {
						// 判断当前用户是否开通了企业
						if (1 == loginByPhone.getCustomerCompany()) {
							if (passWord.equals(loginByPhone.getCustomerRegistPassword())) {
								HttpSession session = request.getSession();
								session.setAttribute("loginUser", loginByPhone);
								message.put("value", loginByPhone);
								message.put("data", "登录成功");
								// 登陆成功 保存cookie
								// 接收用户姓名
								// 将用户信息放到session
								// 判断auto是否是-1
								if (auto.equals("0")) { 
									/* int day = Integer.parseInt(auto);//自动保存天数 */
									int seconds = 60 * 60 * 24 * 7; 
									Cookie c = new Cookie("autoLogin",JsonUtils.objectToJson(loginByPhone)); // 声明cookie
									c.setMaxAge(seconds); // 设置天数
									c.setPath(request.getContextPath());// 根路径
									response.addCookie(c); // 保存cookie
								} 
								return InterfaceResultUtil.getReturnMapSuccess(message);
							} else {
								message.put("data", "手机号或密码错误，请重新登录！");
								return InterfaceResultUtil.getReturnMapError(message);
							}
						} else {
							message.put("data", "该用户没有开通企业注册！");
							return InterfaceResultUtil.getReturnMapError(message);
						}
					} else {
						message.put("data", "该用户不存在，请重新登录或注册！");
						return InterfaceResultUtil.getReturnMapError(message);
					}

				} else {
					message.put("data", "手机号格式不符合要求，请重新输入！");
					return InterfaceResultUtil.getReturnMapError(message);
				}

			} else {
				// 邮箱登登录
				if (username.matches(regexEmail)) {
					CustomerRegistVo loginByEmail = customerRegistService.getLoginByPhone(username, type);
					if (loginByEmail != null) {
						if (1 == loginByEmail.getCustomerCompany()) {
							if (passWord.equals(loginByEmail.getCustomerRegistPassword())) {
								HttpSession session = request.getSession();
								session.setAttribute("loginUser", loginByEmail);
								message.put("value", loginByEmail);
								message.put("data", "登录成功！");
								return InterfaceResultUtil.getReturnMapSuccess(message);
							} else {
								message.put("data", "邮箱或密码错误，请重新登录！");
								return InterfaceResultUtil.getReturnMapError(message);
							}
						} else {
							message.put("data", "该用户没有开通企业注册！");
							return InterfaceResultUtil.getReturnMapError(message);
						}

					} else {
						message.put("data", "该用户不存在，请重新登录或注册！");
						return InterfaceResultUtil.getReturnMapError(message);
					}

				} else {
					message.put("data", "邮箱格式不符合要求，请重新输入！");
					return InterfaceResultUtil.getReturnMapError(message);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	/**
	 * @Title: loginOut
	 * @Description: 退出登录
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 */
	@ApiOperation(value = "用户退出", httpMethod = "GET", response = JSONObject.class, notes = "用户退出接口")
	@RequestMapping("/loginOut")
	@ResponseBody
	public JSONObject loginOut(HttpServletResponse response) {
		JSONObject message = new JSONObject();
		try {
			HttpSession session = request.getSession();
		
			Enumeration<String> en = session.getAttributeNames(); 
			while (en.hasMoreElements()) {
				session.removeAttribute(en.nextElement().toString());
			}
			session.invalidate();
			message.put("data", "已成功退出，欢迎下次登录");
			// cookie自动登录 删除
			Cookie c = new Cookie("autoLogin", "ddd");
			c.setMaxAge(0);
			c.setPath(request.getContextPath());
			response.addCookie(c); 
			//登录状态删除
			 Cookie cookie=new Cookie("loginStatus", String.valueOf(1));
			 cookie.setMaxAge(0); 
			 cookie.setPath(request.getContextPath());//设置cookie路径
			 response.addCookie(cookie);
			return InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return InterfaceResultUtil.getReturnMapValidUser();
	}

}
