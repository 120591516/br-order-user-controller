package br.order.user.controller.empCom;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.common.utils.InterfaceResultUtil;
import br.order.user.pojo.empCom.Enterprise;
import br.order.user.pojo.empComUser.EnterpriseEmp;
import br.order.user.pojo.empUser.CustomerInfo;
import br.order.user.pojo.empUser.CustomerRegist;
import br.order.user.service.empCom.EnterpriseService;
import br.order.user.service.empComUser.EnterpriseEmpService;
import br.order.user.service.empUser.CustomerInfoService;
import br.order.user.service.empUser.CustomerRegistService;


/**
 * @ClassName: EnterpriseController
 * @Description: TODO
 * @author 杨春阳
 * @date 2016年11月17日 上午10:10:02
 */
@Controller
@RequestMapping("/enterprise")
public class EnterpriseController {
    /**
     * {企业信息service}
     */
    @Autowired
    private EnterpriseService enterpriseService;
    @Autowired
    private HttpServletRequest request;
    /**
     * {企业员工service}
     */
    @Autowired
    private EnterpriseEmpService enterpriseEmpService;
    @Autowired
    private CustomerInfoService customerInfoService;
    @Autowired
    private CustomerRegistService customerRegistService;
    
    /** 
    * @Title: insertEnterprise 
    * @Description: TODO(注册企业信息) 
    * @param enterprise 企业信息
    * @return JSONObject    
    * @throws 
    */
    
    @ApiOperation(value = "注册企业信息", httpMethod = "POST", response = JSONObject.class, notes = "注册企业信息")
    @RequestMapping(value = "/insertEnterprise", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject insertEnterprise(
    		@ApiParam(required=true ,name="enterprise",value="enterprise,企业信息对象") Enterprise enterprise,
    		@ApiParam(required = true, name = "customerRegist", value = "customerRegist,客户注册对象") CustomerRegist customerRegist,
    		@ApiParam(required = true, name = "customerInfo", value = "customerInfo,客户信息对象") CustomerInfo customerInfo
    		) {
        JSONObject message = new JSONObject();
        try {
           //添加用户信息
        	String customerInfoId = customerInfoService.insertCustomerInfo(customerInfo);
        	if(customerInfoId.length()>0){
        		//注册企业信息
        		enterprise.setEnterpriceConnEmpId(customerInfoId);
        		String enterpriseId = enterpriseService.insertEnterprise(enterprise);
        		customerRegist.setCustomerInfoId(customerInfoId);
        		customerRegist.setCustomerCompany(1);
        		customerRegist.setCustomerCompanyId(enterpriseId);
        		customerRegist.setCustomerRegistEmail(1);
        		customerRegist.setCustomerRegistSms(1);
        		customerRegistService.insertCustomerRegist(customerRegist);
        		return InterfaceResultUtil.getReturnMapSuccess(null);
        	}
        	else{
        		message.put("message", "注册失败！");
        	}
        }
        catch (Exception e) {
        	e.printStackTrace();
        	message.put("message", "注册失败！");
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }
    /** 
    * @Title: updateEnterprise 
    * @Description: TODO(修改企业信息) 
    * @param enterprise 企业信息对象
    * @return JSONObject    
    * @throws 
    */
    
    @ApiOperation(value = "修改企业信息", httpMethod = "POST", response = JSONObject.class, notes = "修改企业信息")
    @RequestMapping(value = "/updateEnterprise", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject updateEnterprise(@ApiParam(required=true ,name="enterprise",value="enterprise,企业信息对象") Enterprise enterprise) {
        JSONObject message = new JSONObject();
        try {
            enterprise.setEnterpriseEditTime(new Date());
            int i=enterpriseService.updateEnterprise(enterprise);
            message.put("data", i);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }
    /** 
    * @Title: getEnterpriseById 
    * @Description: TODO(根据登录注册用户获取用户下的企业信息) 
    * @param enterpriseEditId 
    * @return JSONObject    
    * @throws 
    */
    
    @ApiOperation(value = "根据注册用户id查询企业信息", httpMethod = "POST", response = JSONObject.class, notes = "根据注册用户id查询企业信息")
    @RequestMapping(value = "/getEnterpriseById", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject getEnterpriseById(@ApiParam(required=true ,name="enterpriseId",value="enterpriseId,注册用户id")Long customerInfoId){
        JSONObject message =new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerInfo customerInfo = (CustomerInfo) session.getAttribute("customerInfo");
            customerInfo=new CustomerInfo();//测试使用
            customerInfo.setCustomerInfoId("1");//测试使用
            EnterpriseEmp enterpriseEmp=enterpriseEmpService.getEnterpriseEmpByInfoId(customerInfo.getCustomerInfoId());
            Enterprise enterprise=enterpriseService.getEnterpriseById(enterpriseEmp.getEnterpriseId());
            message.put("data", enterprise);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }
    
    /** 
    * @Title: deleteEnterprise 
    * @Description: TODO(删除企业信息 ) 
    * @param enterpriseId 企业信息id
    * @return JSONObject    
    * @throws 
    */
    
    @ApiOperation(value = "删除企业信息", httpMethod = "GET", response = JSONObject.class, notes = "删除企业信息")
    @RequestMapping(value = "/deleteEnterprise")
    @ResponseBody
    public JSONObject deleteEnterprise(@ApiParam(required=true ,name="enterpriseId",value="enterpriseId,企业信息id")String enterpriseId){
        JSONObject message =new JSONObject();
      
        try {
            Enterprise enterprise = new Enterprise();
            enterprise.setEnterpriseId(enterpriseId);
            enterprise.setEnterpriseStatus(1);
            enterprise.setEnterpriseEditTime(new Date());
            int i = enterpriseService.updateEnterprise(enterprise);
            if (i>0) {
                i=enterpriseEmpService.updataEnterpriseEmpByExample(enterprise);
                message.put("data", i);
                return InterfaceResultUtil.getReturnMapSuccess(message);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }
}
