package com.smt.log.operation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.douglei.tools.web.HttpUtil;
import com.smt.log.tables.LogTables;
import com.smt.log.tables.LogTablesService;
import com.smt.parent.code.filters.log.LogOperation;
import com.smt.parent.code.filters.token.TokenContext;
import com.smt.parent.code.filters.token.TokenEntity;
import com.smt.parent.code.query.QueryExecutor;
import com.smt.parent.code.response.Response;
import com.smt.parent.code.spring.web.LoggingResponse;

/**
 * 
 * @author DougLei
 */
@RestController
@RequestMapping("/log")
public class LogController {

	@Autowired
	private LogService service;
	
	@Autowired
	private LogTablesService logTablesService;
	
	@Autowired
	private QueryExecutor queryExecutor;
	
	/**
	 * (不验证token)记录日志
	 * @param operation
	 * @return
	 */
	@RequestMapping(value="/add_", method=RequestMethod.POST)
	public Response add_(@RequestBody LogOperation operation) {
		// TODO 后期要去判断是否是合法的调用方来调用该接口
		if(operation.getProjectCode() == null)
			return new Response(null, null, "记录日志时未提供项目编码", "smt.log.add.fail.projectcode.not.provided");
		if(operation.getTenantId() == null)
			return new Response(null, null, "记录日志时未提供租户id", "smt.log.add.fail.tenantid.not.provided");
		
		LogTables logTables = logTablesService.getByCode(operation.getProjectCode(), operation.getTenantId());
		if(logTables== null || logTables.getIsDeleted() !=0)
			return new Response(null, null, "记录日志失败, 编码为[%s]的项目不存在日志表", "smt.log.add.fail.logtables.unexists");
		
		return service.add(logTables.getId(), operation);
	}
	
	/**
	 * 记录日志
	 * @param operation
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/add", method=RequestMethod.POST)
	public Response add(@RequestBody LogOperation operation, HttpServletRequest request) {
		TokenEntity token = TokenContext.get();
		if(token.getProjectCode() == null)
			return new Response(null, null, "记录日志时未提供项目编码", "smt.log.add.fail.projectcode.not.provided");
		
		LogTables logTables = logTablesService.getByCode(token.getProjectCode(), token.getTenantId());
		if(logTables== null || logTables.getIsDeleted() !=0)
			return new Response(null, null, "记录日志失败, 编码为[%s]的项目不存在日志表", "smt.log.add.fail.logtables.unexists");
		
		operation.setUserId(token.getUserId());
		operation.setOperDate(token.getCurrentDate());
		operation.setClientIp(HttpUtil.getClientIp(request));
		return service.add(logTables.getId(), operation);
	}
	
	/**
	 * 查询日志
	 * @param request
	 * @return
	 */
	@LoggingResponse(loggingBody=false)
	@RequestMapping(value="/query", method=RequestMethod.GET)
	public Response query(HttpServletRequest request) {
		TokenEntity token = TokenContext.get();
		if(token.getProjectCode() == null)
			return new Response(null, null, "查询日志时未提供项目编码", "smt.log.query.fail.projectcode.not.provided");
		
		LogTables logTables = logTablesService.getByCode(token.getProjectCode(), token.getTenantId());
		if(logTables== null || logTables.getIsDeleted() !=0)
			return new Response(null, null, "查询日志失败, 编码为[%s]的项目不存在日志表", "smt.log.query.fail.logtables.unexists");
		
		return service.query(logTables.getId(), queryExecutor.parse(request));
	}
	
	/**
	 * 查询日志请求明细
	 * @param operationId
	 * @return
	 */
	@LoggingResponse(loggingBody=false)
	@RequestMapping(value="/detail/query/{operationId}", method=RequestMethod.GET)
	public Response queryDetail(@PathVariable int operationId) {
		TokenEntity token = TokenContext.get();
		if(token.getProjectCode() == null)
			return new Response(null, null, "查询日志请求明细时未提供项目编码", "smt.log.detail.query.fail.projectcode.not.provided");
		
		LogTables logTables = logTablesService.getByCode(token.getProjectCode(), token.getTenantId());
		if(logTables== null || logTables.getIsDeleted() !=0)
			return new Response(null, null, "查询日志失败, 编码为[%s]的项目不存在日志表", "smt.log.query.fail.logtables.unexists");
		
		return service.queryDetail(operationId, logTables.getId());
	}
}