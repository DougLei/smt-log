package com.smt.log;

import java.util.Arrays;

import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.smt.parent.code.filters.log.LogOperation;
import com.smt.parent.code.filters.log.LogRequest;

/**
 * 
 * @author DougLei
 */
@TransactionComponent
public class LogService {
	
	/**
	 * 记录日志
	 * @param operation
	 * @return 
	 */
	@Transaction
	public void add(LogOperation operation) {
		Object[] old = operation.getBatch()==null?null:SessionContext.getSqlSession().uniqueQuery_("select id from log_operation where batch=?", Arrays.asList(operation.getBatch()));
		if(old == null) {
			SessionContext.getTableSession().save(operation);
		} else {
			operation.setId(Integer.parseInt(old[0].toString()));
		}
		
		LogRequest request = operation.getLogRequest();
		if(request != null) {
			request.setOperationId(operation.getId());
			if(old != null)
				request.setOrder(Integer.parseInt(SessionContext.getSqlSession().uniqueQuery_("select count(id) from log_request where operation_id=?", Arrays.asList(operation.getId()))[0].toString()));
			SessionContext.getTableSession().save(request);
		}
	}
}
