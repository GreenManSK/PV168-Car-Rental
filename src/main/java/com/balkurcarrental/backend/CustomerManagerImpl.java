package com.balkurcarrental.backend;

import com.balkurcarrental.backend.exceptions.EntityNotFoundException;
import com.balkurcarrental.backend.exceptions.InvalidEntityException;
import com.balkurcarrental.backend.exceptions.ServiceFailureException;
import java.util.List;

/**
 * Implementation of customer manager
 *
 * @author Lukáš Kurčík [445742]
 */
public class CustomerManagerImpl implements CustomerManager {

    @Override
    public void createCustomer(Customer customer) throws InvalidEntityException, ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Customer getCustomerById(Long id) throws EntityNotFoundException, ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateCustomer(Customer customer) throws InvalidEntityException, EntityNotFoundException, ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteCustomer(Customer customer) throws EntityNotFoundException, ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Customer> findAllCustomers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Customer> findCustomersByName(String name) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Customer> findCustomersBySurname(String surname) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
