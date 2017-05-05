package br.order.user.controller.empUser;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import br.crm.pojo.dict.DictEmailRecord;
import br.crm.service.dict.DictEmailService;
import br.crm.service.dict.DictSMSRecordService;
import br.order.common.utils.SimpleEmail;
import br.order.redis.redis.RedisService;
import br.order.user.pojo.empUser.CustomerInfo;
import br.order.user.pojo.empUser.CustomerRegist;
import br.order.user.pojo.empUserRelation.Relationship;
import br.order.user.service.empUser.CustomerInfoService;
import br.order.user.service.empUser.CustomerRegistService;
import br.order.user.service.empUserRelation.RelationshipService;
import br.order.user.vo.empUser.CustomerRegistVo;
import br.order.user.vo.empUserRelation.RelationShipVo;

/** 
* (客户信息controller)
* @ClassName: CustomerInfoController 
* @Description: 客户信息controller
* @author 杨春阳
* @date 2016年9月13日 下午4:24:05 
*/
@Controller
@RequestMapping("/customerInfo")
public class CustomerInfoController {
	
    /**
     * {客户信息service}
     */
    @Autowired
    private CustomerInfoService customerInfoService;
    /**
     * {关系表service}
     */
    @Autowired
    private DictEmailService dictEmailService;
    @Autowired
    private RelationshipService relationshipService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private DictSMSRecordService dictSMSRecordService;
    @Autowired
	private CustomerRegistService customerRegistService;
    @Autowired
    private SimpleEmail simpleEmail;
    public SimpleEmail getSimpleEmail() {
		return simpleEmail;
	}

	public void setSimpleEmail(SimpleEmail simpleEmail) {
		this.simpleEmail = simpleEmail;
	}
	
	@Autowired
	private RedisService redisService;
	

