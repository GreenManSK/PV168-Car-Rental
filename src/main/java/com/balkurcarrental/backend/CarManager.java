package com.balkurcarrental.backend;

import com.balkurcarrental.backend.exceptions.InvalidEntityException;
import com.balkurcarrental.backend.exceptions.EntityNotFoundException;
import java.util.List;

/**
 * Interface for car manager
 *
 * @author Šimon Baláž [433272], Lukáš Kurčík [445742]
 */
public interface CarManager {

    /**
     * Stores new car into database. Id for new car is automatically generated
     * and stored into id attribute.
     *
     * @param car Car to be created
     * @throws com.balkurcarrental.backend.exceptions.InvalidEntityException
     * when brand or registrationNumber is null or car with same
     * registrationNumber is already in the database
     * @throws IllegalArgumentException when customer is null, or customer has
     * already assigned id.
     */
    void createCar(Car car) throws InvalidEntityException;

    /**
     * Returns car with given id.
     *
     * @param id primary key for requested car
     * @return car with given primary key or null if car doesn't exist
     * @throws com.balkurcarrental.backend.exceptions.EntityNotFoundException
     * when entity is not found in the database
     * @throws IllegalArgumentException when given id is null.
     */
    Car getCarById(Long id) throws EntityNotFoundException;

    /**
     * Updates car in database.
     *
     * @param car updated car to be stored into database.
     * @throws IllegalArgumentException when car is null, or car has null id.
     * @throws com.balkurcarrental.backend.exceptions.EntityNotFoundException
     * when entity is not found in the database
     * @throws com.balkurcarrental.backend.exceptions.InvalidEntityException
     * when brand or registrationNumber is null or car with same
     * registrationNumber is already in the database
     */
    void updateCar(Car car) throws InvalidEntityException, EntityNotFoundException;

    /**
     * Deletes car from database.
     *
     * @param car car to be deleted from db.
     * @throws IllegalArgumentException when car is null, or car has null id.
     * @throws com.balkurcarrental.backend.exceptions.EntityNotFoundException
     * when entity is not found in the database
     */
    void deleteCar(Car car) throws EntityNotFoundException;

    /**
     * Returns list of all car in the database.
     *
     * @return list of all car in database.
     */
    List<Car> findAllCars();

    /**
     * Returns list of all cars with given brand in the database
     *
     * @param brand Brand to search for
     * @return list of all cars with given brand in the database
     * @throws IllegalArgumentException when brand is null
     */
    List<Car> findCarsByBrand(String brand);
}
