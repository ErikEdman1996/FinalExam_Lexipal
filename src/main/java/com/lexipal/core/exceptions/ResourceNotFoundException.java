package com.lexipal.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException
{
  public String object, field;
  private Object value;


    public ResourceNotFoundException(String object, String field, Object value)
    {
      super(String.format("%s with %s '%s' not found'", object, field, value));
      this.object = object;
      this.field = field;
      this.value = value;
    }
}
