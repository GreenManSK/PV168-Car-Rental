package com.balkurcarrental.backend;

/**
 * This is builder for the {@link Car} class to make tests better readable.
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class CarBuilder {

    private Long id;
    private String brand;
    private String registrationNumber;

    public CarBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public CarBuilder brand(String brand) {
        this.brand = brand;
        return this;
    }

    public CarBuilder registrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        return this;
    }
    
    public Car build() {
        Car car = new Car();
        car.setId(id);
        car.setBrand(brand);
        car.setRegistrationNumber(registrationNumber);
        return car;
    }
}
