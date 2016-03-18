package com.balkurcarrental.backend;

import com.balkurcarrental.backend.exceptions.InvalidCarException;
import com.balkurcarrental.backend.exceptions.InvalidCustomerException;
import com.balkurcarrental.backend.exceptions.InvalidRentException;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Tests for RentManagerImpl
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class RentManagerImplTest {

    private RentManagerImpl manager;

    @Before
    public void setUp() {
        manager = new RentManagerImpl();
    }

    @Rule
    public ExpectedException exceptedException = ExpectedException.none();

    @Test(expected = InvalidRentException.class)
    public void createRentWithSetId() throws InvalidRentException, InvalidCustomerException, InvalidCarException {
        Customer customer = new Customer();
        customer.setId(4L);
        Car car = new Car();
        car.setId(4L);
        Rent rent = makeRent(customer, car, 250, new Date(2016, 3, 9), 10, 5);
        rent.setId(4L);

        exceptedException.expect(InvalidRentException.class);
        manager.createRent(rent);
    }

    @Test
    public void createRentWithInvalidCar() throws InvalidCarException, InvalidRentException, InvalidCustomerException {
        Customer customer = new Customer();
        customer.setId(5L);
        Car car = new Car();
        Rent rent = makeRent(customer, car, 250, new Date(2016, 3, 9), 10, 5);

        exceptedException.expect(InvalidCarException.class);
        manager.createRent(rent);
    }

    public void createRentsWithSameDateForOneCar() throws InvalidRentException, InvalidCustomerException, InvalidCarException {
        Customer customerLukas = new Customer();
        customerLukas.setId(6L);
        Customer customerJanko = new Customer();
        customerJanko.setId(7L);
        Car car = new Car();
        car.setId(6L);

        Date date = new Date(2016, 3, 9);
        Rent rent = makeRent(customerLukas, car, 250, date, 10, 5);

        manager.createRent(rent);

        Rent rent2 = makeRent(customerJanko, car, 250, date, 10, 5);
        exceptedException.expect(InvalidRentException.class);
        manager.createRent(rent2);
    }

    private static Rent makeRent(Customer customer, Car car, int pricePerDay, Date beginningOfRent, int expectedRentDays, int realRentDays) {
        Rent rent = new Rent();

        rent.setCustomer(customer);
        rent.setCar(car);
        rent.setPricePerDay(pricePerDay);
        rent.setBeginningOfRent(beginningOfRent);
        rent.setExpectedRentDays(expectedRentDays);
        rent.setRealRentDays(realRentDays);

        return rent;
    }

}
