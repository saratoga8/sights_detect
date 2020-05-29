package com.sights_detect.core.seekers.objects.google

import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.HttpRequestTimeoutException
import io.ktor.network.sockets.ConnectTimeoutException
import org.apache.http.ConnectionClosedException
import org.apache.logging.log4j.kotlin.Logging
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.UnknownHostException
import java.util.*
import javax.imageio.ImageIO

internal open class GoogleVision(properties: Properties): Logging {
	open val request = com.sights_detect.core.net.Request(properties)
	var error = ""

	companion object {
		private val MIN_SIZE: Pair<Int, Int> = Pair(700, 500)
		private val GOOGLE_RECOMMENDED_SIZE: Pair<Int, Int> = Pair(700, 500)
	}

	suspend fun doRequest(imgPath: String): GoogleResponse {
		return try {
			System.gc()
			request.post(buildRequest(imgPath))
		}
		catch (e: ClientRequestException) {
			exceptionResponse(e, "HTTP request to Google Vision Service has failed")
		}
		catch (e: HttpRequestTimeoutException) {
			exceptionResponse(e, "HTTP request to Google Vision Service timed out")
		}
		catch (e: ConnectTimeoutException) {
			exceptionResponse(e, "Connection to Google Vision Service timed out")
		}
		catch (e: ConnectionClosedException) {
			exceptionResponse(e, "Connection to Google Vision Service has been closed")
		}
		catch (e: UnknownHostException) {
			exceptionResponse(e, "Cant connect to Google Vision Service")
		}
		catch (e: IOException) {
			exceptionResponse(e, "Cant resize the image $imgPath")
		}
		catch (e: IllegalArgumentException) {
			exceptionResponse(e, "Cant build request with pic $imgPath")
		}
	}

	fun stop() = request.stop()

	private fun exceptionResponse(exception: Exception, msg: String): GoogleResponse {
		error = msg
//		logger.error(exception.toString())
		return GoogleResponse(listOf())
	}

	private fun encodeFile(path: String): String {
//		logger.debug("Encoding picture in file $path")        //      makes error during testing
		val file = File(path)
		require(file.exists())    { "The given image's path $path doesn't exist" }
		val bytes = imgToBytes(file)
		return Base64.getEncoder().encodeToString(bytes)
	}

	private fun imgToBytes(file: File): ByteArray {
		val img = ImageIO.read(file) ?: throw IllegalArgumentException("Cant read image from the file ${file.absolutePath}")
		return if (img.height > MIN_SIZE.second || img.width > MIN_SIZE.first)
			resize(GOOGLE_RECOMMENDED_SIZE, img, file.extension)
		else
			file.readBytes()
	}

	private fun resize(size: Pair<Int, Int>, inputImage: BufferedImage, imgFormat: String): ByteArray {
		val outputImage: BufferedImage = BufferedImage(size.first, size.second, inputImage.type)
		val g2d: Graphics2D = outputImage.createGraphics()
		g2d.drawImage(inputImage, 0, 0, size.first, size.second, null)
		g2d.dispose()

		val byteArrOutStream = ByteArrayOutputStream()
		ImageIO.write(outputImage, imgFormat, byteArrOutStream)
		return byteArrOutStream.toByteArray()
	}

	protected fun buildRequest(path: String): Any {
		val features = listOf(Feature(1, "LANDMARK_DETECTION"), Feature(2, "WEB_DETECTION"))
		val requests = listOf(Request(features, Image(encodeFile(path))))
		return GoogleRequest(requests)
	}
}