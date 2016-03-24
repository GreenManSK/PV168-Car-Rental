package com.balkurcarrental.common;

/**
 * Entity not found exception
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String msg) {
        super(msg);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
