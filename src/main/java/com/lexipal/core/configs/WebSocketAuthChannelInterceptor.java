package com.lexipal.core.configs;

import com.lexipal.core.utils.JwtAuthConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor
{
    private final JwtDecoder jwtDecoder;
    private final JwtAuthConverter jwtAuthConverter;

    @Autowired
    public WebSocketAuthChannelInterceptor(final JwtDecoder jwtDecoder,
                                           final JwtAuthConverter jwtAuthConverter)
    {
        this.jwtDecoder = jwtDecoder;
        this.jwtAuthConverter = jwtAuthConverter;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT)
        {
            return message;
        }

        String token = extractBearerToken(accessor);
        Jwt jwt = jwtDecoder.decode(token);
        AbstractAuthenticationToken convertedAuthentication = jwtAuthConverter.convert(jwt);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                convertedAuthentication.getAuthorities(),
                jwt.getSubject()
        );

        accessor.setUser(authentication);
        return message;
    }

    private String extractBearerToken(StompHeaderAccessor accessor)
    {
        String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
        if (authorizationHeader == null || authorizationHeader.isBlank())
        {
            authorizationHeader = accessor.getFirstNativeHeader("authorization");
        }

        if (authorizationHeader == null || authorizationHeader.isBlank())
        {
            throw new BadCredentialsException("Missing Authorization header");
        }

        if (!authorizationHeader.startsWith("Bearer "))
        {
            throw new BadCredentialsException("Authorization header must use Bearer token");
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isBlank())
        {
            throw new BadCredentialsException("Bearer token is required");
        }

        return token;
    }
}
