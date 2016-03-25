package com.balkurcarrental.backend;

import java.time.LocalDate;
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
    private LocalDate beginningDate;
    private LocalDate expectedReturnDate;
    private LocalDate realReturnDate;

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

    public LocalDate getBeginningDate() {
        return beginningDate;
    }

    public void setBeginningDate(LocalDate beginningDate) {
        this.beginningDate = beginningDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getRealReturnDate() {
        return realReturnDate;
    }

    public void setRealReturnDate(LocalDate realReturnDate) {
        this.realReturnDate = realReturnDate;
    }

    @Override
    public String toString() {
        return "Rent{"
                + "id=" + id
                + ", customer=" + customer.toString()
                + ", car=" + car.toString()
                + ", pricePerDay=" + pricePerDay
                + ", beginningOfRent=" + beginningDate
                + ", expectedRentDays=" + expectedReturnDate
                + ", realRentDays=" + realReturnDate
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
