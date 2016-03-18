package com.balkurcarrental.backend;

import java.util.Objects;

/**
 * This entity class represents Car. Care have brand and registrationNumber
 * specified. One car can be rented zero or more times, but not twice at the
 * same time.
 *
 * @author Šimon Baláž [433272], Lukáš Kurčík [445742]
 */
public class Car {

    private Long id;
    private String brand;
    private String registrationNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @Override
    public String toString() {
        return "Car{"
                + "id=" + id
                + ", brand=\'" + brand
                + "\', registrationNumber=\'" + registrationNumber
                + "\'}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (this.id == null && this != obj) {
            // this is a special case - two entities without id assigned yet
            // should be evaluated as non equal
            return false;
        }
        final Car other = (Car) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

}
