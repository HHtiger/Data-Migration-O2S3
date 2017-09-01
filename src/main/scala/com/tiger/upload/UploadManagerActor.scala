package com.tiger.upload

import java.io.File
import java.nio.file.{Files, Paths}
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.Logging
import com.huawei.obs.services.ObsClient
import com.tiger.config.BaseConfig

class UploadManagerActor(obsClient: ObsClient) extends Actor {
	
	val log = Logging(context.system.eventStream, "upload")
	
	override def receive = {
		
		case "start" =>
			
			val rootPath = BaseConfig.DOWNLOAD_DIR + File.separator;
			
			val dirs = Paths.get(rootPath)
				.toFile
				.listFiles()
				.filter(d => Files.exists(Paths.get(d + File.separator + BaseConfig.JOB_DONE_FILENAME)))
			
			dirs match {
				case dirs if dirs.nonEmpty =>
					
					val d = dirs(0)
					context.actorOf(Props.create(classOf[DirCtlActor], d.toPath, obsClient).withDispatcher("tiger-dispatcher"), "d_" + System.currentTimeMillis() + "_" + d.getName)
				
				case _ =>
					log.debug("no jobDone dirs ")
					TimeUnit.SECONDS.sleep(2)
					self ! "start"
			}
		
	}
	
}