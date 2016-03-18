package com.balkurcarrental.backend.exceptions;

/**
 * Invalid customer exception
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class InvalidCustomerException extends Exception {

    public InvalidCustomerException(String msg) {
        super(msg);
    }

    public InvalidCustomerException(Throwable cause) {
        super(cause);
    }

    public InvalidCustomerException(String message, Throwable cause) {
        super(message, cause);
    }

}
