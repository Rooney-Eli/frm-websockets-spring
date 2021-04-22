package com.example.springbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.BinaryWebSocketHandler
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@SpringBootApplication
class SpringBackendApplication

fun main(args: Array<String>) {
	runApplication<SpringBackendApplication>(*args)
}

@Configuration
@EnableWebSocket
class WebSocket() : WebSocketConfigurer {

	val textSession = ConcurrentHashMap<String, WebSocketSession>()
	val binarySession = ConcurrentHashMap<String, WebSocketSession>()

	override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
		registry
			.addHandler(SocketTextHandler(), "/textSocket")
			.addHandler(SocketBinaryHandler(), "/binSocket")
			.setAllowedOrigins("*")
	}

	inner class SocketTextHandler : TextWebSocketHandler() {

		override fun afterConnectionEstablished(session: WebSocketSession) {
			println("User with address: ${session.remoteAddress} connected to socket with ID: ${session.id}")
			textSession[session.id] = session
		}

		override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
			println("Received text message from ${session.remoteAddress}, session ${session.id}")
			println("Message: ${message.payload}")
			textSession.forEach {
				it.value.sendMessage(message)
			}
		}

		override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
			println("Wrong datatype sent to Text socket from client!")
		}

		override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
			println("User with address: ${session.remoteAddress} disconnected from socket with ID: ${session.id}")
			textSession.remove(session.id)
		}

	}

	inner class SocketBinaryHandler : BinaryWebSocketHandler() {

		override fun afterConnectionEstablished(session: WebSocketSession) {
			println("User with address: ${session.remoteAddress} connected to socket with ID: ${session.id}")
			binarySession[session.id] = session
			session.binaryMessageSizeLimit = 10000000 //set for larger image blobs
		}

		override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
			println("Received binary message from ${session.remoteAddress}, session ${session.id}")
			binarySession.forEach {
				it.value.sendMessage(message)
			}
		}

		override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
			println("Wrong datatype sent to Binary socket from client!")
		}

		override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
			println("User with address: ${session.remoteAddress} disconnected from socket with ID: ${session.id}")
			binarySession.remove(session.id)
		}
	}
}