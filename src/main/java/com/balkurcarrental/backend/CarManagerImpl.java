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
import com.balkurcarrental.common.DBUtils.Operation;

/**
 * Implementation of car manager
 *
 * @author Lukáš Kurčík [445742]
 */
public class CarManagerImpl implements CarManager {

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
    public void createCar(Car car) throws InvalidEntityException {
        checkDataSource();
        validate(car);
        if (car.getId() != null) {
            throw new IllegalArgumentException("car id is already set");
        }

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "INSERT INTO car (brand, registration_number) VALUES (?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            if (!isRegistrationNumberUnique(connection, car)) {
                throw new InvalidEntityException(
                        "Car with same registration number found when inserting car " + car);
            }

            st.setString(1, car.getBrand());
            st.setString(2, car.getRegistrationNumber());

            int addedRows = st.executeUpdate();
            DBUtils.checkUpdatesCount(addedRows, car, Operation.INSERT);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            car.setId(id);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting car " + car + " into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } catch (EntityNotFoundException ex) {
            Logger.getLogger(CarManagerImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public Car getCarById(Long id) throws EntityNotFoundException {
        checkDataSource();
        if (id == null) {
            throw new IllegalArgumentException(
                    "Trying to retrive car with null id");
        }

        Connection connection = null;
        PreparedStatement st = null;

        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, brand, registration_number FROM car WHERE id = ?"
            );

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            Car car = executeQueryForSingleGrave(st);
            if (car != null) {
                return car;
            } else {
                throw new EntityNotFoundException(
                        "Car with id " + id + " was not found in database.");
            }
        } catch (SQLException ex) {
            String msg = "Error when retrieving car with id " + id;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void updateCar(Car car) throws InvalidEntityException,
            EntityNotFoundException {
        checkDataSource();
        validate(car);
        if (car.getId() == null) {
            throw new IllegalArgumentException("car id is null");
        }

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "UPDATE car SET brand = ?, registration_number = ? WHERE id = ?"
            );

            if (!isRegistrationNumberUnique(connection, car)) {
                throw new InvalidEntityException(
                        "Car with same registration number found when updating car " + car);
            }

            st.setString(1, car.getBrand());
            st.setString(2, car.getRegistrationNumber());
            st.setLong(3, car.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, car, Operation.UPDATE);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating car " + car + " in db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void deleteCar(Car car) throws EntityNotFoundException {
        checkDataSource();
        if (car == null) {
            throw new IllegalArgumentException("car is null");
        }
        if (car.getId() == null) {
            throw new IllegalArgumentException("car id is null");
        }
        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "DELETE FROM car WHERE id = ?"
            );

            st.setLong(1, car.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, car, Operation.DELETE);
        } catch (SQLException ex) {
            String msg = "Error when deleting car " + car + " from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Car> findAllCars() {
        checkDataSource();

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, brand, registration_number FROM car"
            );
            return executeQueryForMultipleCars(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving all cars from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Car> findCarsByBrand(String brand) {
        checkDataSource();

        if (brand == null) {
            throw new IllegalArgumentException("brand is null");
        }

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT id, brand, registration_number FROM car WHERE brand = ?"
            );
            st.setString(1, brand);

            return executeQueryForMultipleCars(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving all cars from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    static Car executeQueryForSingleGrave(PreparedStatement st) throws
            SQLException, EntityNotFoundException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Car car = resultSetToCar(rs);

            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal error: More car with the same id found");
            }

            return car;
        } else {
            return null;
        }
    }

    static List<Car> executeQueryForMultipleCars(PreparedStatement st) throws
            SQLException {
        ResultSet rs = st.executeQuery();
        List<Car> result = new ArrayList<>();
        while (rs.next()) {
            result.add(resultSetToCar(rs));
        }
        return result;
    }

    private static void validate(Car car) throws IllegalArgumentException,
            InvalidEntityException {
        if (car == null) {
            throw new IllegalArgumentException("car is null");
        }

        if (car.getBrand() == null) {
            throw new InvalidEntityException("car brand is null");
        }

        if (car.getBrand().trim().equals("")) {
            throw new InvalidEntityException("car brand is empty");
        }

        if (car.getRegistrationNumber() == null) {
            throw new InvalidEntityException("car registration number is null");
        }

        if (car.getRegistrationNumber().trim().equals("")) {
            throw new InvalidEntityException("car registration number is empty");
        }
    }

    private static Car resultSetToCar(ResultSet rs) throws SQLException {
        Car car = new Car();

        car.setId(rs.getLong("id"));
        car.setBrand(rs.getString("brand"));
        car.setRegistrationNumber(rs.getString("registration_number"));

        return car;
    }

    private static boolean isRegistrationNumberUnique(Connection connection,
            Car car) {
        try (PreparedStatement st = connection.prepareStatement(
                "SELECT id FROM car WHERE registration_number = ?"
        )) {
            st.setString(1, car.getRegistrationNumber());
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return car.getId() != null && car.getId().equals(rs.
                        getLong("id"));
            }
            return true;
        } catch (SQLException ex) {
            throw new ServiceFailureException(
                    "Error when checking uniqueness or registration number for car " + car,
                    ex);
        }
    }
}
