package com.radovicdanilo.myplace.config.sockets

import com.radovicdanilo.myplace.config.security.service.TokenService
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import java.security.Principal

@Configuration
class WebSocketSecurityConfig(
    private val tokenService: TokenService,
) : WebSocketMessageBrokerConfigurer {

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val accessor = StompHeaderAccessor.wrap(message)
                if (accessor.command != StompCommand.CONNECT) return message

                val token = accessor.getNativeHeader("Authorization")?.get(0)?.substringAfter("Bearer ")
                if (token == null) return message

                try {
                    accessor.user = Principal { tokenService.parseToken(token).subject }
                } catch (_: Exception) {
                    throw AuthenticationCredentialsNotFoundException("Invalid token")
                }
                return message
            }
        })
    }
}
