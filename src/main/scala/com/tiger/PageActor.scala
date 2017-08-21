package com.tiger

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.event.Logging
import akka.pattern._
import akka.util.Timeout
import com.github.pagehelper.{PageHelper, PageInfo}
import com.tiger.config.BaseConfig
import com.tiger.mapper.newDb.NewMapper
import com.tiger.mapper.oldDb.OldMapper
import com.tiger.model.Qyzt

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class PageActor(submitStatuRef:ActorRef,newMapper: NewMapper, oldMapper: OldMapper, page: Int,var dirName: String) extends Actor {

  val log = Logging(context.system, this)
  var FileNum = new AtomicInteger(0)
  var list: mutable.Buffer[Qyzt] = _

  def mkdir: Unit = {
    val dir = new File(BaseConfig.DOWNLOAD_DIR + File.separator + dirName + File.separator)
    val isCreate = dir.mkdir
    if (!isCreate) mkdir
  }

  def query = {
    PageHelper.startPage(page, BaseConfig.QUREY_ROWS_NUM)
    list = newMapper.queryAll(BaseConfig.xzqh, BaseConfig.TABLE_NAME).asScala
  }

  override def preStart(): Unit = {
    mkdir
    query
  }

//  override def postStop(): Unit = {
//    log.info("{} finished ", dirName)
//    val jobDoneFileName = BaseConfig.DOWNLOAD_DIR + File.separator + dirName + File.separator + BaseConfig.JOB_DONE_FILENAME
//    Files.createFile(Paths.get(jobDoneFileName))
//  }

  override def receive = {
    case "start" =>
      for (q <- list) {
        val loadRef = context.actorOf(Props.create(classOf[DownLoadActor], oldMapper, dirName))
        loadRef ! q
      }
    case q:Qyzt =>
      sender() ! PoisonPill

      submitStatuRef ! q

      if (FileNum.incrementAndGet() == list.size) {
//        self ! PoisonPill

        import scala.concurrent.ExecutionContext.Implicits.global
        implicit val timeout = Timeout(100 seconds)
        val fCommit = submitStatuRef ? "commit"
        fCommit.onComplete {
          case Success(r) =>
            FileNum.set(0)
            log.info("{} finished ", dirName)
            val jobDoneFileName = BaseConfig.DOWNLOAD_DIR + File.separator + dirName + File.separator + BaseConfig.JOB_DONE_FILENAME
            Files.createFile(Paths.get(jobDoneFileName))

            val page = new PageInfo[Qyzt](list.asJava)
            if(page.isHasNextPage){
              dirName = System.currentTimeMillis() + ""
              mkdir
              query
              self ! "start"
            }else{
              self ! PoisonPill
              log.debug("all done")
            }
          case Failure(NonFatal(e)) =>
            println("commit qyzt timeout")
        }

      }
  }

}
