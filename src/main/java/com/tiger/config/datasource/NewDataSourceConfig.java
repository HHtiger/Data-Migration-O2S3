package com.tiger.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

/**
 * Package: com.tiger.config
 * ClassName: DataSourceConfig
 * Author: Tiger
 * Description:
 * CreateDate: 2016/10/5
 * Version: 1.0
 */

@Configuration
@Order(2)
@EnableTransactionManagement
@MapperScan(basePackages = "com.tiger.mapper.newDb", sqlSessionFactoryRef = "newSqlSessionFactory")
public class NewDataSourceConfig {

    private static Logger logger = LoggerFactory.getLogger(NewDataSourceConfig.class);

//    @Autowired
//    private StatFilter statFilter;
//
//    @Autowired
//    private Slf4jLogFilter logFilter;

    @Bean
    public ServletRegistrationBean druidServlet() {
        ServletRegistrationBean reg = new ServletRegistrationBean();
        reg.setServlet(new StatViewServlet());
        reg.addUrlMappings("/druid/*");
        // reg.addInitParameter("allow", "127.0.0.1");
        // reg.addInitParameter("deny","");
        reg.addInitParameter("loginUsername", "admin");
        reg.addInitParameter("loginPassword", "admin");
        return reg;
    }


    @Primary
    @Bean(name = "newDruidDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.new")
    public DruidDataSource druidDataSource() {
        return new DruidDataSource();
    }

    @Primary
    @Bean(name = "newSqlSessionFactory")
    public SqlSessionFactory newSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        logger.info("new sqlsession--" + this.druidDataSource().hashCode());
        logger.info("new filtersize--" + this.druidDataSource().getProxyFilters().size());
        sqlSessionFactoryBean.setDataSource(this.druidDataSource());
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("dialect", "oracle");
        properties.setProperty("pageSizeZero", "true");
        properties.setProperty("reasonable", "false");
        properties.setProperty("params", "pageNum=pageHelperStart;pageSize=pageHelperRows;");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "none");
        pageHelper.setProperties(properties);
        Interceptor[] interceptors = new Interceptor[] { pageHelper };
        sqlSessionFactoryBean.setPlugins(interceptors);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "newTxMan")
    public PlatformTransactionManager primaryTransactionManager() {
        logger.info("new dataSource--" + this.druidDataSource().hashCode());
        return new DataSourceTransactionManager(this.druidDataSource());
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }

}
