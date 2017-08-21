package com.tiger.service;

import com.huawei.obs.services.ObsClient;
import com.huawei.obs.services.exception.ObsException;
import com.founder.config.S3Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateBucketService {

    private static Logger logger = LoggerFactory.getLogger(CreateBucketService.class);

    @Autowired
    private ObsClient obsClient;

    public void doWork(){

        for(int i=0;i< S3Config.BUCKET_HASH_NUM;i++){
            logger.debug("create bucket : {}{}",S3Config.BUCKE_TNAME_PREFIX,i);
            try {
                obsClient.createBucket(S3Config.BUCKE_TNAME_PREFIX + i);
            } catch (ObsException e) {
                logger.error("create bucket error: {}{} : {} ",S3Config.BUCKE_TNAME_PREFIX,i ,e.getErrorMessage());
            }
        }
        logger.debug("create bucket successed...");
    }

    public void createSingle(){
        try {
            if(S3Config.CREATE_SINGLE_BUCKETNAME.isEmpty()){
                logger.error("please check 's3.createSingleBucketName' in application.yml!");
            }
            obsClient.createBucket(S3Config.CREATE_SINGLE_BUCKETNAME);
        } catch (ObsException e) {
            logger.error("create bucket error: {} : {} ",S3Config.CREATE_SINGLE_BUCKETNAME ,e.getErrorMessage());
        }
    }

}
