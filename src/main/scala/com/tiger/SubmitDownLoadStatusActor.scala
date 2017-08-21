package com.tiger

import java.sql.{Connection, PreparedStatement}

import akka.actor.Actor
import akka.event.Logging
import com.tiger.config.BaseConfig
import com.tiger.model.Qyzt
import org.apache.ibatis.session.{ExecutorType, SqlSessionFactory}

/**
  * Created by tiger on 2017/8/14.
  */
class SubmitDownLoadStatusActor(newSqlSessionFactory: SqlSessionFactory) extends Actor{

  val log = Logging(context.system, this)
  val connection: Connection = newSqlSessionFactory.openSession(ExecutorType.REUSE).getConnection
  val pstmt: PreparedStatement = connection.prepareStatement("update " + BaseConfig.TABLE_NAME + " set qyzt = ? where id = ?")

  override def preStart(): Unit = {
    connection.setAutoCommit(false)
  }

  override def receive: Receive = {
    case q:Qyzt =>
      log.debug("{} is {}",q.getId,q.getQyzt)
      pstmt.setString(1, q.getQyzt)
      pstmt.setString(2, q.getId)
      pstmt.addBatch()
    case "commit" =>
        doCommit()
      sender() ! "ok"

  }

  private def doCommit() = {
      pstmt.executeBatch
      connection.commit()
      pstmt.clearBatch()
      log.debug("do submit.")
  }
}
