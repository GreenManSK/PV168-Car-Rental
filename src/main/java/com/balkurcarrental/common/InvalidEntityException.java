package com.balkurcarrental.common;

/**
 * Invalid entity exception
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class InvalidEntityException extends RuntimeException {

    public InvalidEntityException(String msg) {
        super(msg);
    }

    public InvalidEntityException(Throwable cause) {
        super(cause);
    }

    public InvalidEntityException(String message, Throwable cause) {
        super(message, cause);
    }

}
