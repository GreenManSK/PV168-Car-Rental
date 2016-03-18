package com.balkurcarrental.backend.exceptions;

/**
 * Entity not found exception
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class EntityNotFoundException extends Exception {

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