	/** 
    * @Title: insertCustomerInfo 
    * @Description: TODO(添加基本信息)
    * @param customerInfo 客户基本信息
    * @return JSONObject    
    * @throws 
    */
    @ApiOperation(value = "添加基本信息", httpMethod = "POST", response = JSONObject.class, notes = "添加基本信息")
    @RequestMapping(value = "/insertCustomerInfo", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject insertCustomerInfo(
            @ApiParam(required = true, name = "customerInfo", value = "customerInfo,基本信息对象") CustomerInfo customerInfo) {
        JSONObject message = new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerInfo customerInfoSess = (CustomerInfo) session.getAttribute("customerInfo");
            customerInfoSess = new CustomerInfo();//测试使用
            customerInfoSess.setCustomerInfoId("1");//测试使用
            customerInfo.setCustomerInfoEditId(customerInfoSess.getCustomerInfoId());
            customerInfo.setCustomerInfoStatus(0);
            customerInfo.setCustomerInfoCreateTime(new Date());
            customerInfo.setCustomerInfoEditTime(customerInfo.getCustomerInfoCreateTime());
            String customerInfoId = customerInfoService.insertCustomerInfo(customerInfo);
            if(customerInfoId.length()>0){
                Relationship relationship=new Relationship();
                relationship.setCustomerInfoId(customerInfoSess.getCustomerInfoId());
                relationship.setCustomerInfoRelationId(customerInfoId);
                relationship.setStatus(0);
                relationship.setCreatetime(new Date());
                relationship.setEdittime(relationship.getCreatetime());
                int i=relationshipService.insertRelationship(relationship);
                message.put("data", i);
                return InterfaceResultUtil.getReturnMapSuccess(message);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: updateCustomerInfo 
    * @Description:  TODO(修改基本信息)
    * @param customerInfo 客户信息对象
    * @return JSONObject    
    * @throws 
    */
    
    @ApiOperation(value = "修改基本信息", httpMethod = "POST", response = JSONObject.class, notes = "修改基本信息")
    @RequestMapping(value = "/updateCustomerInfo", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject updateCustomerInfo(
            @ApiParam(required = true, name = "customerInfo", value = "customerInfo,基本信息对象") CustomerInfo customerInfo) {
        JSONObject message = new JSONObject();
        try {
        	//从登录接口中的session中获取当前登录的用户id
            HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo =  (CustomerRegistVo) session.getAttribute("loginUser");
    		String customerInfoId = customerRegistVo.getCustomerInfoId();
    		customerInfo.setCustomerInfoEditId(customerInfoId);
            int i = customerInfoService.updateCustomerInfo(customerInfo);
            message.put("data", i);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: getCustomerInfo 
    * @Description:  TODO(登录用户的个人资料(获取登录用户基本信息))
    * @return JSONObject    
    * @throws 
    */
    
    @ApiOperation(value = "获取登录用户基本信息", httpMethod = "GET", response = JSONObject.class, notes = "获取登录用户基本信息")
    @RequestMapping("/getCustomerInfoById")
    @ResponseBody
    public JSONObject getCustomerInfoById() {
        JSONObject message = new JSONObject();
        try {
        	//从登录接口中的session中获取当前登录的用户id
            HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo =  (CustomerRegistVo) session.getAttribute("loginUser");
    		String customerInfoId = customerRegistVo.getCustomerInfoId();
            CustomerInfo customerInfo = customerInfoService.getCustomerInfo(customerInfoId);
            message.put("data", customerInfo);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: getCustomerInfoByPage 
    * @Description:  TODO((根据登录用户查询家庭成员信息)个人亲友关系)
    * @param page 页数
    * @param rows 行数
    * @param relationShipVo 条件查询对象
    * @return JSONObject    
    * @throws 
    */
    
    @ApiOperation(value = "根据登录用户查询家庭成员信息", httpMethod = "GET", response = JSONObject.class, notes = "根据登录用户查询家庭成员信息")
    @RequestMapping("/getCustomerInfoByPage")
    @ResponseBody
    public JSONObject getCustomerInfoByPage(
            @ApiParam(required = true, name = "page", value = "page,当前页") @RequestParam(value = "page", defaultValue = "1", required = true) Integer page,
            @ApiParam(required = true, name = "rows", value = "rows,每页显示条数") @RequestParam(value = "rows", defaultValue = "10", required = true) Integer rows,
            @ApiParam(required =true ,name="relationShipVo" ,value="relationShipVo,查询条件对象")RelationShipVo relationShipVo){
        JSONObject message=new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerInfo customerInfo = (CustomerInfo) session.getAttribute("customerInfo");
            relationShipVo.setCustomerInfoId(customerInfo.getCustomerInfoId());
            PageInfo<RelationShipVo> pageInfo = relationshipService.getRelationship(page, rows, relationShipVo);
            message.put("data", pageInfo);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }
    /** 
    * @Title: deleteCustomerInfo 
    * @Description:  TODO(删除关系成员基本信息)
    * @param customerInfoId 客户信息id
    * @return JSONObject    
    * @throws 
    */
    
    @ApiOperation(value = "删除关系成员基本信息", httpMethod = "GET", response = JSONObject.class, notes = "删除关系成员基本信息")
    @RequestMapping("/deleteCustomerInfo")
    @ResponseBody
    public JSONObject deleteCustomerInfo(@ApiParam(required =true ,name="customerInfoId",value="customerInfoId,基本信息Id")String customerInfoId){
        JSONObject message=new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerInfo customerInfo = (CustomerInfo) session.getAttribute("customerInfo");
            customerInfo = new CustomerInfo();//测试使用
            customerInfo.setCustomerInfoId("1");//测试使用
            CustomerInfo customerInfo1=customerInfoService.getCustomerInfo(customerInfoId); 
            customerInfo1.setCustomerInfoStatus(1);
            int i =customerInfoService.updateCustomerInfo(customerInfo1);
            if (i>0) {
                Relationship relationShip=new Relationship();
                relationShip.setEdittime(new Date());
                relationShip.setCustomerInfoRelationId(customerInfoId);
                relationShip.setCustomerInfoId(customerInfo.getCustomerInfoId());
                relationShip.setStatus(1);
                 i=relationshipService.updateByExampleSelective(relationShip);
                 message.put("data", i);
                 return InterfaceResultUtil.getReturnMapSuccess(message);
            }
        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }
    /** 
     * @Title: getCustomerInfoByInfo 
     * @Description:  TODO(修改手机号邮箱重名校验)
     * @param customerInfoPhone 手机号
     * @param customerInfoEmail 邮箱
     * @return JSONObject    
     * @throws 
     */
    
    @ApiOperation(value = "手机邮箱用户名重名校验", httpMethod = "GET", response = JSONObject.class, notes = "手机邮箱用户名重名校验")
    @RequestMapping("/checkCustomerInfoByInfo")
    @ResponseBody
    public JSONObject getCustomerInfoByInfo(
    		@ApiParam(required =true ,name="customerInfoPhone",value="customerInfoPhone,注册手机号")String customerInfoPhone,
    		@ApiParam(required =true ,name="customerInfoEmail",value="customerInfoEmail,注册邮箱")String customerInfoEmail) {
    	JSONObject message = new JSONObject();
    	try {
    		HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo =  (CustomerRegistVo) session.getAttribute("loginUser");
    		String customerInfoId = customerRegistVo.getCustomerInfoId();
    		//手机号格式校验
    		String check ="^[1][0-9]{10}$";
    		String regex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    		if(StringUtils.isNotEmpty(customerInfoPhone)){
    			if(customerInfoPhone.matches(check)){
        			int i =customerInfoService.getCountByExample(customerInfoId ,customerInfoPhone,customerInfoEmail);
        			if(i>0){
        				message.put("data","手机号重复，请重新输入");    			
        				return InterfaceResultUtil.getReturnMapSuccess(message);
        			}
        			else{
        				message.put("data",0); 
            			return InterfaceResultUtil.getReturnMapSuccess(message);  
        			}        			   	
        		}   
    			else{
    				message.put("data","手机号格式不对，请重新输入！");    			
        			return InterfaceResultUtil.getReturnMapSuccess(message);   
    			}
    		}
    		//邮箱
    		if(StringUtils.isNotEmpty(customerInfoEmail)){
    			if(customerInfoEmail.matches(regex)){
        			int i =customerInfoService.getCountByExample(customerInfoId,customerInfoPhone,customerInfoEmail);    			
        			if(i>0){
        				message.put("data","邮箱号重复，请重新输入");    			
        				return InterfaceResultUtil.getReturnMapSuccess(message);
        			}
        			else{
        				message.put("data",0); 
            			return InterfaceResultUtil.getReturnMapSuccess(message);  
        			}        	
        		}   
    			else{
    				message.put("data","邮箱格式不对，请重新输入！");    			
        			return InterfaceResultUtil.getReturnMapSuccess(message);   
    			}
    		}   
    		       
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
     * @Title: getCustomerInfoByInfo 
     * @Description:  TODO(注册手机号邮箱重名校验)
     * @param customerInfoPhone 注册手机号
     * @param customerInfoEmail 注册邮箱
     * @return JSONObject    
     * @throws 
     */
    
    @ApiOperation(value = "手机邮箱用户名重名校验", httpMethod = "GET", response = JSONObject.class, notes = "手机邮箱用户名重名校验")
    @RequestMapping("/checkCustomerRegister")
    @ResponseBody
    public JSONObject checkCustomerRegister(
    		@ApiParam(required =true ,name="customerInfoPhone",value="customerInfoPhone,注册手机号")String customerInfoPhone,
    		@ApiParam(required =true ,name="customerInfoEmail",value="customerInfoEmail,注册邮箱")String customerInfoEmail) {
    	JSONObject message = new JSONObject();
    	try {
    		
    		//手机号格式校验
    		String check ="^[1][0-9]{10}$";
    		String regex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    		if(StringUtils.isNotEmpty(customerInfoPhone)){
    			if(customerInfoPhone.matches(check)){
        			int i =customerInfoService.getCountBy(customerInfoPhone,customerInfoEmail);
        			if(i>0){
        				message.put("data","手机号重复，请重新输入");    			
        				return InterfaceResultUtil.getReturnMapSuccess(message);
        			}
        			else{
        				message.put("data",0); 
            			return InterfaceResultUtil.getReturnMapSuccess(message);  
        			}        			   	
        		}   
    			else{
    				message.put("data","手机号格式不对，请重新输入！");    			
        			return InterfaceResultUtil.getReturnMapSuccess(message);   
    			}
    		}
    		//邮箱
    		if(StringUtils.isNotEmpty(customerInfoEmail)){
    			if(customerInfoEmail.matches(regex)){
        			int i =customerInfoService.getCountBy(customerInfoPhone,customerInfoEmail);    			
        			if(i>0){
        				message.put("data","邮箱号重复，请重新输入");    			
        				return InterfaceResultUtil.getReturnMapSuccess(message);
        			}
        			else{
        				message.put("data",0); 
            			return InterfaceResultUtil.getReturnMapSuccess(message);  
        			}        	
        		}   
    			else{
    				message.put("data","邮箱格式不对，请重新输入！");    			
        			return InterfaceResultUtil.getReturnMapSuccess(message);   
    			}
    		}   
    		       
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: sendVerifyCode 
    * @Description:  TODO(发送手机号验证码)
    * @param customerInfoPhone 注册手机号
    * @param customerInfoEmail 注册邮箱
    * @return    设定文件 
    * @return JSONObject    返回类型 
    */
    @ApiOperation(value = "发送手机验证码", httpMethod = "GET", response = JSONObject.class, notes = "发送手机验证码")
    @RequestMapping("/sendVerifyCode")
    @ResponseBody
    public JSONObject sendVerifyCode (
            @ApiParam(required =true ,name="customerInfoPhone",value="customerInfoPhone,注册手机号")String customerInfoPhone,
            @ApiParam(required =true ,name="customerInfoEmail",value="customerInfoEmail,注册邮箱")String customerInfoEmail){
    	JSONObject message=new JSONObject();
    	try {
    		Map<String,Object> map =null;
    		if(StringUtils.isNotEmpty(customerInfoPhone)){
    	   		Integer content =(int) ((Math.random()*9+1)*100000);    			
        		map=dictSMSRecordService.sendSMS(customerInfoPhone, content.toString());
        		message.put("data", map);
        		return InterfaceResultUtil.getReturnMapSuccess(message);
			}
    		if(StringUtils.isNotEmpty(customerInfoEmail)){
    			Integer emailContent = (int)((Math.random()*9+1)*100000);
    			
    			String subject ="发送邮箱验证";		    			
    			simpleEmail.sendMail(subject, emailContent.toString(), customerInfoEmail);
    			redisService.set(RedisConstant.br_order_dict_EMAILRecord_dictEmailTo.concat(customerInfoEmail), emailContent.toString(), 360);
    			DictEmailRecord dictEmail = new DictEmailRecord();
    			dictEmail.setDictEmailTitle(subject);
    			dictEmail.setDictEmailContent(emailContent.toString());
    			dictEmail.setDictEmailTo(customerInfoEmail);
    			Long insertEmail = dictEmailService.insertEmail(dictEmail);
    			message.put("data", insertEmail);
        		return InterfaceResultUtil.getReturnMapSuccess(message);
    		}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
    	return InterfaceResultUtil.getReturnMapError(message);
    }
    
    /** 
    * @Title: updateCustomerInfoPhone 
    * @Description: TODO( 修改手机号)
    * @param customerInfoPhone
    * @return    设定文件 
    * @return JSONObject    返回类型 
    */
    @ApiOperation(value = "修改手机号", httpMethod = "POST", response = JSONObject.class, notes = "修改手机号")
    @RequestMapping("/updateCustomerInfoPhone")
    @ResponseBody
    public JSONObject updateCustomerInfoPhone(
    		@ApiParam(required =true ,name="customerInfoPhone",value="customerInfoPhone,手机号")String customerInfoPhone
    		){    	
    	JSONObject message=new JSONObject();
    	try {
    		//从session中获取用户id
    		HttpSession session = request.getSession();
    		CustomerRegistVo customerRegistVo =  (CustomerRegistVo) session.getAttribute("loginUser");
    		String customerInfoId = customerRegistVo.getCustomerInfoId();
    		//手机号格式校验
        	String check ="^[1][0-9]{10}$";
    		//修改用户信息
    		CustomerInfo customerInfo = customerInfoService.getCustomerInfo(customerInfoId);
    		if(customerInfo !=null){
    			if(StringUtils.isNotEmpty(customerInfoPhone)){
    				if(!customerInfoPhone.equals(customerInfo.getCustomerInfoPhone())){
	    				if(customerInfoPhone.matches(check)){
	            			customerInfo.setCustomerInfoPhone(customerInfoPhone);
	            			customerInfo.setCustomerInfoEditTime(new Date());
	            			int updateCustomerInfo = customerInfoService.updateCustomerInfo(customerInfo);
	            			if(updateCustomerInfo>0){
	            				message.put("data", updateCustomerInfo);
	            				return InterfaceResultUtil.getReturnMapSuccess(message);      			
	            			}    			
	            		}else{
	            			message.put("data", "手机号格式不正确，请重新输入！");
	            			return InterfaceResultUtil.getReturnMapError(message);
	            		}    				
	    			}
    				else{
    					message.put("data", "新手机号码和旧手机号号码一样，请重新输入！");
            			return InterfaceResultUtil.getReturnMapError(message);
    				}
	    			
    			}  
    			else{
    				message.put("data", "手机号为空，请输入手机号！");
        			return InterfaceResultUtil.getReturnMapError(message);
    			}
    		}    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return InterfaceResultUtil.getReturnMapError(message);
    }
    
    
    /** 
    * @Title: updateCustomerInfoEmail 
    * @Description:  TODO(修改邮箱)
    * @param customerInfoEmail
    * @return    设定文件 
    * @return JSONObject    返回类型 
    */
    @ApiOperation(value = "修改邮箱", httpMethod = "POST", response = JSONObject.class, notes = "修改邮箱")
    @RequestMapping("/updateCustomerInfoEmail")
    @ResponseBody
    public JSONObject updateCustomerInfoEmail(
    		@ApiParam(required =true ,name="customerInfoEmail",value="customerInfoEmail,邮箱")String customerInfoEmail
    		){    	
    	JSONObject message=new JSONObject();
    	try {
    		//从session中获取用户id
    		HttpSession session = request.getSession();
    		CustomerRegistVo customerRegistVo =  (CustomerRegistVo) session.getAttribute("loginUser");
    		String customerInfoId = customerRegistVo.getCustomerInfoId();
    		String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    		//修改用户信息
    		CustomerInfo customerInfo = customerInfoService.getCustomerInfo(customerInfoId);
    		if(customerInfo !=null){    			
    			if(StringUtils.isNotEmpty(customerInfoEmail)){
    				if(!customerInfoEmail.equals(customerInfo.getCustomerInfoEmail())){
    					if(customerInfoEmail.matches(check)){
        					customerInfo.setCustomerInfoEmail(customerInfoEmail);
                    		customerInfo.setCustomerInfoEditTime(new Date());
                    		int updateCustomerInfo = customerInfoService.updateCustomerInfo(customerInfo);
                    		if(updateCustomerInfo>0){
                    			message.put("data", updateCustomerInfo);
                    			return InterfaceResultUtil.getReturnMapSuccess(message);      			
                    		}
        				}
        				else{
        					message.put("data", "邮箱格式不正确，请重新输入！");
                			return InterfaceResultUtil.getReturnMapSuccess(message);  
        				}
    				}
    				else{
    					message.put("data", "新手机号码和旧手机号号码一样，请重新输入！");
            			return InterfaceResultUtil.getReturnMapError(message);    					
    				} 				
      			}    		
    			else{    			  
    				 message.put("data", "邮箱号为空，请重新输入！");
         			 return InterfaceResultUtil.getReturnMapSuccess(message);
    			 }
    	}    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return InterfaceResultUtil.getReturnMapError(message);
    }
    
    /** 
    * @Title: updateCustomerRegistPassword 
    * @Description: 修改密码
    * @param customerRegistPassword
    * @return    设定文件 
    * @return JSONObject    返回类型 
    */
    @ApiOperation(value = "修改密码", httpMethod = "POST", response = JSONObject.class, notes = "修改密码")
    @RequestMapping("/updateCustomerRegistPassword")
    @ResponseBody
    public JSONObject updateCustomerRegistPassword(
    		@ApiParam(required =true ,name="customerRegistPassword",value="customerRegistPassword,新密码")String newCustomerRegistPassword,
    		@ApiParam(required =true ,name="customerRegistPassword",value="customerRegistPassword,旧密码")String oldCustomerRegistPassword
    		){
    	JSONObject message=new JSONObject();
    	try {
    		//从session中获取用户id
    		HttpSession session = request.getSession();
    		CustomerRegistVo customerRegistVo =  (CustomerRegistVo) session.getAttribute("loginUser");
    		String customerInfoId = customerRegistVo.getCustomerInfoId();
    		//修改用户的密码
    		CustomerRegist customerRegist = customerRegistService.getCustomerInfoId(customerInfoId);
    		//新密码加密
    		Md5Hash md5 = new Md5Hash(newCustomerRegistPassword);
			String passWord = md5.toString();
			//旧密码加密
    		Md5Hash md51 = new Md5Hash(oldCustomerRegistPassword);
			String oldPassWord = md51.toString();
			//校验旧密码
			String customerRegistPassword = customerRegist.getCustomerRegistPassword();
			if(oldPassWord.equals(customerRegistPassword)){
	    		if(customerRegist !=null){
	    			if(StringUtils.isNotEmpty(passWord)){
	        			if(passWord!=customerRegistPassword){
	                		customerRegist.setCustomerRegistPassword(passWord);
	                		customerRegist.setCustomerRegistEditTime(new Date());
	                		int updateCustomerRegist = customerRegistService.updateCustomerRegist(customerRegist);
	                		if(updateCustomerRegist>0){
	                			message.put("data", updateCustomerRegist);
	                			return InterfaceResultUtil.getReturnMapSuccess(message);
	                		}
	            		}
	            		else{
	            			message.put("data", "新密码和老密码重复，请重新输入新密码！");
	            			return InterfaceResultUtil.getReturnMapError(message);
	            		}    			
	        		}
	    			else{
	    				message.put("data", "密码为空，请输入密码！");
	        			return InterfaceResultUtil.getReturnMapError(message);
	    			}	    			
	    		}
		  }
			else{
				message.put("data", "当前密码不对，请重新输入密码！");
				return InterfaceResultUtil.getReturnMapError(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return InterfaceResultUtil.getReturnMapError(message);
    }
} 

