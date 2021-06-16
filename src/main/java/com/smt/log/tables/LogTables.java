package com.smt.log.tables;

import java.util.Date;

/**
 * 
 * @author DougLei
 */
public class LogTables {
	private int id;
	private String createUserId;
	private Date createDate;
	private String projectCode;
	private String tenantId;
	private int isDeleted;
	
	public LogTables() {}
	public LogTables(String createUserId, Date createDate, String projectCode, String tenantId) {
		this.createUserId = createUserId;
		this.createDate = createDate;
		this.projectCode = projectCode;
		this.tenantId = tenantId;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCreateUserId() {
		return createUserId;
	}
	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public String getProjectCode() {
		return projectCode;
	}
	public void setProjectCode(String projectCode) {
		this.projectCode = projectCode;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public int getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}
}
