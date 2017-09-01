package com.tiger.upload

import java.awt.image.RenderedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}
import java.nio.file.{Files, Path}
import java.util.concurrent.CountDownLatch
import javax.media.jai.JAI

import akka.actor.{Actor, PoisonPill}
import akka.event.Logging
import com.founder.config.S3Config
import com.founder.util.OBSUtil
import com.huawei.obs.services.ObsClient
import com.sun.media.jai.codec._
import com.tiger.config.BaseConfig
import com.tiger.util.FileType
import net.coobird.thumbnailator.Thumbnails


class FileCtlActor(file: Path, obsClient: ObsClient, jobCountDown: CountDownLatch) extends Actor {
	
	val log = Logging(context.system.eventStream, "upload")
	
	var fileBytes: Array[Byte] = _
	var bucketName: String = _
	
	override def preStart(): Unit = {
		log.debug("{} - new FileCtlActor : {}", context.dispatcher, self.path)
		self ! ("start", obsClient, jobCountDown)
	}
	
	override def postStop(): Unit = {
		log.debug("{} stop", self.path)
	}
	
	override def receive = {
		
		case ("start", obsClient: ObsClient, jobCountDown: CountDownLatch) =>
			
			file match {
				case _ if file.toFile.isFile =>
					try
						fileBytes = Files.readAllBytes(file)
					catch {
						case e: IOException =>
							log.error("read file error : {}", e.getMessage)
					}
					bucketName = S3Config.getHashBucketName(file.getFileName.toString)
					log.debug("{}", bucketName)
				
				case _ =>
					log.error("{} is not a file ", file)
			}
			
			nomal(obsClient)
			if (BaseConfig.IS_TO_SMALL) small(obsClient)
			
			Files.delete(file)
			jobCountDown.countDown()
			self ! PoisonPill
		
	}
	
	def small(obsClient: ObsClient): Unit = {
		try {
			val fileType = FileType.getFileType(fileBytes)
			if (!fileType.isEmpty && isPic(fileType)) {
				val small_file_out = new ByteArrayOutputStream
				val fis = new FileSeekableStream(file.toFile)
				var in: RenderedImage = JAI.create("stream", fis)
				val fos = new ByteArrayOutputStream
				val encodeParam = new JPEGEncodeParam
				if (fileType == "tif") {
					val decodeParam = new TIFFDecodeParam
					decodeParam.setJPEGDecompressYCbCrToRGB(false)
					val decoder = ImageCodec.createImageDecoder("TIFF", fis, decodeParam)
					in = decoder.decodeAsRenderedImage
				}
				val encoder = ImageCodec.createImageEncoder("JPEG", fos, encodeParam)
				encoder.encode(in)
				Thumbnails.of(new ByteArrayInputStream(fos.toByteArray)).size(180, 220).outputFormat("JPEG").toOutputStream(small_file_out)
				fos.close()
				fis.close()
				log.warning("{} - {} begin to upload small file ", context.dispatcher, self.path)
				OBSUtil.putObject(small_file_out.toByteArray, obsClient, bucketName, file.getFileName.toString + "_small")
				log.warning("{} - {} finish to upload small file ", context.dispatcher, self.path)
				small_file_out.close()
			}
			else throw new IOException("not a pic,type is : " + fileType)
		} catch {
			case e: Throwable =>
				log.error("file can not resize to small {},{}", file, e.getMessage)
		}
	}
	
	def nomal(obsClient: ObsClient) = {
		log.warning("{} - {} begin to upload normal file ", context.dispatcher, self.path)
		OBSUtil.putObject(fileBytes, obsClient, bucketName, file.getFileName.toString)
		log.warning("{} - {} finish to upload normal file ", context.dispatcher, self.path)
	}
	
	def isPic(fileType: String) = Array("jpg", "png", "gif", "tif", "bmp").contains(fileType)
	
}