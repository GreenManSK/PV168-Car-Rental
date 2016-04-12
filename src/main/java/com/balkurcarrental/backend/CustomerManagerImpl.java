package com.balkurcarrental.backend;

import com.balkurcarrental.common.DBUtils;
import com.balkurcarrental.common.EntityNotFoundException;
import com.balkurcarrental.common.InvalidEntityException;
import com.balkurcarrental.common.ServiceFailureException;
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
    
    private static final Logger logger = Logger.getLogger(
            CarManagerImpl.class.getName());

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createCustomer(Customer customer) throws InvalidEntityException {
        checkDataSource();
        validate(customer);
        
        if (customer.getId() != null) {
            throw new IllegalArgumentException("Customer id is already set");
        }

        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "INSERT INTO customer (name, surname, phone_number) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );            

            st.setString(1, customer.getName());
            st.setString(2, customer.getSurname());
            st.setString(3, customer.getPhoneNumber());

            int addedRows = st.executeUpdate();
            DBUtils.checkUpdatesCount(addedRows, customer, DBUtils.Operation.INSERT);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            customer.setId(id);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting customer " + customer + " into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } catch (EntityNotFoundException ex) {
            Logger.getLogger(CustomerManagerImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }    
        
    }

    @Override
    public Customer getCustomerById(Long id) throws EntityNotFoundException {
        checkDataSource();
        
        if (id == null) {
            throw new IllegalArgumentException(
                    "Trying to retrive customer with null id");
        }
        
        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, name, surname, phone_number FROM customer WHERE id = ?"
            );

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            Customer customer = executeQueryForSingleCustomer(st);
            if (customer != null) {
                return customer;
            } else {
                throw new EntityNotFoundException(
                        "Customer with id " + id + " was not found in database.");
            }
        } catch (SQLException ex) {
            String msg = "Error when retrieving customer with id " + id;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void updateCustomer(Customer customer) throws InvalidEntityException, 
            EntityNotFoundException {
        checkDataSource();
        validate(customer);
        
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer id is null");
        }

        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "UPDATE customer SET name = ?, surname = ?, phone_number = ? WHERE id = ?"
            );            

            st.setString(1, customer.getName());
            st.setString(2, customer.getSurname());
            st.setString(3, customer.getPhoneNumber());
            st.setLong(4, customer.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, customer, DBUtils.Operation.UPDATE);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating customer " + customer + " in db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void deleteCustomer(Customer customer) throws EntityNotFoundException {
        checkDataSource();
        
        if (customer == null) {
            throw new IllegalArgumentException("Customer is null");
        }
        if (customer.getId() == null) {
            throw new IllegalArgumentException("Customer id is null");
        }
        
        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "DELETE FROM customer WHERE id = ?"
            );

            st.setLong(1, customer.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, customer, DBUtils.Operation.DELETE);
        } catch (SQLException ex) {
            String msg = "Error when deleting customer " + customer + " from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Customer> findAllCustomers() {
        checkDataSource();

        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, name, surname, phone_number FROM customer"
            );
            return executeQueryForMultipleCustomers(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving all customers from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Customer> findCustomersByName(String name) {
        checkDataSource();

        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }

        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, name, surname, phone_number FROM customer WHERE name = ?"
            );
            st.setString(1, name);

            return executeQueryForMultipleCustomers(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving customers by name from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Customer> findCustomersBySurname(String surname) {
        checkDataSource();

        if (surname == null) {
            throw new IllegalArgumentException("Surname is null");
        }

        Connection connection = null;
        PreparedStatement st = null;
        
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, name, surname, phone_number FROM customer WHERE surname = ?"
            );
            st.setString(1, surname);

            return executeQueryForMultipleCustomers(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving customers by surname from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }    
    
    private static Customer resultSetToCustomer(ResultSet rs) throws SQLException {
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
    
    static Customer executeQueryForSingleCustomer(PreparedStatement st) throws
            SQLException, EntityNotFoundException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Customer customer = resultSetToCustomer(rs);

            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal error: More customers with the same id found");
            }

            return customer;
        } else {
            return null;
        }
    }
    
     static List<Customer> executeQueryForMultipleCustomers(PreparedStatement st) throws
            SQLException {
        ResultSet rs = st.executeQuery();
        List<Customer> result = new ArrayList<>();
        while (rs.next()) {
            result.add(resultSetToCustomer(rs));
        }
        return result;
    }

}
