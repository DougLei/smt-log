declare @adminId char(36)
declare @tenantId char(36)

set @adminId= '200ceb26807d6bf99fd6f4f0d1ca54d40001'
set @tenantId= '200ceb26807d6bf99fd6f4f0d1ca54d40002'


-- 初始化日志表信息
insert into log_tables(create_user_id, create_date, project_code, tenant_id, is_deleted)
	values(@adminId, getdate(), 'SMT_ROOT', @tenantId, 0)
insert into log_tables(create_user_id, create_date, project_code, tenant_id, is_deleted)
	values(@adminId, getdate(), 'SMT_VC', @tenantId, 0)
insert into log_tables(create_user_id, create_date, project_code, tenant_id, is_deleted)
	values(@adminId, getdate(), 'SMT_TS', @tenantId, 0)
select * from log_tables	


