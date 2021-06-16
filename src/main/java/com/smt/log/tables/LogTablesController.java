package com.smt.log.tables;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.smt.parent.code.response.Response;
import com.smt.parent.code.spring.eureka.cloud.feign.APIGeneralResponse;
import com.smt.parent.code.spring.eureka.cloud.feign.APIGeneralServer;
import com.smt.parent.code.spring.eureka.cloud.feign.RestTemplateWrapper;
import com.smt.parent.code.spring.web.LoggingResponse;

/**
 * 
 * @author DougLei
 */
@RestController
@RequestMapping("/log/tables")
public class LogTablesController {
	
	@Autowired
	private RestTemplateWrapper restTemplate;
	
	@Autowired
	private LogTablesService service;
	
	// 验证project是否存在
	private boolean projectExists(String projectCode) {
		return restTemplate.generalExchange(new APIGeneralServer() {
			@Override
			public String getName() {
				return "(同步)验证是否存在指定编码的项目";
			}
			@Override
			public String getUrl() {
				return "http://smt-base/smt-base/project/query?$mode$=UNIQUE_QUERY&code="+projectCode;
			}
		}, null, APIGeneralResponse.class) != null;
	}
	
	/**
	 * 添加日志表信息
	 * @param projectCode
	 * @return
	 */
	@LoggingResponse
	@RequestMapping(value="/insert/{projectCode}", method=RequestMethod.POST)
	public Response insert(@PathVariable String projectCode) {
		if(!projectExists(projectCode))
			return new Response(projectCode, null, "添加日志表信息失败, 不存在编码为[%s]的项目", "smt.log.insert.fail.project.unexists", projectCode);
		return service.insert(projectCode);
	}
	
	/**
	 * 删除日志表信息
	 * @param projectCode
	 * @param request
	 * @return
	 */
	@LoggingResponse
	@RequestMapping(value="/delete/{projectCode}", method=RequestMethod.POST)
	public Response delete(@PathVariable String projectCode, HttpServletRequest request) {
		if(!projectExists(projectCode))
			return new Response(projectCode, null, "删除日志表信息失败, 不存在编码为[%s]的项目", "smt.log.delete.fail.project.unexists", projectCode);
		return service.delete(projectCode, "true".equalsIgnoreCase(request.getParameter("truncate")));
	}
	
	/**
	 * 物理删除日志表信息
	 * @param projectCode
	 * @return
	 */
	@LoggingResponse
	@RequestMapping(value="/drop/{projectCode}", method=RequestMethod.POST)
	public Response drop(@PathVariable String projectCode) {
		if(!projectExists(projectCode))
			return new Response(projectCode, null, "物理删除日志表信息失败, 不存在编码为[%s]的项目", "smt.log.drop.fail.project.unexists", projectCode);
		return service.drop(projectCode);
	}
	
	/**
	 * 清空日志表
	 * @param projectCode
	 * @return
	 */
	@LoggingResponse
	@RequestMapping(value="/truncate/{projectCode}", method=RequestMethod.POST)
	public Response truncate(@PathVariable String projectCode) {
		if(!projectExists(projectCode))
			return new Response(projectCode, null, "清空日志表失败, 不存在编码为[%s]的项目", "smt.log.truncate.fail.project.unexists", projectCode);
		return service.truncate(projectCode);
	}
}