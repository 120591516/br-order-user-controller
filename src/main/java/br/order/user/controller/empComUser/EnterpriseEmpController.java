package br.order.user.controller.empComUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
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
import br.order.user.pojo.empCom.Enterprise;
import br.order.user.pojo.empComDept.EnterpriseDep;
import br.order.user.pojo.empComUser.EnterpriseEmp;
import br.order.user.pojo.empUser.CustomerInfo;
import br.order.user.service.empCom.EnterpriseService;
import br.order.user.service.empComDept.EnterpriseDepService;
import br.order.user.service.empComUser.EnterpriseEmpService;
import br.order.user.service.empUser.CustomerInfoService;
import br.order.user.vo.empComUser.EnterpriseEmpVo;
import br.order.user.vo.empUser.CustomerRegistVo;

/** 
* (企业员工controller) 
* @ClassName: EnterpriseEmpController 
* @Description: (企业员工controller) 
* @author 王文腾
* @date 2016年9月14日 上午11:52:32 
*/
@Controller
@RequestMapping("/enterpriseEmp")
public class EnterpriseEmpController {
    /**
     * {企业员工service}
     */
    @Autowired
    private EnterpriseEmpService enterpriseEmpService;

    /**
     * {企业service}
     */
    @Autowired
    private HttpServletRequest request;

    /**
     * {客户信息service}
     */
    @Autowired
    private CustomerInfoService customerInfoService;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private EnterpriseDepService enterpriseDepService;

    /** 
    * @Title: insertEnterpriseEmp 
    * @Description: (添加部门员工) 
    * @param customerInfo 客户信息对象
    * @param enterpriseDepId 企业部门id
    * @return JSONObject    
    * @throws 
    */ 

