package com.lexipal.core.exceptions;

public class NicknameConflictException extends RuntimeException
{
    public NicknameConflictException(String nickname)
    {
        super("Nickname '" + nickname + "' is already taken");
    }
}
