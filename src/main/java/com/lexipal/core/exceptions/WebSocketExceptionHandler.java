package com.lexipal.core.exceptions;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class WebSocketExceptionHandler
{
    @MessageExceptionHandler(ResourceNotFoundException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, Object> handleResourceNotFoundException(ResourceNotFoundException ex)
    {
        return buildErrorResponse(404, ex.getMessage());
    }

    @MessageExceptionHandler(UnauthorizedActionException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, Object> handleUnauthorizedActionException(UnauthorizedActionException ex)
    {
        return buildErrorResponse(403, ex.getMessage());
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public Map<String, Object> handleIllegalArgumentException(IllegalArgumentException ex)
    {
        return buildErrorResponse(400, ex.getMessage());
    }

    private Map<String, Object> buildErrorResponse(int status, String message)
    {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("status", status);
        return response;
    }
}