    @ApiOperation(value = "添加部门员工", httpMethod = "POST", response = JSONObject.class, notes = "添加部门员工")
    @RequestMapping(value = "/insertEnterpriseEmp", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject insertEnterpriseEmp(
            @ApiParam(required = true, name = "customerInfo", value = "customerInfo,个人信息对象") CustomerInfo customerInfo,
            @ApiParam(required = true, name = "enterpriseDepId", value = "部门id,enterpriseDepId") String enterpriseDepId) {

        JSONObject message = new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
            customerInfo.setCustomerInfoEditId(customerRegistVo.getCustomerInfoId());
            String customerInfoId = customerInfoService.insertCustomerInfo(customerInfo);
            if (customerInfoId.length() > 0) {
                //根据获取到的关系信息获取企业信息
                EnterpriseEmp enterpriseEmp = new EnterpriseEmp();
                enterpriseEmp.setCustomerInfoId(customerInfoId);
                enterpriseEmp.setEnterpriseDepId(enterpriseDepId);
                enterpriseEmp.setEnterpriseId(customerRegistVo.getCustomerCompanyId());
                int i = enterpriseEmpService.insertEnterpriseEmp(enterpriseEmp);
                message.put("data", i);
                return InterfaceResultUtil.getReturnMapSuccess(message);

            }

        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: getAllEnterpriseEmpByPage 
    * @Description:(分页获取企业员工信息) 
    * @param page 页数
    * @param rows 行数
    * @param enterpriseId 企业id
    * @return JSONObject    
    * @throws 
    */

    @ApiOperation(value = "分页获取部门员工信息", httpMethod = "get", response = JSONObject.class, notes = "分页获取企业员工信息")
    @RequestMapping("/getAllEnterpriseEmpByPage")
    @ResponseBody
    public JSONObject getAllEnterpriseEmpByPage(
            @ApiParam(required = true, name = "page", value = "page,当前页") @RequestParam(value = "page", defaultValue = "1", required = true) Integer page,
            @ApiParam(required = true, name = "rows", value = "rows,每页显示条数") @RequestParam(value = "rows", defaultValue = "10", required = true) Integer rows,
            @ApiParam(required = true, name = "enterpriseDeptId", value = "enterpriseDeptId,部门id") String enterpriseDeptId) {
        JSONObject message = new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
            if (customerRegistVo != null) {
                PageInfo<EnterpriseEmpVo> pageInfo = enterpriseEmpService.getAllEnterpriseEmpByPage(page, rows,
                        enterpriseDeptId);
                message.put("data", pageInfo);
                return InterfaceResultUtil.getReturnMapSuccess(message);
            }
            else {
                message.put("data", "请登录之后进行查询");
                return InterfaceResultUtil.getReturnMapError(message);

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: getEnterpriseEmp 
    * @Description:根据员工主键查询员工信息
    * @param enterpriseEmpId
    * @return JSONObject    
    * @throws 
    */

    @ApiOperation(value = "根据员工主键查询员工信息", httpMethod = "get", response = JSONObject.class, notes = "根据员工主键查询员工信息")
    @RequestMapping("/getEnterpriseEmp")
    @ResponseBody
    public JSONObject getEnterpriseEmp(
            @ApiParam(required = true, name = "enterpriseEmpId", value = "enterpriseEmpId,员工id") String enterpriseEmpId) {
        JSONObject message = new JSONObject();
        try {
            EnterpriseEmp enterpriseEmp = enterpriseEmpService.getEmterPriseEmpById(enterpriseEmpId);
            Enterprise enterprise = enterpriseService.getEnterpriseById(enterpriseEmp.getEnterpriseId());
            EnterpriseDep enterpriseDep = enterpriseDepService.getEnterpriseDepById(enterpriseEmp.getEnterpriseDepId());
            CustomerInfo customerInfo = customerInfoService
                    .getCustomerInfoByPrimaryKey(enterpriseEmp.getCustomerInfoId());
            EnterpriseEmpVo enterpriseEmpVo = new EnterpriseEmpVo();
            BeanUtils.copyProperties(enterpriseEmpVo, enterpriseEmp);
            BeanUtils.copyProperties(enterpriseEmpVo, customerInfo);
            BeanUtils.copyProperties(enterpriseEmpVo, enterprise);
            BeanUtils.copyProperties(enterpriseEmpVo, enterpriseDep);
            message.put("data", enterpriseEmpVo);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: updateEnterpriseEmp 
    * @Description: (修改企业部门员工信息) 
    * @param customerInfo
    * @param enterpriseDepId
    * @return JSONObject    
    * @throws 
    */

    @ApiOperation(value = "修改部门员工", httpMethod = "POST", response = JSONObject.class, notes = "修改部门员工")
    @RequestMapping(value = "/updateEnterpriseEmp", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject updateEnterpriseEmp(
            @ApiParam(required = true, name = "enterpriseEmp", value = "员工对象,enterpriseEmp") EnterpriseEmp enterpriseEmp) {

        JSONObject message = new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
            int i = 0;
            //修改员工基本信息
            if (enterpriseEmp != null) {
                enterpriseEmp.setEnterpriseId(customerRegistVo.getCustomerCompanyId());
                enterpriseEmp.setStatus(0);
                i = enterpriseEmpService.updateEnterpriseEmp(enterpriseEmp);
            }
            message.put("data", i);
            return InterfaceResultUtil.getReturnMapSuccess(message);

        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    @ApiOperation(value = "删除部门员工", httpMethod = "GET", response = JSONObject.class, notes = "删除部门员工")
    @RequestMapping(value = "/deleteEnterpriseEmp")
    @ResponseBody
    public JSONObject deleteEnterpriseEmp(
            @ApiParam(required = true, name = "enterpriseEmpId", value = "企业员工id,enterpriseEmpId") String enterpriseEmpId) {

        JSONObject message = new JSONObject();
        try {
            EnterpriseEmp enterpriseEmp = enterpriseEmpService.getEmterPriseEmpById(enterpriseEmpId);
            enterpriseEmp.setEnterpriseEmpId(enterpriseEmpId);
            enterpriseEmp.setStatus(1);
            int i = enterpriseEmpService.updateEnterpriseEmp(enterpriseEmp);
            if (i > 0) {
                CustomerInfo customerInfo = new CustomerInfo();
                customerInfo.setCustomerInfoId(enterpriseEmp.getCustomerInfoId());
                i = customerInfoService.updateCustomerInfo(customerInfo);
                message.put("data", i);
                return InterfaceResultUtil.getReturnMapSuccess(message);
            }

        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

}
