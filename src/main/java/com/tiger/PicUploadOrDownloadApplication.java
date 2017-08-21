package com.tiger;

import com.founder.config.S3Config;
import com.tiger.config.BaseConfig;
import com.tiger.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class PicUploadOrDownloadApplication implements CommandLineRunner {

    private static Logger logger = LoggerFactory.getLogger(PicUploadOrDownloadApplication.class);

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private CreateBucketService createBucketService;

    @Autowired
    private ListBucketService listBucketService;

    @Autowired
    private InitService initService;

    public static ApplicationContext ctx;

    public static void main(String[] args) {
         ctx = SpringApplication.run(PicUploadOrDownloadApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        switch (BaseConfig.WORK_TYPE) {
            case 0:
                logger.debug("begin download....");
                downloadService.doWork();
                break;
//            case 1:
//                logger.debug("begin upload....");
//                uploadService.doWork();
//                break;
            case 2:
                logger.debug("begin create bucket....");
                createBucketService.doWork();
                break;
            case 3:
                logger.debug("begin list bucket....");
                listBucketService.doWork();
                break;
            case 4:
                logger.debug("begin create single Bucket : {} ", S3Config.CREATE_SINGLE_BUCKETNAME);
                createBucketService.createSingle();
                break;
            case 5:
                logger.debug("query data at ==> http://{ip}:{port}/s3/queryPicByObjectKey/{objectKey}");
                System.in.read();
                break;
            default:
                logger.error(
                        "please check 'job.workType' in application.yml \n" +
                        "0 -> download \n" +
                        "1 -> upload \n" +
                        "2 -> create bucket \n" +
                        "3 -> list buckets \n" +
                        "4 -> create single Bucket \n" +
                        "5 -> start list servers \n"
                );
        }
//        initService.initData();
//        System.exit(0);

    }

}