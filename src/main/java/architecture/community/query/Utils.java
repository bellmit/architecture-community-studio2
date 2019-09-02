package architecture.community.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.SqlParameterValue;

import architecture.community.query.dao.CustomQueryJdbcDao;
import architecture.community.web.model.DataSourceRequest;
import architecture.ee.jdbc.sqlquery.mapping.BoundSql;

public class Utils {
	
	
	public static Map<String, Object> getAdditionalParameter( DataSourceRequest dataSourceRequest ){
		Map<String, Object> additionalParameter = new HashMap<String, Object>();
		additionalParameter.put("filter", dataSourceRequest.getFilter());
		additionalParameter.put("sort", dataSourceRequest.getSort());		
		additionalParameter.put("data", dataSourceRequest.getData());
		return additionalParameter;
	}
	
	public static List<SqlParameterValue> getSqlParameterValues (List<ParameterValue> values ){
		ArrayList<SqlParameterValue> al = new ArrayList<SqlParameterValue>();	
		for( ParameterValue v : values)
		{
			//logger.debug("isSetByObject : {} , {}", v.isSetByObject(), v.getValueObject() );
			al.add(new SqlParameterValue(v.getJdbcType(), v.isSetByObject() ? v.getValueObject() : v.getValueText()) );
		}
		return al;
	}
	

	public static <T> List<T> list(CustomQueryJdbcDao dao, DataSourceRequest request, Class<T> elementType) {	
		BoundSql sqlSource = dao.getBoundSqlWithAdditionalParameter(request.getStatement(), getAdditionalParameter(request));		
		if( request.getPageSize() > 0 ) {
			// paging 
			if( request.getParameters().size() > 0 ) 
				return dao.getExtendedJdbcTemplate().query( 
					sqlSource.getSql(), 
					request.getSkip(), 
					request.getPageSize(),
					elementType,
					architecture.community.query.Utils.getSqlParameterValues( request.getParameters() ).toArray());
			else 
				return dao.getExtendedJdbcTemplate().query( 
					sqlSource.getSql(), 
					request.getSkip(), 
					request.getPageSize(),
					elementType);
			
		}else {		
			if( request.getParameters().size() > 0 )
				return dao.getExtendedJdbcTemplate().queryForList(sqlSource.getSql(), elementType, getSqlParameterValues( request.getParameters() ).toArray() );
			else	
				return dao.getExtendedJdbcTemplate().queryForList(sqlSource.getSql(), elementType);
		}
	}
	
	public static <T> T queryForObject (CustomQueryJdbcDao dao, DataSourceRequest request, Class<T> requiredType) {				
		BoundSql sqlSource = dao.getBoundSqlWithAdditionalParameter(request.getStatement(), getAdditionalParameter(request));		
		if( request.getParameters().size() > 0 )
			return dao.getExtendedJdbcTemplate().queryForObject( sqlSource.getSql(), requiredType, getSqlParameterValues( request.getParameters() ).toArray());
		else	
			return dao.getExtendedJdbcTemplate().queryForObject( sqlSource.getSql(), requiredType );
	}
}
