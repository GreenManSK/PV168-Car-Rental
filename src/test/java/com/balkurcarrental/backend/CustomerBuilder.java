package com.balkurcarrental.backend;

/**
 * This is builder for the {@link Customer} class to make tests better readable.
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class CustomerBuilder {

    private Long id;
    private String name;
    private String surname;
    private String phoneNumber;

    public CustomerBuilder id(Long id) {
        this.id = id;
        return this;

    }

    public CustomerBuilder name(String name) {
        this.name = name;
        return this;
    }

    public CustomerBuilder surname(String surname) {
        this.surname = surname;
        return this;
    }

    public CustomerBuilder phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }
    
    public Customer build() {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setSurname(surname);
        customer.setPhoneNumber(phoneNumber);
        return customer;
    }
}
