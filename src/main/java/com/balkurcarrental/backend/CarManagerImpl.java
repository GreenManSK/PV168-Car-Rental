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
 * Implementation of car manager
 *
 * @author Lukáš Kurčík [445742]
 */
public class CarManagerImpl implements CarManager {

    private final DataSource dataSource;

    public CarManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createCar(Car car) throws InvalidEntityException, ServiceFailureException {
        validate(car);
        if (car.getId() != null) {
            throw new IllegalArgumentException("car id is already set");
        }

        if (!isRegistrationNumberUnique(car)) {
            throw new InvalidEntityException("Car with same registration number found when inserting car " + car);
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "INSERT INTO car (brand, registration_number) VALUES (?,?)",
                        Statement.RETURN_GENERATED_KEYS
                )) {
            st.setString(1, car.getBrand());
            st.setString(2, car.getRegistrationNumber());

            int addedRows = st.executeUpdate();
            if (addedRows != 1) {
                throw new ServiceFailureException("Internal Error: More rows ("
                        + addedRows + ") inserted when trying to insert car " + car);
            }

            ResultSet keyRS = st.getGeneratedKeys();
            car.setId(getKey(keyRS, car));
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when inserting car " + car, ex);
        }
    }

    @Override
    public Car getCarById(Long id) throws EntityNotFoundException, ServiceFailureException {
        if (id == null) {
            throw new IllegalArgumentException("Trying to retrive car with null id");
        }
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id, brand, registration_number FROM car WHERE id = ?"
                )) {
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                Car car = resultSetToCar(rs);

                if (rs.next()) {
                    throw new ServiceFailureException(
                            "Internal error: More entities with the same id found "
                            + "(source id: " + id + ", found " + car + " and " + resultSetToCar(rs));

                }

                return car;
            } else {
                throw new EntityNotFoundException("Car with id " + id + " was not found in database.");
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving car with id " + id, ex);
        }
    }

    @Override
    public void updateCar(Car car) throws InvalidEntityException, EntityNotFoundException, ServiceFailureException {
        validate(car);
        if (car.getId() == null) {
            throw new IllegalArgumentException("car id is null");
        }

        if (!isRegistrationNumberUnique(car)) {
            throw new InvalidEntityException("Car with same registration number found when updating car " + car);
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "UPDATE car SET brand = ?, registration_number = ? WHERE id = ?"
                )) {
            st.setString(1, car.getBrand());
            st.setString(2, car.getRegistrationNumber());
            st.setLong(3, car.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Car " + car + " was not found in database.");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid updated rows count detected (one row should be updated): " + count + " when updating car " + car);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when updating car " + car, ex);
        }
    }

    @Override
    public void deleteCar(Car car) throws EntityNotFoundException, ServiceFailureException {
        if (car == null) {
            throw new IllegalArgumentException("car is null");
        }
        if (car.getId() == null) {
            throw new IllegalArgumentException("car id is null");
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "DELETE FROM car WHERE id = ?"
                )) {
            st.setLong(1, car.getId());

            int count = st.executeUpdate();
            if (count == 0) {
                throw new EntityNotFoundException("Car " + car + " was not found in database!.");
            } else if (count != 1) {
                throw new ServiceFailureException("Invalid deleted rows count detected (one row should be updated): " + count + " when deleting car " + car);
            }
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when deleting car " + car, ex);
        }
    }

    @Override
    public List<Car> findAllCars() throws ServiceFailureException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id, brand, registration_number FROM car"
                )) {

            ResultSet rs = st.executeQuery();

            List<Car> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToCar(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving all cars", ex);
        }
    }

    @Override
    public List<Car> findCarsByBrand(String brand) throws ServiceFailureException {
        if (brand == null) {
            throw new IllegalArgumentException("brand is null");
        }
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id, brand, registration_number FROM car WHERE brand = ?"
                )) {
            st.setString(1, brand);
            ResultSet rs = st.executeQuery();

            List<Car> result = new ArrayList<>();
            while (rs.next()) {
                result.add(resultSetToCar(rs));
            }
            return result;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when retrieving car by brand " + brand, ex);
        }
    }

    private void validate(Car car) throws IllegalArgumentException, InvalidEntityException {
        if (car == null) {
            throw new IllegalArgumentException("car is null");
        }

        if (car.getBrand() == null) {
            throw new InvalidEntityException("car brand is null");
        }

        if (car.getBrand().isEmpty()) {
            throw new InvalidEntityException("car brand is empty");
        }

        if (car.getRegistrationNumber() == null) {
            throw new InvalidEntityException("car registration number is null");
        }

        if (car.getRegistrationNumber().isEmpty()) {
            throw new InvalidEntityException("car registration number is empty");
        }
    }

    private Long getKey(ResultSet keyRS, Car car) throws ServiceFailureException, SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert car " + car
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new ServiceFailureException("Internal Error: Generated key"
                        + "retriving failed when trying to insert car " + car
                        + " - more keys found");
            }
            return result;
        } else {
            throw new ServiceFailureException("Internal Error: Generated key"
                    + "retriving failed when trying to insert car " + car
                    + " - no key found");
        }
    }

    private Car resultSetToCar(ResultSet rs) throws SQLException {
        Car car = new Car();

        car.setId(rs.getLong("id"));
        car.setBrand(rs.getString("brand"));
        car.setRegistrationNumber(rs.getString("registration_number"));

        return car;
    }

    private boolean isRegistrationNumberUnique(Car car) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement st = connection.prepareStatement(
                        "SELECT id FROM car WHERE registration_number = ?"
                )) {
            st.setString(1, car.getRegistrationNumber());
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return car.getId() != null && car.getId().equals(rs.getLong("id"));
            }
            return true;
        } catch (SQLException ex) {
            throw new ServiceFailureException("Error when checking uniqueness or registration number for car " + car, ex);
        }
    }
}
