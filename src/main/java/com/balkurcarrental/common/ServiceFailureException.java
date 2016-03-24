package com.balkurcarrental.common;

/**
 * Indicates service failure.
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class ServiceFailureException extends RuntimeException {

    public ServiceFailureException(String msg) {
        super(msg);
    }

    public ServiceFailureException(Throwable cause) {
        super(cause);
    }

    public ServiceFailureException(String message, Throwable cause) {
        super(message, cause);
    }

}
