package com.balkurcarrental.backend;

import com.balkurcarrental.backend.exceptions.InvalidCarException;
import com.balkurcarrental.backend.exceptions.InvalidCustomerException;
import com.balkurcarrental.backend.exceptions.InvalidRentException;
import java.util.List;

/**
 * Implementation of rent manager
 *
 * @author Lukáš Kurčík [445742]
 */
public class RentManagerImpl implements RentManager {

    @Override
    public void createRent(Rent rent) throws InvalidRentException, InvalidCustomerException, InvalidCarException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Rent getRentById(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateRent(Rent rent) throws InvalidRentException, InvalidCustomerException, InvalidCarException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteRent(Rent rent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Rent> findAllRents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Rent> findRentsForCustomer(Customer customer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Rent> findRentsForCar(Car car) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
