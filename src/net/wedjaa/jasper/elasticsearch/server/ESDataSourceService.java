package net.wedjaa.jasper.elasticsearch.server;

import java.util.Map;

import net.sf.jasperreports.engine.JRParameter;
import net.wedjaa.jasper.elasticsearch.ESSearch;

import com.jaspersoft.jasperserver.api.metadata.jasperreports.service.ReportDataSourceService;

public class ESDataSourceService implements ReportDataSourceService {

	String elasticSearchIndexes;
	String elasticSearchTypes;
	
	String elasticSearchHost;
	String elasticSearchPort;
	String elasticSearchCluster;

	String elasticSearchUsername;
	String elasticSearchPassword;

	String elasticSearchMode;
	
	ESSearch esSearch = null;
	
	public ESDataSourceService() {
		esSearch = null;
	}
	
	@Override
	public void closeConnection() {
		if ( esSearch != null ) {
			esSearch.close();
			esSearch = null;
		}
		return;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setReportParameterValues(@SuppressWarnings("rawtypes") Map parameters) {
		esSearch = new ESSearch(elasticSearchIndexes, 
				elasticSearchTypes, Integer.parseInt(elasticSearchMode), 
				elasticSearchHost, Integer.parseInt(elasticSearchPort), 
				elasticSearchUsername, elasticSearchPassword, elasticSearchCluster);
		parameters.put(JRParameter.REPORT_CONNECTION, esSearch);
	}

	public String getElasticSearchIndexes() {
		return elasticSearchIndexes;
	}

	public void setElasticSearchIndexes(String elasticSearchIndexes) {
		this.elasticSearchIndexes = elasticSearchIndexes;
	}

	public String getElasticSearchTypes() {
		return elasticSearchTypes;
	}

	public void setElasticSearchTypes(String elasticSearchTypes) {
		this.elasticSearchTypes = elasticSearchTypes;
	}

	public String getElasticSearchHost() {
		return elasticSearchHost;
	}

	public void setElasticSearchHost(String elasticSearchHost) {
		this.elasticSearchHost = elasticSearchHost;
	}

	public String getElasticSearchPort() {
		return elasticSearchPort;
	}

	public void setElasticSearchPort(String elasticSearchPort) {
		this.elasticSearchPort = elasticSearchPort;
	}

	public String getElasticSearchCluster() {
		return elasticSearchCluster;
	}

	public void setElasticSearchCluster(String elasticSearchCluster) {
		this.elasticSearchCluster = elasticSearchCluster;
	}

	public String getElasticSearchUsername() {
		return elasticSearchUsername;
	}

	public void setElasticSearchUsername(String elasticSearchUsername) {
		this.elasticSearchUsername = elasticSearchUsername;
	}

	public String getElasticSearchPassword() {
		return elasticSearchPassword;
	}

	public void setElasticSearchPassword(String elasticSearchPassword) {
		this.elasticSearchPassword = elasticSearchPassword;
	}

	public String getElasticSearchMode() {
		return elasticSearchMode;
	}

	public void setElasticSearchMode(String elasticSearchMode) {
		this.elasticSearchMode = elasticSearchMode;
	}


}
