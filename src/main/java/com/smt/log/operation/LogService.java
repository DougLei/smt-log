package com.smt.log.operation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.douglei.orm.context.PropagationBehavior;
import com.douglei.orm.context.SessionContext;
import com.douglei.orm.context.Transaction;
import com.douglei.orm.context.TransactionComponent;
import com.douglei.orm.mapping.MappingTypeNameConstants;
import com.douglei.orm.mapping.handler.MappingHandler;
import com.douglei.orm.mapping.handler.entity.AddOrCoverMappingEntity;
import com.douglei.tools.file.reader.FileBufferedReader;
import com.smt.parent.code.filters.log.LogOperation;
import com.smt.parent.code.filters.log.LogRequest;
import com.smt.parent.code.query.QueryCriteriaEntity;
import com.smt.parent.code.query.mode.impl.UniqueQueryMode;
import com.smt.parent.code.response.Response;
import com.smt.parent.code.spring.eureka.cloud.feign.APIGeneralResponse;
import com.smt.parent.code.spring.eureka.cloud.feign.APIGeneralServer;
import com.smt.parent.code.spring.eureka.cloud.feign.RestTemplateWrapper;

/**
 * 
 * @author DougLei
 */
@TransactionComponent
public class LogService {
	private String xml4LogOperation;
	private String xml4LogRequest;
	public LogService() {
		try(FileBufferedReader reader = new FileBufferedReader("mappings/table/LOG_OPERATION.tmp.xml.template")){
			this.xml4LogOperation = reader.readAll(1214);
		}
		try(FileBufferedReader reader = new FileBufferedReader("mappings/table/LOG_REQUEST.tmp.xml.template")){
			this.xml4LogRequest = reader.readAll(1125);
		}
	}
	
	@Autowired
	private RestTemplateWrapper restTemplate;
	
	/**
	 * 记录日志
	 * @param tableId
	 * @param operation
	 * @return 
	 */
	@Transaction
	public Response add(int tableId, LogOperation operation) {
		// 先判断MappingContainer中是否有相应的日志表Mapping实例, 若没有则创建
		boolean isInitial= false;
		MappingHandler mappingHandler = SessionContext.getSessionFactory().getMappingHandler();
		if(isInitial= !mappingHandler.exists("LOG_OPERATION_"+tableId)) 
			mappingHandler.execute(
					new AddOrCoverMappingEntity(String.format(xml4LogOperation, tableId), MappingTypeNameConstants.TABLE).enableProperty(),
					new AddOrCoverMappingEntity(String.format(xml4LogRequest, tableId), MappingTypeNameConstants.TABLE).enableProperty());
		
		// 进行日志数据的保存
		Object[] old = (isInitial || operation.getBatch()==null)?null:SessionContext.getSqlSession().uniqueQuery_("select id from log_operation_"+tableId+" where batch=?", Arrays.asList(operation.getBatch()));
		if(old == null) {
			SessionContext.getTableSession().save("LOG_OPERATION_"+tableId, operation.toMap());
		} else {
			operation.setId(Integer.parseInt(old[0].toString()));
		}
		
		LogRequest request = operation.getLogRequest();
		if(request != null) {
			request.setOperationId(operation.getId());
			if(old != null)
				request.setOrder(Integer.parseInt(SessionContext.getSqlSession().uniqueQuery_("select count(id) from log_request_"+tableId+" where operation_id=?", Arrays.asList(operation.getId()))[0].toString()));
			SessionContext.getTableSession().save("LOG_REQUEST_"+tableId, request.toMap());
		}
		return new Response(true);
	}
	
	/**
	 * 记录错误token的日志
	 * @param operation
	 * @return
	 */
	@Transaction
	public Response add4ErrorToken(LogOperation operation) {
		SessionContext.getTableSession().save("LOG_ERROR_TOKEN", operation.toErrorTokenMap());
		return new Response(true);
	}

	/**
	 * 查询日志
	 * @param tableId
	 * @param entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transaction(propagationBehavior=PropagationBehavior.SUPPORTS)
	public Response query(int tableId, QueryCriteriaEntity entity) {
		Object result = entity.getMode().executeQuery("QueryLogList", tableId, entity.getParameters());
		if(entity.getMode() instanceof UniqueQueryMode) {
			if(result != null) {
				Map<String, Object> map = (Map<String, Object>) result;
				map.put("USER_NAME", getUserNames(map.get("USER_ID").toString())[0]);
			}
		}else {
			List<Map<String, Object>> listMap = (List<Map<String, Object>>) result;
			if(!listMap.isEmpty()) {
				String[] userIds = new String[listMap.size()];
				int i = 0;
				for (; i < userIds.length; i++) 
					userIds[i]= listMap.get(i).get("USER_ID").toString();
				
				i=0;
				for(String userName: getUserNames(userIds)) 
					listMap.get(i++).put("USER_NAME", userName);
			}
		}
		return new Response(result);
	}
	// 获取用户名称集合
	@SuppressWarnings("unchecked")
	private String[] getUserNames(String... userIds){
		// 构建请求体
		Map<String, Object> requestBody = new HashMap<String, Object>(8);
		requestBody.put("$mode$", "QUERY");
		requestBody.put("ID", "IN("+appendUserId(userIds)+")");
		requestBody.put("ID,", "RESULT()");
		requestBody.put("NAME", "RESULT()");
		
		// 发起api请求
		List<Map<String, String>> users = (List<Map<String, String>>)restTemplate.generalExchange(new APIGeneralServer() {
			
			@Override
			public String getName() {
				return "(同步)查询指定id的用户name集合";
			}
			
			@Override
			public String getUrl() {
				return "http://smt-base/smt-base/user/query4JBPM";
			}
			
		}, JSONObject.toJSONString(requestBody), APIGeneralResponse.class);
		
		// 将userName和userId进行比较和替换
		if(users.size() >0) {
			for(int i=0; i<userIds.length; i++) {
				for(int j=0; j<users.size(); j++) {
					if(users.get(j).get("ID").equals(userIds[i])) {
						userIds[i]= users.get(j).get("NAME");
						break;
					}
				}
			} 
		}
		return userIds;
	}
	private String appendUserId(String... userIds) {
		StringBuilder userIds_ = new StringBuilder(userIds.length*37);
		for(String userId: userIds) {
			if(userIds_.indexOf(userId) == -1)
				userIds_.append(userId).append(',');
		}
		userIds_.setLength(userIds_.length()-1);
		return userIds_.toString();
	}

	/**
	 * 查询日志请求明细
	 * @param operationId
	 * @param tableId
	 * @return
	 */
	@Transaction(propagationBehavior=PropagationBehavior.SUPPORTS)
	public Response queryDetail(int operationId, int tableId) {
		return new Response(SessionContext.getSqlSession().query("select * from log_request_"+ tableId +" where operation_id=?", Arrays.asList(operationId)));
	}
}
