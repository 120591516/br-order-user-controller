package br.order.user.controller.empComDept;

import java.util.Date;
import java.util.List;

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
import br.crm.common.utils.JsonUtils;
import br.order.user.pojo.empComDept.EnterpriseDep;
import br.order.user.service.empCom.EnterpriseService;
import br.order.user.service.empComDept.EnterpriseDepService;
import br.order.user.service.empComUser.EnterpriseEmpService;
import br.order.user.vo.empComDept.EnterpriseDepTreeVo;
import br.order.user.vo.empComDept.EnterpriseDepVo;
import br.order.user.vo.empUser.CustomerRegistVo;

/** 
* (企业部门controller)
* @ClassName: EnterpriseDepController 
* @Description: TODO(企业部门controller) 
* @author 王文腾
* @date 2016年9月14日 上午11:49:07 
*/
@Controller
@RequestMapping("/enterpriseDep")
public class EnterpriseDepController {
    /**
     * {企业部门service}
     */
    @Autowired
    private EnterpriseDepService enterpriseDepService;

    /**
     * {企业service}
     */
    @Autowired
    private EnterpriseService enterpriseService;

    /**
     * {企业员工service}
     */
    @Autowired
    private EnterpriseEmpService enterpriseEmpService;

    @Autowired
    private HttpServletRequest request;

    /** 
    * @Title: insertEnterpriseDep 
    * @Description: TODO(添加企业部门信息) 
    * @param enterpriseDep 企业部门信息
    * @return JSONObject    
    * @throws 
    */

