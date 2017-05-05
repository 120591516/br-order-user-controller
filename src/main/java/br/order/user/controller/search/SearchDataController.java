package br.order.user.controller.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import br.crm.service.branch.OrgBranchService;
import br.crm.service.suite.OrgExamSuiteService;
import br.crm.vo.branch.OrganizationBranchVo;
import br.crm.vo.suite.OrgExamSuiteQu;
import br.crm.vo.suite.OrgExamSuiteVo;
import br.order.common.utils.InterfaceResultUtil;

/**
 * 
 * @ClassName: SearchDataController
 * @Description: TODO(搜索服务Controller)
 * @author adminis
 * @date 2016年10月17日 下午4:41:33
 *
 */
@Controller
@RequestMapping("/searchData")
public class SearchDataController {

	@Autowired
	private OrgExamSuiteService orgExamSuiteService;

	@Autowired
	private OrgBranchService orgBranchService;

	@ApiOperation(value = "用户预约首页搜索套餐", httpMethod = "POST", response = JSONObject.class, notes = "用户预约首页搜索套餐")
	@RequestMapping("/searchSuite")
	@ResponseBody
	public JSONObject searchSuite(@ApiParam(required = true, name = "page", value = "page,当前页") @RequestParam(value = "page", defaultValue = "1", required = true) Integer page, @ApiParam(required = true, name = "rows", value = "rows,每页显示条数") @RequestParam(value = "rows", defaultValue = "10", required = true) Integer rows, @ApiParam(required = true, name = "orgExamSuiteQu", value = "orgExamSuiteQu,查询条件") OrgExamSuiteQu orgExamSuiteQu) {
		JSONObject message = new JSONObject();
		try {
			PageInfo<OrgExamSuiteVo> suiteList = orgExamSuiteService.searchSuite(page, rows, orgExamSuiteQu);
			message.put("data", suiteList);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

	@ApiOperation(value = "用户预约首页搜索门店", httpMethod = "POST", response = JSONObject.class, notes = "用户预约首页搜索门店")
	@RequestMapping("/searchBranch")
	@ResponseBody
	public JSONObject searchBranch(
			@ApiParam(required = true, name = "page", value = "page,当前页") @RequestParam(value = "page", defaultValue = "1", required = true) Integer page,
			@ApiParam(required = true, name = "rows", value = "rows,每页显示条数") @RequestParam(value = "rows", defaultValue = "10", required = true) Integer rows,
			@ApiParam(required = true, name = "organizationBranchVo", value = "organizationBranchVo,查询条件") OrganizationBranchVo organizationBranchVo) {
		JSONObject message = new JSONObject();
		try {
			PageInfo<OrganizationBranchVo> branchList = orgBranchService.searchBranch(page, rows, organizationBranchVo);
			message.put("data", branchList);
			return InterfaceResultUtil.getReturnMapSuccess(message);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}
}
