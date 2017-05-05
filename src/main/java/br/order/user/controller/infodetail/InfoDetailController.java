package br.order.user.controller.infodetail;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.service.InfoDetail.InfoDetailService;
import br.crm.service.suite.OrgBranchSuiteService;
import br.crm.vo.branch.OrganizationBranchVo;
import br.crm.vo.suite.OrgExamSuiteVo;
import br.order.common.utils.InterfaceResultUtil;

/**
 * 
 * @ClassName: InfoDetailController
 * @Description: TODO(商品详情Controller)
 * @author adminis
 * @date 2016年10月24日 上午11:18:37
 *
 */
@Controller
@RequestMapping("/infodetail")
public class InfoDetailController {

	@Autowired
	private InfoDetailService infoDetailService;
	@Autowired
	private OrgBranchSuiteService orgBranchSuiteService;
	
	/**
	 * 
	 * @Title: showSuiteInfoDetail
	 * @Description: TODO(查询套餐详情)
	 * @param @param suiteId   套餐ID
	 * @param @return    设定文件
	 * @return JSONObject    返回类型
	 * @throws
	 */
	@ApiOperation(value = "套餐详情", httpMethod = "GET", response = JSONObject.class, notes = "套餐详情")
	@RequestMapping("/showSuiteInfoDetail")
	@ResponseBody
	public JSONObject showSuiteInfoDetail(@ApiParam(required = true, name = "suiteId", value = "suiteId,套餐id") String suiteId) {
		JSONObject message = new JSONObject();
		if (StringUtils.isEmpty(suiteId)) {
			message.put("data", "参数错误");
			return InterfaceResultUtil.getReturnMapError(message);
		}
		try {

			OrgExamSuiteVo orgExamSuiteVo = infoDetailService.showSuiteInfoDetail(suiteId);
			message.put("data", orgExamSuiteVo);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return InterfaceResultUtil.getReturnMapError(message);
	}	
	
	
	/**
	 * 
	 * @Title: showBranchInfoDetail
	 * @Description: TODO(查询门店详情)
	 * @param @param branchId   门店ID
	 * @param @return    设定文件
	 * @return JSONObject    返回类型
	 * @throws
	 */
	@ApiOperation(value = "门店详情", httpMethod = "GET", response = JSONObject.class, notes = "门店详情")
	@RequestMapping("/showBranchInfoDetail")
	@ResponseBody
	public JSONObject showBranchInfoDetail(@ApiParam(required = true, name = "branchId", value = "branchId,门店id") String branchId) {

		JSONObject message = new JSONObject();
		if (StringUtils.isEmpty(branchId)) {
			message.put("data", "参数错误");
			return InterfaceResultUtil.getReturnMapError(message);
		}
		try {
			OrganizationBranchVo organizationBranchVo = infoDetailService.showBranchInfoDetail(branchId);
			message.put("data", organizationBranchVo);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return InterfaceResultUtil.getReturnMapError(message);
	}
    /** 
   * @Title: getOrgBranchBySuiteId 
   * @Description: TODO() 
   * @param examSuiteId
   * @return JSONObject    
   * @throws 
   */
   
   @ApiOperation(value="根据套餐id查看门店信息",httpMethod="GET",notes="根据套餐id查看门店信息")
       @RequestMapping("/getOrgBranchBySuiteId")
       @ResponseBody
       public JSONObject getOrgBranchBySuiteId(@ApiParam(required=true,value="套餐id",name="examSuiteId")String examSuiteId){
            JSONObject message=new JSONObject();
            try {
                List<OrganizationBranchVo> list=orgBranchSuiteService.getOrgBranchBySuiteId(examSuiteId);
               message.put("data", list);
               return InterfaceResultUtil.getReturnMapSuccess(message);
           } catch (Exception e) {
               // TODO: handle exception
               e.printStackTrace();
           }
            return InterfaceResultUtil.getReturnMapError(message);
       }
}
