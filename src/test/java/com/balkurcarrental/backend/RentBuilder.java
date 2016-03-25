package com.balkurcarrental.backend;

import java.time.LocalDate;

/**
 * This is builder for the {@link Rent} class to make tests better readable.
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class RentBuilder {
    
    private Long id;
    private Customer customer;
    private Car car;
    private int pricePerDay;
    private LocalDate beginningDate;
    private LocalDate expectedReturnDate;
    private LocalDate realReturnDate;
    
    public RentBuilder id(Long id) {
        this.id = id;
        return this;
    }
    
    public RentBuilder customer(Customer customer) {
        this.customer = customer;
        return this;
    }
    
    public RentBuilder car(Car car) {
        this.car = car;
        return this;
    }
    
    public RentBuilder pricePerDay(int pricePerDay) {
        this.pricePerDay = pricePerDay;
        return this;
    }
    
    public RentBuilder beginningDate(LocalDate beginningDate) {
        this.beginningDate = beginningDate;
        return this;
    }
    
    public RentBuilder expectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
        return this;
    }
    
    public RentBuilder realReturnDate(LocalDate realReturnDate) {
        this.realReturnDate = realReturnDate;
        return this;
    }
    
    public Rent build() {
        Rent rent = new Rent();
        rent.setId(id);
        rent.setBeginningDate(beginningDate);
        rent.setCar(car);
        rent.setCustomer(customer);
        rent.setExpectedReturnDate(expectedReturnDate);
        rent.setPricePerDay(pricePerDay);
        rent.setRealReturnDate(realReturnDate);
        return rent;
    }
    
}
