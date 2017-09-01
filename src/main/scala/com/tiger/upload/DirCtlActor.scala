package com.tiger.upload

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.CountDownLatch

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, OneForOneStrategy, PoisonPill, Props}
import akka.event.Logging
import akka.util.Timeout
import com.huawei.obs.services.ObsClient
import com.tiger.config.BaseConfig

import scala.concurrent.duration._

class DirCtlActor(dir: Path, obsClient: ObsClient) extends Actor {
	
	val log = Logging(context.system.eventStream, "upload")
	val files: Array[File] = dir.toFile.listFiles()
	val jobCountDown: CountDownLatch = new CountDownLatch(files.length - 1)
	
	override val supervisorStrategy: OneForOneStrategy =
		OneForOneStrategy(maxNrOfRetries = 5) {
			case _ =>
				jobCountDown.countDown()
				Stop
			//      case _: NullPointerException => Restart
			//      case _: IllegalArgumentException => Stop
			//      case _: Exception => Escalate
		}
	
	override def preStart(): Unit = {
		dir match {
			case _ if dir.toFile.isDirectory =>
				log.debug("into dir {}", self.path)
				
				
				implicit val timeout = Timeout(5 seconds)
				
				files.filterNot(cd => {
					cd.toPath.getFileName.toString.equals(BaseConfig.JOB_DONE_FILENAME)
				})
					.foreach(cd => {
						
						log.debug("{},{}", context.dispatcher, cd)
						context.actorOf(Props.create(classOf[FileCtlActor], cd.toPath, obsClient, jobCountDown).withDispatcher("blocking-io-dispatcher"), "f_" + System.currentTimeMillis() + "_" + cd.getName)
						
					})
				
				jobCountDown.await()
				
				log.debug("{} ok", self.path)
				
				self ! PoisonPill
			
			case _ =>
				log.error("{} is not a dir ", dir)
		}
	}
	
	override def postStop(): Unit = {
		try {
			log.debug("file numbers {}", dir.toFile.listFiles().length)
			if (dir.toFile.listFiles().length <= 1) {
				Files.delete(Paths.get(dir + File.separator + BaseConfig.JOB_DONE_FILENAME))
				Files.delete(dir)
			}
		} catch {
			case e: Exception => log.error(e.getMessage)
		} finally {
			context.system.actorSelection("user/uploadManagerActor") ! "start"
		}
		
	}
	
	override def receive = {
		case _ =>
		
	}
	
}