    @ApiOperation(value = "添加企业部门信息", httpMethod = "POST", response = JSONObject.class, notes = "添加企业部门信息")
    @RequestMapping(value = "/insertEnterpriseDep", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject insertEnterpriseDep(
            @ApiParam(required = true, name = "enterpriseDep", value = "enterpriseDep,部门信息对象") EnterpriseDep enterpriseDep) {
        JSONObject message = new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
            enterpriseDep.setEnterpriseId(customerRegistVo.getCustomerCompanyId());
            enterpriseDep.setEnterpriseDepEditId(customerRegistVo.getCustomerInfoId());
            enterpriseDep.setEnterpriseDepCreateTime(new Date());
            enterpriseDep.setEnterpriseDepEditTime(enterpriseDep.getEnterpriseDepCreateTime());
            enterpriseDep.setEnterpriseDepStatus(0);
            int i = enterpriseDepService.insertEnterpriseDep(enterpriseDep);
            message.put("data", i);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: updateEnterpriseDep 
    * @Description: TODO(修改企业部门信息) 
    * @param enterpriseDep 企业部门信息
    * @return JSONObject    
    * @throws 
    */

    @ApiOperation(value = "修改企业部门信息", httpMethod = "POST", response = JSONObject.class, notes = "修改企业部门信息")
    @RequestMapping(value = "/updateEnterpriseDep", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject updateEnterpriseDep(
            @ApiParam(required = true, name = "enterpriseDep", value = "enterpriseDep,部门信息对象") EnterpriseDep enterpriseDep) {
        JSONObject message = new JSONObject();
        try {
            if (null!=enterpriseDep.getEnterpriseDepId()) {
                HttpSession session = request.getSession();
                CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
                enterpriseDep.setEnterpriseId(customerRegistVo.getCustomerCompanyId());
                enterpriseDep.setEnterpriseDepEditTime(new Date());
                int i = enterpriseDepService.updateEnterpriseDep(enterpriseDep);
                message.put("data", i);
                return InterfaceResultUtil.getReturnMapSuccess(message);
                
            }else{
                return InterfaceResultUtil.getReturnMapError(message);
            }
        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    /** 
    * @Title: deleteEnterpriseDep 
    * @Description: TODO(删除企业部门信息) 
    * @param enterpriseDepId 企业部门id
    * @return JSONObject    
    * @throws 
    */

    @ApiOperation(value = "删除企业部门信息", httpMethod = "GET", response = JSONObject.class, notes = "删除企业部门信息")
    @RequestMapping("/deleteEnterpriseDep")
    @ResponseBody
    public JSONObject deleteEnterpriseDep(
            @ApiParam(required = true, name = "enterpriseDepId", value = "企业id',enterpriseDepId") String enterpriseDeptId) {
        JSONObject message = new JSONObject();
        try {
            //判断是否有下级部门
            int count = enterpriseDepService.countDeptChildNum(enterpriseDeptId);
            if (count > 0) {
                message.put("data", "该部门尚存在下级部门，暂不能删除");
                return InterfaceResultUtil.getReturnMapError(message);
            }
            else {
                count = enterpriseEmpService.countEmpNumByDeptId(enterpriseDeptId);
                if (count > 0) {
                    message.put("data", "该部门尚存在员工，暂不能删除");
                    return InterfaceResultUtil.getReturnMapError(message);
                }
                else {
                    EnterpriseDep enterpriseDep = new EnterpriseDep();
                    enterpriseDep.setEnterpriseDepId(enterpriseDeptId);
                    enterpriseDep.setEnterpriseDepStatus(1);
                    enterpriseDep.setEnterpriseDepEditTime(new Date());
                    int i = enterpriseDepService.updateEnterpriseDep(enterpriseDep);
                    message.put("data", i);
                    return InterfaceResultUtil.getReturnMapSuccess(message);
                }
            }
        }
        catch (Exception e) {
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    @ApiOperation(value = "获取企业部门树菜单", httpMethod = "GET", response = JSONObject.class, notes = "获取企业部门树菜单")
    @RequestMapping("/getEnterpriseDepTree")
    @ResponseBody
    public JSONObject getEnterpriseDepTree() {
        JSONObject message = new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
            String enterpriseDeptId = customerRegistVo.getCustomerCompanyId();
            List<EnterpriseDepTreeVo> all = enterpriseDepService.getMenuTree(enterpriseDeptId);
            String data = JsonUtils.objectToJson(all);
            message.put("data", data);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    @ApiOperation(value = "获取企业部门列表", httpMethod = "GET", response = JSONObject.class, notes = "获取企业部门列表")
    @RequestMapping("/getEnterpriseDepList")
    @ResponseBody
    public JSONObject getEnterpriseDepList() {
        JSONObject message = new JSONObject();
        try {
            HttpSession session = request.getSession();
            CustomerRegistVo customerRegistVo = (CustomerRegistVo) session.getAttribute("loginUser");
            String enterpriseId = customerRegistVo.getCustomerCompanyId();
            List<EnterpriseDep> list = enterpriseDepService.getDepInEnterprise(enterpriseId);
            message.put("data", list);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }

    @ApiOperation(value = "查看部门信息", httpMethod = "GET", response = JSONObject.class, notes = "查看部门信息")
    @RequestMapping("/getEnterpriseDep")
    @ResponseBody
    public JSONObject getEnterpriseDep(
            @ApiParam(required = true, name = "enterpriseDeptId", value = "enterpriseDeptId,部门id") String enterpriseDeptId) {
        JSONObject message = new JSONObject();
        try {
            EnterpriseDep enterpriseDep = enterpriseDepService.getEnterpriseDepById(enterpriseDeptId);
            message.put("data", enterpriseDep);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }
    @ApiOperation(value = "企业部门重名校验", httpMethod = "GET", response = JSONObject.class, notes = "企业部门重名校验")
    @RequestMapping("/checkDeptName")
    @ResponseBody
    public JSONObject checkDeptName(
            @ApiParam(required = true, name = "enterpriseDeptName", value = "enterpriseDeptName,部门名称") String enterpriseDeptName) {
        JSONObject message = new JSONObject();
        try {
            int count = enterpriseDepService.checkDeptName(enterpriseDeptName);
            message.put("data", count);
            return InterfaceResultUtil.getReturnMapSuccess(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return InterfaceResultUtil.getReturnMapError(message);
    }
}
