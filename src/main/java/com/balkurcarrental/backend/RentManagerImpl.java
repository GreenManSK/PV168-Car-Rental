package com.balkurcarrental.backend;

import com.balkurcarrental.backend.exceptions.EntityNotFoundException;
import com.balkurcarrental.backend.exceptions.InvalidEntityException;
import com.balkurcarrental.backend.exceptions.ServiceFailureException;
import java.util.List;

/**
 * Implementation of rent manager
 *
 * @author Lukáš Kurčík [445742]
 */
public class RentManagerImpl implements RentManager {

    @Override
    public void createRent(Rent rent) throws InvalidEntityException, ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Rent getRentById(Long id) throws EntityNotFoundException, ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateRent(Rent rent) throws InvalidEntityException, EntityNotFoundException, ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteRent(Rent rent) throws EntityNotFoundException, ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Rent> findAllRents() throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Rent> findRentsForCustomer(Customer customer) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Rent> findRentsForCar(Car car) throws ServiceFailureException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
