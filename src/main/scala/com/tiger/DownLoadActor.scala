package com.tiger

import java.io.File
import java.nio.file.{Files, Paths}

import akka.actor.Actor
import akka.event.Logging
import com.tiger.config.BaseConfig
import com.tiger.mapper.oldDb.OldMapper
import com.tiger.model.Qyzt

/**
  * Created by tiger on 2017/8/11.
  */
class DownLoadActor(oldMapper: OldMapper, dirName:String) extends Actor {

  val log = Logging(context.system, this)

  override def receive: Receive = {
    case q: Qyzt =>
      log.info(q.getId)
      q.getTablename match {
        case "ZPFJ_FJXXB" =>
          Option(oldMapper.findBZaFjxxById(q.getId.substring(6), BaseConfig.dbUsername)) match {
            case Some(value) =>
              Option(value.getFjnr) match {
                case Some(pic) =>
                  Files.write(Paths.get(BaseConfig.DOWNLOAD_DIR + File.separator + dirName + File.separator + q.getId), pic)
                  q.setQyzt("ok")
                case None =>
                  log.error("{} byte is null", q.getId)
                  q.setQyzt(q.getId +" byte is null")
              }
            case None =>
              log.error("{} is null", q.getId)
              q.setQyzt(q.getId + " is null")
          }
        case "ZPFJ_PTRYZPXXB" =>
          Option(oldMapper.findBRyzpById(q.getId.substring(6), BaseConfig.dbUsername)) match {
            case Some(value) =>
              Option(value.getPhoto) match {
                case Some(pic) =>
                  Files.write(Paths.get(BaseConfig.DOWNLOAD_DIR + File.separator + dirName + File.separator + q.getId), pic)
                  q.setQyzt("ok")
                case None =>
                  log.error("{} byte is null", q.getId)
                  q.setQyzt(q.getId + " byte is null")
              }
            case None =>
              log.error("{} is null", q.getId)
              q.setQyzt(q.getId + " is null")
          }
        case _ =>
          log.error("error table name")
          q.setQyzt("error table name")
      }
      sender() ! q
  }
}

