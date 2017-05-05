package br.order.user.controller.firstdata;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.wordnik.swagger.annotations.ApiOperation;

import br.crm.pojo.dict.DictImg;
import br.crm.pojo.firstshow.Firstdatashow;
import br.crm.service.dict.DictImgService;
import br.crm.service.firstdata.OrgFirstDataService;
import br.crm.vo.firstdata.FirstdatashowVo;
import br.order.common.utils.InterfaceResultUtil;

/**
 * 用户首页展示Controller
 * 
 * @ClassName: FirstDataController
 * @Description: TODO(用户首页展示Controller)
 * @author adminis
 * @date 2016年10月12日 上午10:53:00
 *
 */
@Controller
@RequestMapping("/userFirstData")
public class UserFirstDataController {

	@Autowired
	private OrgFirstDataService orgFirstDataService;

	/**
	 * 图片服务
	 */
	@Autowired
	private DictImgService dictImgService;

	@ApiOperation(value = "用户首页展示", httpMethod = "GET", response = JSONObject.class, notes = "用户首页展示")
	@RequestMapping("/showUserFirstData")
	@ResponseBody
	public JSONObject showUserFirstData() {

		JSONObject message = new JSONObject();
		try {
			// 查询套餐
			List<FirstdatashowVo> suiteList = orgFirstDataService.selectSuite();
			if (CollectionUtils.isNotEmpty(suiteList)) {
				for (FirstdatashowVo firstdatashowVo : suiteList) {
					// 根据图片的ID查询图片
					if (null != firstdatashowVo.getFirstdatashowDataImageId() && 0 != firstdatashowVo.getFirstdatashowDataImageId()) {
						DictImg img = dictImgService.getEntityById(firstdatashowVo.getFirstdatashowDataImageId());
						if (null != img) {
							firstdatashowVo.setImgURL(img.getImgLocation());
						}
					}
				}
			}

			// 查询门店
			List<FirstdatashowVo> branchList = orgFirstDataService.selectBranch();

			if (CollectionUtils.isNotEmpty(branchList)) {
				for (FirstdatashowVo firstdatashowVo : branchList) {
					// 根据图片的ID查询图片
					if (null != firstdatashowVo.getFirstdatashowDataImageId() && 0 != firstdatashowVo.getFirstdatashowDataImageId()) {
						DictImg img = dictImgService.getEntityById(firstdatashowVo.getFirstdatashowDataImageId());
						if (null != img) {
							firstdatashowVo.setImgURL(img.getImgLocation());
						}
					}
					//查询套餐数量
					int suiteCount=orgFirstDataService.selectSuiteCountByBranch(firstdatashowVo.getFirstdatashowDataId());
					firstdatashowVo.setSuiteCount(suiteCount+"");
				}
			}
			message.put("suite", suiteList);
			message.put("branch", branchList);
			return InterfaceResultUtil.getReturnMapSuccess(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return InterfaceResultUtil.getReturnMapError(message);
	}

}
