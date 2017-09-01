package com.tiger.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.huawei.obs.services.ObsClient;
import com.tiger.upload.UploadManagerActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UploadService {

    private static Logger logger = LoggerFactory.getLogger("upload");

    @Autowired
    private ObsClient obsClient;

    public void doWork() {

        ActorSystem system = ActorSystem.create("downLoadSystem");
        ActorRef uploadRef = system.actorOf(Props.create(UploadManagerActor.class,obsClient).withDispatcher("tiger-dispatcher"),"uploadManagerActor");
        uploadRef.tell("start",ActorRef.noSender());
    }
}
