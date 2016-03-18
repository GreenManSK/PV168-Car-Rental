package com.balkurcarrental.backend.exceptions;

/**
 * Invalid rent exception
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class InvalidRentException extends Exception {

    public InvalidRentException(String msg) {
        super(msg);
    }

    public InvalidRentException(Throwable cause) {
        super(cause);
    }

    public InvalidRentException(String message, Throwable cause) {
        super(message, cause);
    }
}
