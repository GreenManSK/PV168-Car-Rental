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
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Car)) {
            return false;
        }
        Car car = (Car) obj;

        if (!Objects.equals(getId(), car.getId())) {
            return false;
        }
        if (!getBrand().equals(car.getBrand())) {
            return false;
        }
        if (!Objects.equals(getBrand(), car.getBrand())) {
            return false;
        }
        return Objects.equals(getRegistrationNumber(), car.getRegistrationNumber());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.id);
        hash = 41 * hash + Objects.hashCode(this.brand);
        hash = 41 * hash + Objects.hashCode(this.registrationNumber);
        return hash;
    }

}
