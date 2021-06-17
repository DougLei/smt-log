package com.smt.parent.code.filters.token;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import com.alibaba.fastjson.JSONObject;
import com.douglei.tools.ExceptionUtil;
import com.douglei.tools.StringUtil;
import com.smt.parent.code.filters.FilterEnum;
import com.smt.parent.code.filters.log.LogContext;
import com.smt.parent.code.response.Response;
import com.smt.parent.code.response.ResponseUtil;
import com.smt.parent.code.spring.eureka.cloud.feign.APIServer;
import com.smt.parent.code.spring.eureka.cloud.feign.RestTemplateWrapper;

/**
 * token验证过滤器
 * @author DougLei
 */
public class TokenFilter implements Filter {
	private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);
	private static final String IP = "192.168.1.222";
	
	@Autowired
	private TokenConfigurationProperties properties;
	
	@Autowired
	private RestTemplateWrapper restTemplate;

	@Override
	public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		if((properties.getIgnoreUrlMatcher() == null || !properties.getIgnoreUrlMatcher().match(request.getServletPath())) 
				&& !validate(request, (HttpServletResponse)response)) 
			return;
		chain.doFilter(req, response);
		TokenContext.remove();
	}
	
	// 验证token
	private boolean validate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TokenValidateResult result = validate_(req);
		TokenEntity entity = result.getEntity();
			
		// 验证成功, 则在日志中记录用户/项目/租户的唯一标识, 并记录token数据
		if(result.isSuccess()) {
			LogContext.loggingIds(entity.getUserId(), entity.getProjectCode(), entity.getTenantId());
			TokenContext.set(entity);
			return true;
		}
		
		// 验证失败, 日志记录token值和响应体, 并输出失败的具体信息
		Response response = new Response(null, null, result.getMessage(), result.getCode(), result.getParams());
		LogContext.loggingToken(result.getToken());
		LogContext.loggingResponse(response, true);
		ResponseUtil.writeJSON(resp, response.toJSONString());
		return false;
	}
	
	// 验证token
	private TokenValidateResult validate_(HttpServletRequest req) throws IOException {
		String token_ = req.getHeader("token_");
		if(StringUtil.unEmpty(token_)) 
			return new TokenValidateResult(JSONObject.parseObject(URLDecoder.decode(token_, StandardCharsets.UTF_8.name()), TokenEntity.class));
		
		String token = req.getHeader(FilterEnum.TOKEN.getHeaderName());
		if(StringUtil.isEmpty(token))
			return new TokenValidateResult(token, "token不能为空", "smt.parent.token.filter.validate.isnull");
		
		try {
			return restTemplate.exchange(new APIServer() {
				
				@Override
				public String getName() {
					return "(同步)验证Token";
				}
				
				@Override
				public String getUrl() {
					return "http://smt-base/smt-base/token/validate/" + token + "?clientIp=" +IP;
				}
				
				@Override
				public HttpMethod getRequestMethod() {
					return HttpMethod.GET;
				}

			}, null, TokenValidateResult.class).getBody();
		} catch (Exception e) {
			String exceptionId = UUID.randomUUID().toString();
			logger.error("(同步)验证token异常, exceptionId=[{}], exceptionDetail=\n{}", exceptionId, ExceptionUtil.getStackTrace(e));
			return new TokenValidateResult(token, "(同步)验证token异常, 联系管理员查看日志, exceptionId=[%s]", "smt.parent.token.filter.validate.exception", exceptionId);
		}
	}
}