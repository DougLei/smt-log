package com.smt.log;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.douglei.tools.web.HttpUtil;
import com.smt.parent.code.filters.log.LogOperation;
import com.smt.parent.code.filters.token.TokenContext;
import com.smt.parent.code.response.Response;

/**
 * 
 * @author DougLei
 */
@RestController
@RequestMapping("/log")
public class LogController {

	@Autowired
	private LogService service;
	
	/**
	 * (不验证token)记录日志
	 * @param operation
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/add_", method=RequestMethod.POST)
	public Response add_(@RequestBody LogOperation operation, HttpServletRequest request) {
		// TODO 后期要去判断是否是合法的调用方来调用该接口
		if(operation.getUserId() == null)
			operation.setUserId("unknow");
		return service.add(operation);
	}
	
	/**
	 * 记录日志
	 * @param operation
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/add", method=RequestMethod.POST)
	public Response add(@RequestBody LogOperation operation, HttpServletRequest request) {
		operation.setUserId(TokenContext.get().getUserId());
		operation.setOperDate(new Date());
		operation.setClientIp(HttpUtil.getClientIp(request));
		
		return service.add(operation);
	}
}