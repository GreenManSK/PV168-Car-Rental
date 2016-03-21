package com.balkurcarrental.backend;

import com.balkurcarrental.backend.exceptions.EntityNotFoundException;
import com.balkurcarrental.backend.exceptions.InvalidEntityException;
import com.balkurcarrental.backend.exceptions.ServiceFailureException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Implementation of customer manager
 *
 * @author Lukáš Kurčík [445742]
 */
public class CustomerManagerImpl implements CustomerManager {
    
    private final DataSource dataSource;

    public CustomerManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createCustomer(Customer customer) throws InvalidEntityException, ServiceFailureException {
        validate(customer);
        
        if (customer.getId() != null) {
            throw new IllegalArgumentException("Customer id is already set");
        }
        
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "INSERT INTO customer (name, surname, phone_number) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                )) {
            st.setString(1, customer.getName());
            st.setString(2, customer.getSurname());
            st.setString(3, customer.getPhoneNumber());

            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert customer " + customer);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            customer.setId(getKey(keyRS, customer));
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting customer " + customer, ex);
        }
        
    }

    @Override
    public Customer getCustomerById(Long id) throws EntityNotFoundException, ServiceFailureException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to retrive customer with null id");
        }
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id, name, surname, phone_number FROM customer WHERE id = ?"
                )) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Customer customer = resultSetToCustomer(rs);

                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                            + "(source id: " + id + ", found " + customer + " and " + resultSetToCustomer(rs));

                }

                return customer;
            } else {
                throw new EntityNotFoundException("Customer with id " + id + " was not found in database.");
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving car with id " + id, ex);
        }
    }

    @Override
    public void updateCustomer(Customer customer) throws InvalidEntityException, EntityNotFoundException, ServiceFailureException {
        validate(customer);
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer id is null");
        }        

        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "UPDATE customer SET name = ?, surname = ?, phone_number = ? WHERE id = ?"
                )) {
            st.setString(1, customer.getName());
            st.setString(2, customer.getSurname());
            st.setString(2, customer.getPhoneNumber());
            st.setLong(4, customer.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Customer " + customer + " was not found in database.");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid updated rows count detected (one row should be updated): " + count + " when updating car " + customer);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when updating car " + customer, ex);
        }
    }

    @Override
    public void deleteCustomer(Customer customer) throws EntityNotFoundException, ServiceFailureException {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer id is null");
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "DELETE FROM customer WHERE id = ?"
                )) {
            st.setLong(1, customer.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Customer " + customer + " was not found in database!.");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid deleted rows count detected (one row should be updated): " + count + " when deleting car " + customer);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when deleting car " + customer, ex);
        }
    }

    @Override
    public List<Customer> findAllCustomers() {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id, name, surname, phone_number FROM customer"
                )) {

            ResultSet rs = st.executeQuery();

            List<Customer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToCustomer(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving all customers", ex);
        }
    }

    @Override
    public List<Customer> findCustomersByName(String name) throws ServiceFailureException {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id, name, surname, phone_number FROM customer WHERE name = ?"
                )) {
            st.setString(1, name);
            ResultSet rs = st.executeQuery();

            List<Customer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToCustomer(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving customer by name " + name, ex);
        }        
    }

    @Override
    public List<Customer> findCustomersBySurname(String surname) throws ServiceFailureException {
        if (surname == null) {
            throw new IllegalArgumentException("Surname is null");
        }
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id, name, surname, phone_number FROM customer WHERE surname = ?"
                )) {
            st.setString(1, surname);
            ResultSet rs = st.executeQuery();

            List<Customer> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToCustomer(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving customer by surname " + surname, ex);
        }     
    }
    
    private Long getKey(ResultSet keyRS, Customer customer) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert customer " + customer
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert customer " + customer
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert customer " + customer
                    + " - no key found");
        }
    }
    
    private Customer resultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();

        customer.setId(rs.getLong("id"));
        customer.setName(rs.getString("name"));
        customer.setSurname(rs.getString("surname"));
        customer.setPhoneNumber(rs.getString("phone_number"));

        return customer;
    }
    
    private void validate(Customer customer) throws IllegalArgumentException, InvalidEntityException {
        if (customer== null) {
            throw new IllegalArgumentException("Customer is null");
        }

        if (customer.getName() == null) {
            throw new InvalidEntityException("Name of customer is null");
        }

        if (customer.getName().trim().equals("")) {
            throw new InvalidEntityException("Name of customer is empty");
        }

        if (customer.getSurname() == null) {
            throw new InvalidEntityException("Surname of customer is null");
        }

        if (customer.getSurname().trim().equals("")) {
            throw new InvalidEntityException("Surname of customer is empty");
        }
        
        if (customer.getPhoneNumber() == null) {
            throw new InvalidEntityException("Phone number of customer is null");
        }

        if (customer.getPhoneNumber().trim().equals("")) {
            throw new InvalidEntityException("Phone number of customer is empty");
        }        
        
    }

}
