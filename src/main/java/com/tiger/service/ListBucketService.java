package com.tiger.service;

import com.huawei.obs.services.ObsClient;
import com.huawei.obs.services.exception.ObsException;
import com.tiger.util.OBSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ListBucketService {

    private static Logger logger = LoggerFactory.getLogger(ListBucketService.class);

    @Autowired
    private ObsClient obsClient;

    public void doWork() throws ObsException {
        OBSUtil.listBucket(obsClient);
    }
}
