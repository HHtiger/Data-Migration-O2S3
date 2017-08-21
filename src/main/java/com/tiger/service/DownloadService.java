package com.tiger.service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.github.pagehelper.PageHelper;
import com.tiger.PageActor;
import com.tiger.SubmitDownLoadStatusActor;
import com.tiger.config.BaseConfig;
import com.tiger.mapper.newDb.NewMapper;
import com.tiger.mapper.oldDb.OldMapper;
import com.tiger.model.Qyzt;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DownloadService {

    private static Logger logger = LoggerFactory.getLogger("download");

    @Autowired
    private OldMapper oldUserMapper;

    @Autowired
    private NewMapper newUserMapper;

    @Autowired
    private SqlSessionFactory newSqlSessionFactory;

    private List<Qyzt> queryAllNewUserByPage(int pageNum) {
        PageHelper.startPage(pageNum, BaseConfig.QUREY_ROWS_NUM);
        return newUserMapper.queryAll(BaseConfig.xzqh, BaseConfig.TABLE_NAME);
    }

    public void doWork() throws ExecutionException, InterruptedException {

        ActorSystem system = ActorSystem.create("downLoadSystem");
        ActorRef submitStatuRef = system.actorOf(Props.create(SubmitDownLoadStatusActor.class, newSqlSessionFactory));
        ActorRef pageRef = system.actorOf(Props.create(PageActor.class,submitStatuRef,newUserMapper,oldUserMapper,1,Long.toString(System.currentTimeMillis())));
        pageRef.tell("start",pageRef);

    }

}
