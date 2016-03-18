package com.balkurcarrental.backend;

import com.balkurcarrental.backend.exceptions.InvalidCarException;
import com.balkurcarrental.backend.exceptions.InvalidCustomerException;
import com.balkurcarrental.backend.exceptions.InvalidRentException;
import java.util.List;

/**
 * Interface for rent manager
 *
 * @author Šimon Baláž [433272], Lukáš Kurčík [445742]
 */
public interface RentManager {

    /**
     * Stores new rent into database. Id for new car is automatically generated
     * and stored into id attribute.
     *
     * @param rent Rent to be created
     * @throws com.balkurcarrental.backend.exceptions.InvalidRentException when
     * expectedRentDays or realRentDays is <= 0, beginningOfRent is null, or car is already rented in this time
     * @throws com.balkurcarrental.backend.exceptions.InvalidCustomerException when customer is invalid
     * @throws com.balkurcarrental.backend.exceptions.InvalidCarException when car is
     * invalid
     * @throws IllegalArgumentException when rent is null, or rent has
     * already assigned id.
     */
    void createRent(Rent rent) throws InvalidRentException, InvalidCustomerException, InvalidCarException;

    /**
     * Returns rent with given id.
     *
     * @param id primary key for requested rent
     * @return rent with given primary key or null if rent doesn't exist
     * @throws IllegalArgumentException when given id is null.
     */
    Rent getRentById(Long id);

    /**
     * Updates rent in database.
     *
     * @param rent updated rent to be stored into database.
     * @throws IllegalArgumentException when rent is null, or rent has null id.
     * @throws com.balkurcarrental.backend.exceptions.InvalidRentException when
     * expectedRentDays or realRentDays is <= 0, beginningOfRent is null, or car is already rented in this time
     * @throws com.balkurcarrental.backend.exceptions.InvalidCustomerException when customer is invalid
     * @throws com.balkurcarrental.backend.exceptions.InvalidCarException when car is
     * invalid
     */
    void updateRent(Rent rent) throws InvalidRentException, InvalidCustomerException, InvalidCarException;

    /**
     * Deletes rent from database.
     *
     * @param rent rent to be deleted from db.
     * @throws IllegalArgumentException when rent is null, or rent has null id.
     */
    void deleteRent(Rent rent);

    /**
     * Returns list of all rents in the database.
     *
     * @return list of all rents in database.
     */
    List<Rent> findAllRents();

    /**
     * Returns list of all rents made by given customer in the database
     *
     * @param customer Customer to search for
     * @return list of all rents made by given customer in the database
     * @throws IllegalArgumentException when customer is null
     */
    List<Rent> findRentsForCustomer(Customer customer);

    /**
     * Returns list of all rents of given car in the database
     *
     * @param car Car to search for
     * @return list of all rents of given car in the database
     * @throws IllegalArgumentException when car is null
     */
    List<Rent> findRentsForCar(Car car);
}
