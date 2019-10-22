package me.exrates.externalservice.configurations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import wrappers.NamedParameterJdbcTemplateWrapper;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

    @Value("${datasource.driver-class-name}")
    private String driverClassName;
    @Value("${datasource.url}")
    private String jdbcUrl;
    @Value("${datasource.username}")
    private String user;
    @Value("${datasource.password}")
    private String password;

    @Bean(name = "masterHikariDataSource")
    public DataSource masterHikariDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setConnectionInitSql("SELECT 1");
        hikariConfig.setConnectionTimeout(5 * 1000);
        hikariConfig.setValidationTimeout(5 * 1000);
        hikariConfig.setIdleTimeout(5 * 60 * 1000);
        hikariConfig.setMaxLifetime(10 * 60 * 1000);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setMaximumPoolSize(25);
        hikariConfig.setInitializationFailTimeout(0);
        return new HikariDataSource(hikariConfig);
    }

    @DependsOn("masterHikariDataSource")
    @Bean(name = "masterTemplate")
    public NamedParameterJdbcOperations masterNamedParameterJdbcTemplate(@Qualifier("masterHikariDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplateWrapper(dataSource);
    }

    @Bean(name = "masterTxManager")
    public PlatformTransactionManager masterPlatformTransactionManager() {
        return new DataSourceTransactionManager(masterHikariDataSource());
    }
}