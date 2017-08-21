package com.tiger.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.Order;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Package: com.tiger.config
 * ClassName: BaseConfig
 * Author: Tiger
 * Description:
 * CreateDate: 2016/10/8
 * Version: 1.0
 */

@Configuration
@Order(1)
@ComponentScan(basePackages = {"com.tiger", "com.founder"}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.tiger.model")})
public class BaseConfig {

    private static Logger logger = LoggerFactory.getLogger(BaseConfig.class);

    public static String xzqh;

    @Value("${db.xzqh}")
    public void setXzqh(String xzqh) {
        BaseConfig.xzqh = xzqh;
    }

    public static String dbUsername;

    @Value("${db.dbUsername}")
    public void setDbUsername(String dbUsername) {
        BaseConfig.dbUsername = dbUsername;
    }

    public static String TABLE_NAME;

    @Value("${db.tablename}")
    public void setTablename(String tablename) {
        BaseConfig.TABLE_NAME = tablename;
    }

    @Bean(name = "executor")
    public ExecutorService initExecutorService(@Value("${job.downloadThreadNum}") int downloadThreadNum) {
        return Executors.newFixedThreadPool(downloadThreadNum);
    }

    public static int WORK_TYPE = -1;

    @Value("${job.workType}")
    public void setWorkType(int workType) {
        WORK_TYPE = workType;
    }

    public static int QUREY_ROWS_NUM;

    @Value("${job.qureyRowsNum}")
    public void setQureyRowsNum(int qureyRowsNum) {
        QUREY_ROWS_NUM = qureyRowsNum;
    }

    public static String DOWNLOAD_DIR;

    @Value("${file.downloadDir}")
    public void setDownloadDir(String downloadDir) {
        BaseConfig.DOWNLOAD_DIR = downloadDir;
    }

    public static boolean OPEN_JOB_DONE_MODE = true;

    @Value("${job.openJobDoneMode}")
    public void setOpenJobDoneMode(String openJobDoneMode) {
        if (openJobDoneMode.contains("true")) {
            BaseConfig.OPEN_JOB_DONE_MODE = true;
        } else {
            BaseConfig.OPEN_JOB_DONE_MODE = false;
        }
    }

    public final static String JOB_DONE_FILENAME = "jobDone";

    public final static String FINISDED_COUNT_FILENAME = "finishedCount";


}
