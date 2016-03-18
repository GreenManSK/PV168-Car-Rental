package com.balkurcarrental.backend;

import java.util.Objects;

/**
 * This entity class represents customer. Customer have name, surname and
 * phone number specified. Customer can rent zero or more cars.
 *
 * @author Šimon Baláž [433272], Lukáš Kurčík [445742]
 */
public class Customer {

    private Long id;
    private String name;
    private String surname;
    private String phoneNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "Customer{"
                + "id=" + id
                + ", name\'" + name
                + "\', surname=\'" + surname
                + "\', phoneNumber=\'" + phoneNumber
                + "\'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Customer)) {
            return false;
        }
        Customer customer = (Customer) obj;
        if (!Objects.equals(getId(), customer.getId())) {
            return false;
        }
        if (!Objects.equals(getName(), customer.getName())) {
            return false;
        }
        if (!Objects.equals(getSurname(), customer.getSurname())) {
            return false;
        }
        return Objects.equals(getPhoneNumber(), customer.getPhoneNumber());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.id);
        hash = 83 * hash + Objects.hashCode(this.name);
        hash = 83 * hash + Objects.hashCode(this.surname);
        hash = 83 * hash + Objects.hashCode(this.phoneNumber);
        return hash;
    }

}
