package com.smt.log.tables;

import java.util.Arrays;

import com.douglei.orm.context.PropagationBehavior;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.mapping.handler.entity.DeleteMappingEntity;
import com.smt.log.SmtLogException;
import com.smt.parent.code.filters.token.TokenContext;
import com.smt.parent.code.filters.token.TokenEntity;
import com.smt.parent.code.response.Response;

/**
 * 
 * @author DougLei
 */
@TransactionComponent
public class LogTablesService {

	/**
	 * 获取指定项目编码的日志表信息
	 * @param projectCode
	 * @param tenantId
	 * @return
	 */
	@Transaction(propagationBehavior=PropagationBehavior.SUPPORTS)
	public LogTables getByCode(String projectCode, String tenantId) {
		return SessionContext.getSqlSession().uniqueQuery(
				LogTables.class, "select * from log_tables where project_code=? and tenant_id=?", Arrays.asList(projectCode, tenantId));
	}
	
	/**
	 * 添加日志表信息
	 * @param projectCode
	 * @return
	 */
	@Transaction
	public Response insert(String projectCode) {
		TokenEntity token =TokenContext.get();
		LogTables logTables = getByCode(projectCode, token.getTenantId());
		if(logTables == null) {
			SessionContext.getTableSession().save(new LogTables(token.getUserId(), token.getCurrentDate(), projectCode, token.getTenantId()));
		}else if(logTables.getIsDeleted() != 0) {
			logTables.setIsDeleted(0);
			SessionContext.getTableSession().update(logTables);
		}else {
			throw new SmtLogException("添加失败, 编码为["+projectCode+"]的项目已存在日志表信息");
		}
		return new Response(projectCode);
	}

	/**
	 * 删除日志表信息
	 * @param projectCode
	 * @param truncate 是否清空日志表
	 * @return
	 */
	@Transaction
	public Response delete(String projectCode, boolean truncate) {
		LogTables logTables = getByCode(projectCode, TokenContext.get().getTenantId());
		if(logTables== null || logTables.getIsDeleted() !=0)
			throw new SmtLogException("删除失败, 编码为["+projectCode+"]的项目不存在日志表信息");
		
		// 修改日志表信息为删除状态
		logTables.setIsDeleted(1);
		SessionContext.getTableSession().update(logTables);
		
		// 将相关的日志表从MappingContainer中移除
		SessionContext.getSessionFactory().getMappingHandler().execute(
				new DeleteMappingEntity("LOG_OPERATION_"+logTables.getId(), false), new DeleteMappingEntity("LOG_REQUEST_"+logTables.getId(), false));
		
		// 清空日志表
		if(truncate) 
			truncate_(logTables);
		return new Response(projectCode);
	}

	/**
	 * 物理删除日志表信息
	 * @param projectCode
	 * @return
	 */
	@Transaction
	public Response drop(String projectCode) {
		LogTables logTables = getByCode(projectCode, TokenContext.get().getTenantId());
		if(logTables== null)
			throw new SmtLogException("物理删除失败, 编码为["+projectCode+"]的项目不存在日志表信息");
		if(logTables.getIsDeleted() == 0)
			throw new SmtLogException("物理删除失败, 编码为["+projectCode+"]的项目关联的日志表信息处于正常状态");
			
		SessionContext.getSqlSession().executeUpdate("delete log_tables where id=?", Arrays.asList(logTables.getId()));
		SessionContext.getSqlSession().executeUpdate("drop table LOG_OPERATION_"+ logTables.getId());
		SessionContext.getSqlSession().executeUpdate("drop table LOG_REQUEST_"+ logTables.getId());
		return new Response(projectCode);
	}

	/**
	 * 清空日志表
	 * @param projectCode
	 * @return
	 */
	@Transaction
	public Response truncate(String projectCode) {
		LogTables logTables = getByCode(projectCode, TokenContext.get().getTenantId());
		if(logTables== null)
			throw new SmtLogException("清空日志表失败, 编码为["+projectCode+"]的项目不存在日志表信息");
		
		truncate_(logTables);
		return new Response(projectCode);
	}
	// 清空日志表
	private void truncate_(LogTables logTables) {
		SessionContext.getSqlSession().executeUpdate("truncate table LOG_OPERATION_"+ logTables.getId());
		SessionContext.getSqlSession().executeUpdate("truncate table LOG_REQUEST_"+ logTables.getId());
	}
}