package com.smt.log;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.smt.parent.code.filters.log.LogOperation;
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
	 * 记录日志
	 * @param operation
	 * @return
	 */
	@RequestMapping(value="/add", method=RequestMethod.POST)
	public Response add(@RequestBody LogOperation operation) {
		if(operation.getOperDate() == null)
			operation.setOperDate(new Date());
		
		service.add(operation);
		return new Response(operation);
	}
	
	/**
	 * 记录日志(不验证token)
	 * @param operation
	 * @return
	 */
	@RequestMapping(value="/add_", method=RequestMethod.POST)
	public Response addBuiltIn(@RequestBody LogOperation operation) {
		return add(operation);
	}
}