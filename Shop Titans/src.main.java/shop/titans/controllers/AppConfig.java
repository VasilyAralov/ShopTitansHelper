package shop.titans.controllers;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@PropertySource(value="classpath*:persistence.properties")
public class AppConfig {
	
	@Autowired
	Environment env;
	
	@Bean
	DataSource dataSource() {
	    final DriverManagerDataSource dataSource = new DriverManagerDataSource();
	    dataSource.setDriverClassName(env.getProperty("driverClassName"));
	    dataSource.setUrl(env.getProperty("url"));
	    return dataSource;
	}	
	
	@Bean
	JdbcTemplate start(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

}
