package com.balkurcarrental.backend.exceptions;

/**
 * Invalid car exception
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class InvalidCarException extends Exception {

    public InvalidCarException(String msg) {
        super(msg);
    }

    public InvalidCarException(Throwable cause) {
        super(cause);
    }

    public InvalidCarException(String message, Throwable cause) {
        super(message, cause);
    }
}
