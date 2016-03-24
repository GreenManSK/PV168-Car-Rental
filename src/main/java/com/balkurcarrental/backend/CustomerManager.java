package com.balkurcarrental.backend;

import com.balkurcarrental.common.InvalidEntityException;
import com.balkurcarrental.common.EntityNotFoundException;
import com.balkurcarrental.common.ServiceFailureException;
import java.util.List;

/**
 * Interface for customer manager
 *
 * @author Šimon Baláž [433272], Lukáš Kurčík [445742]
 */
public interface CustomerManager {

    /**
     * Stores new customer into database. Id for new grave is automatically
     * generated and stored into id attribute.
     *
     * @param customer customer to be created
     * @throws com.balkurcarrental.common.InvalidEntityException
     * when name or surname is null, customer with same name and surname is
     * already created or phone number is invalid
     * @throws IllegalArgumentException when customer is null, or customer has
     * already assigned id.
     * @throws ServiceFailureException when db operation fails.
     */
    void createCustomer(Customer customer) throws InvalidEntityException;

    /**
     * Returns customer with given id.
     *
     * @param id primary key for requested customer
     * @return customer with given primary key or null if customer doesn't exist
     * @throws IllegalArgumentException when given id is null.
     * @throws com.balkurcarrental.common.EntityNotFoundException
     * when entity is not found in the database
     * @throws ServiceFailureException when db operation fails.
     */
    Customer getCustomerById(Long id) throws EntityNotFoundException;

    /**
     * Updates customer in database.
     *
     * @param customer updated customer to be stored into database.
     * @throws IllegalArgumentException when customer is null, or customer has
     * null id.
     * @throws com.balkurcarrental.common.InvalidEntityException
     * when name or surname is null, customer with same name and surname is
     * already created or phone number is invalid
     * @throws com.balkurcarrental.common.EntityNotFoundException
     * when entity is not found in the database
     * @throws ServiceFailureException when db operation fails.
     */
    void updateCustomer(Customer customer) throws InvalidEntityException, EntityNotFoundException;

    /**
     * Deletes customer from database.
     *
     * @param customer customer to be deleted from db.
     * @throws IllegalArgumentException when customer is null, or customer has
     * null id.
     * @throws com.balkurcarrental.common.EntityNotFoundException
     * when entity is not found in the database
     * @throws ServiceFailureException when db operation fails.
     */
    void deleteCustomer(Customer customer) throws EntityNotFoundException;

    /**
     * Returns list of all customers in the database.
     *
     * @return list of all customers in database.
     * @throws ServiceFailureException when db operation fails.
     */
    List<Customer> findAllCustomers();

    /**
     * Returns list of all customers with given name in the database
     *
     * @param name Name to search for
     * @return list of all customers with given name in the database
     * @throws IllegalArgumentException when name is null
     * @throws ServiceFailureException when db operation fails.
     */
    List<Customer> findCustomersByName(String name);

    /**
     * Returns list of all customers with given surname in the database
     *
     * @param surname Surname to search for
     * @return list of all customers with given surname in the database
     * @throws IllegalArgumentException when surname is null
     * @throws ServiceFailureException when db operation fails.
     */
    List<Customer> findCustomersBySurname(String surname);
}
