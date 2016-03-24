package com.balkurcarrental.backend;

import com.balkurcarrental.common.EntityNotFoundException;
import com.balkurcarrental.common.InvalidEntityException;
import com.balkurcarrental.common.ServiceFailureException;
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
     * @throws IllegalArgumentException when rent is null, or rent has already
     * assigned id.
     * @throws ServiceFailureException when db operation fails.
     * @throws com.balkurcarrental.common.InvalidEntityException
     * when expectedRentDays or realRentDays is <= 0, beginningOfRent is null,
     * or car is already rented in this time. When customer is invalid or when
     * car is invalid
     */
    void createRent(Rent rent) throws InvalidEntityException;

    /**
     * Returns rent with given id.
     *
     * @param id primary key for requested rent
     * @return rent with given primary key or null if rent doesn't exist
     * @throws com.balkurcarrental.common.EntityNotFoundException
     * when entity is not found in the database
     * @throws ServiceFailureException when db operation fails.
     * @throws IllegalArgumentException when given id is null.
     */
    Rent getRentById(Long id) throws EntityNotFoundException;

    /**
     * Updates rent in database.
     *
     * @param rent updated rent to be stored into database.
     * @throws IllegalArgumentException when rent is null, or rent has null id.
     * @throws com.balkurcarrental.common.EntityNotFoundException
     * when entity is not found in the database
     * @throws ServiceFailureException when db operation fails.
     * @throws com.balkurcarrental.common.InvalidEntityException
     * when expectedRentDays or realRentDays is <= 0, beginningOfRent is null,
     * or car is already rented in this time. When customer is invalid or when
     * car is invalid.
     */
    void updateRent(Rent rent) throws InvalidEntityException, EntityNotFoundException;

    /**
     * Deletes rent from database.
     *
     * @param rent rent to be deleted from db.
     * @throws IllegalArgumentException when rent is null, or rent has null id.
     * @throws com.balkurcarrental.common.EntityNotFoundException
     * when entity is not found in the database
     * @throws ServiceFailureException when db operation fails.
     */
    void deleteRent(Rent rent) throws EntityNotFoundException;

    /**
     * Returns list of all rents in the database.
     *
     * @return list of all rents in database.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Rent> findAllRents();

    /**
     * Returns list of all rents made by given customer in the database
     *
     * @param customer Customer to search for
     * @return list of all rents made by given customer in the database
     * @throws IllegalArgumentException when customer is null or has null id
     * @throws ServiceFailureException when db operation fails.
     */
    List<Rent> findRentsForCustomer(Customer customer);

    /**
     * Returns list of all rents of given car in the database
     *
     * @param car Car to search for
     * @return list of all rents of given car in the database
     * @throws IllegalArgumentException when car is null or has null id
     * @throws ServiceFailureException when db operation fails.
     */
    List<Rent> findRentsForCar(Car car);
}
