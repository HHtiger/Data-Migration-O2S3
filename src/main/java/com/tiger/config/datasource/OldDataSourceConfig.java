package com.tiger.config.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@MapperScan(basePackages = "com.tiger.mapper.oldDb", sqlSessionFactoryRef = "oldSqlSessionFactory")
public class OldDataSourceConfig {

    private static Logger logger = LoggerFactory.getLogger(OldDataSourceConfig.class);

//    @Autowired
//    private StatFilter statFilter;

    @Bean(name = "oldDruidDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.old")
    public DruidDataSource druidDataSource() {
        return new DruidDataSource();
    }

    @Bean(name = "oldSqlSessionFactory")
    public SqlSessionFactory oldSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        logger.info("old sqlsession--" + this.druidDataSource().hashCode());
        logger.info("old filtersize--" + this.druidDataSource().getProxyFilters().size());
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
//        sqlSessionFactoryBean.getObject().getConfiguration().setDefaultExecutorType(ExecutorType.BATCH);
//        logger.debug(sqlSessionFactoryBean.getObject().getConfiguration().getDefaultExecutorType().toString());
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "oldTxMan")
    public PlatformTransactionManager primaryTransactionManager() {
        logger.info("old dataSource--" + this.druidDataSource().hashCode());
        return new DataSourceTransactionManager(this.druidDataSource());
    }

}
