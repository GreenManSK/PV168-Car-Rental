package com.balkurcarrental.backend;

import java.util.Date;
import java.util.Objects;

/**
 * This entity class represents rent. Rent have price per day, date for
 * beginning of rent, number of expected rent days and number of real rent days.
 * Rent have always one customer and one car.
 *
 * @author Šimon Baláž [433272], Lukáš Kurčík [445742]
 */
public class Rent {

    private Long id;
    private Customer customer;
    private Car car;
    private int pricePerDay;
    private Date beginningOfRent;
    private int expectedRentDays;
    private int realRentDays;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public int getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(int pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public Date getBeginningOfRent() {
        return beginningOfRent;
    }

    public void setBeginningOfRent(Date beginningOfRent) {
        this.beginningOfRent = beginningOfRent;
    }

    public int getExpectedRentDays() {
        return expectedRentDays;
    }

    public void setExpectedRentDays(int expectedRentDays) {
        this.expectedRentDays = expectedRentDays;
    }

    public int getRealRentDays() {
        return realRentDays;
    }

    public void setRealRentDays(int realRentDays) {
        this.realRentDays = realRentDays;
    }

    @Override
    public String toString() {
        return "Rent{"
                + "id=" + id
                + ", customer=" + customer.toString()
                + ", car=" + car.toString()
                + ", pricePerDay=" + pricePerDay
                + ", beginningOfRent=" + beginningOfRent.toString()
                + ", expectedRentDays=" + expectedRentDays
                + ", realRentDays=" + realRentDays
                + "}";
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
        final Rent other = (Rent) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

}
