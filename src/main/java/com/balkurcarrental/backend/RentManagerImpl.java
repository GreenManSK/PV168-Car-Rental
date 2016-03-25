package com.balkurcarrental.backend;

import com.balkurcarrental.common.DBUtils;
import com.balkurcarrental.common.EntityNotFoundException;
import com.balkurcarrental.common.InvalidEntityException;
import com.balkurcarrental.common.ServiceFailureException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Implementation of rent manager
 *
 * @author Lukáš Kurčík [445742]
 */
public class RentManagerImpl implements RentManager {

    private static final Logger logger = Logger.getLogger(
            RentManagerImpl.class.getName());

    private DataSource dataSource;
    private CarManager carManager;
    private CustomerManager customerManger;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    public void setCarManager(CarManager carManager) {
        this.carManager = carManager;
    }

    private void checkCarManager() {
        if (carManager == null) {
            throw new IllegalStateException("CarManager is not set");
        }
    }

    public void setCustomerManager(CustomerManager customerManger) {
        this.customerManger = customerManger;
    }

    private void checkCustomerManager() {
        if (customerManger == null) {
            throw new IllegalStateException("CustomerManager is not set");
        }
    }

    @Override
    public void createRent(Rent rent) throws InvalidEntityException {
        checkDataSource();
        validate(rent);
        if (rent.getId() != null) {
            throw new IllegalArgumentException("rent id is already set");
        }

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "INSERT INTO rent (customer_id, car_id, price_per_day, beginning_date, expected_return_date, real_return_date) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            if (rentIdForCar(connection, rent.getCar(), rent.getBeginningDate(), rent.
                    getRealReturnDate()) != null) {
                throw new InvalidEntityException(
                        "Car is already rented in this time of rent " + rent);
            }

            st.setLong(1, rent.getCustomer().getId());
            st.setLong(2, rent.getCar().getId());
            st.setInt(3, rent.getPricePerDay());
            st.setDate(4, toSqlDate(rent.getBeginningDate()));
            st.setDate(5, toSqlDate(rent.getExpectedReturnDate()));
            st.setDate(6, toSqlDate(rent.getRealReturnDate()));

            int addedRows = st.executeUpdate();
            DBUtils.checkUpdatesCount(addedRows, rent, DBUtils.Operation.INSERT);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            rent.setId(id);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting rent " + rent + " into db";
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
    public Rent getRentById(Long id) throws EntityNotFoundException {
        checkDataSource();
        checkCarManager();
        checkCustomerManager();
        if (id == null) {
            throw new IllegalArgumentException(
                    "Trying to retrive rent with null id");
        }

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT * FROM rent WHERE id = ?"
            );

            st.setLong(1, id);
            ResultSet rs = st.executeQuery();

            Rent rent = executeQueryForSingleRent(st);
            if (rent != null) {
                return rent;
            } else {
                throw new EntityNotFoundException(
                        "Rent with id " + id + " was not found in database.");
            }
        } catch (SQLException ex) {
            String msg = "Error when retrieving rent with id " + id;
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void updateRent(Rent rent) throws InvalidEntityException,
            EntityNotFoundException {
        checkDataSource();
        validate(rent);
        if (rent.getId() == null) {
            throw new IllegalArgumentException("rent id is null");
        }

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            st = connection.prepareStatement(
                    "UPDATE rent SET customer_id = ?, car_id = ?, price_per_day = ?, beginning_date = ?, expected_return_date = ?, real_return_date = ? WHERE id = ?"
            );

            Long rentForCar = rentIdForCar(connection, rent.getCar(), rent.
                    getBeginningDate(), rent.getRealReturnDate());
            if (rentForCar != null && !rent.getId().equals(rentForCar)) {
                throw new InvalidEntityException(
                        "Car is already rented in this time of rent " + rent);
            }

            st.setLong(1, rent.getCustomer().getId());
            st.setLong(2, rent.getCar().getId());
            st.setInt(3, rent.getPricePerDay());
            st.setDate(4, toSqlDate(rent.getBeginningDate()));
            st.setDate(5, toSqlDate(rent.getExpectedReturnDate()));
            st.setDate(6, toSqlDate(rent.getRealReturnDate()));
            st.setLong(7, rent.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, rent, DBUtils.Operation.UPDATE);
            connection.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating rent " + rent + " in db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(connection);
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public void deleteRent(Rent rent) throws EntityNotFoundException {
        checkDataSource();
        if (rent == null) {
            throw new IllegalArgumentException("rent is null");
        }
        if (rent.getId() == null) {
            throw new IllegalArgumentException("rent id is null");
        }

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "DELETE FROM rent WHERE id = ?"
            );

            st.setLong(1, rent.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, rent, DBUtils.Operation.DELETE);
        } catch (SQLException ex) {
            String msg = "Error when deleting rent " + rent + " from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Rent> findAllRents() {
        checkDataSource();
        checkCarManager();
        checkCustomerManager();

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT * FROM rent"
            );
            return executeQueryForMultipleRents(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving all rents from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Rent> findRentsForCustomer(Customer customer) {
        checkDataSource();
        checkCarManager();
        checkCustomerManager();

        if (customer == null) {
            throw new IllegalArgumentException("customer is null");
        }
        if (customer.getId() == null) {
            throw new IllegalArgumentException("customer id is null");
        }

        Connection connection = null;
        PreparedStatement st = null;
        try {
            connection = dataSource.getConnection();
            st = connection.prepareStatement(
                    "SELECT * FROM rent WHERE customer_id = ?"
            );
            st.setLong(1, customer.getId());
            return executeQueryForMultipleRents(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving rents for customer " + customer + " from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    @Override
    public List<Rent> findRentsForCar(Car car) {
        checkDataSource();
        checkCarManager();
        checkCustomerManager();

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
                    "SELECT * FROM rent WHERE car_id = ?"
            );
            st.setLong(1, car.getId());
            return executeQueryForMultipleRents(st);
        } catch (SQLException ex) {
            String msg = "Error when retrieving rents for car " + car + " from db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(connection, st);
        }
    }

    Rent executeQueryForSingleRent(PreparedStatement st) throws
            SQLException, EntityNotFoundException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Rent rent = resultSetToRent(rs);

            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal error: More rent with the same id found");
            }

            return rent;
        } else {
            return null;
        }
    }

    List<Rent> executeQueryForMultipleRents(PreparedStatement st) throws
            SQLException {
        ResultSet rs = st.executeQuery();
        List<Rent> result = new ArrayList<>();
        while (rs.next()) {
            result.add(resultSetToRent(rs));
        }
        return result;
    }

    private static void validate(Rent rent) throws IllegalArgumentException,
            InvalidEntityException {
        if (rent == null) {
            throw new IllegalArgumentException("rent is null");
        }

        if (rent.getCustomer() == null) {
            throw new InvalidEntityException("rent customer is null");
        }
        if (rent.getCustomer().getId() == null) {
            throw new InvalidEntityException("rent customer id is null");
        }

        if (rent.getCar() == null) {
            throw new InvalidEntityException("rent car is null");
        }
        if (rent.getCar().getId() == null) {
            throw new InvalidEntityException("rent car id is null");
        }

        if (rent.getPricePerDay() <= 0) {
            throw new InvalidEntityException("rent price per day is <= 0");
        }

        if (rent.getBeginningDate() == null) {
            throw new InvalidEntityException("rent beginning date is null");
        }

        if (rent.getExpectedReturnDate() != null && rent.getExpectedReturnDate().
                isBefore(rent.getBeginningDate())) {
            throw new InvalidEntityException("rent expected return date is before beginning date");
        }

        if (rent.getRealReturnDate() != null && rent.getRealReturnDate().
                isBefore(rent.getBeginningDate())) {
            throw new InvalidEntityException("rent real return date is before beginning date");
        }
    }

    private Rent resultSetToRent(ResultSet rs) throws SQLException {
        Rent rent = new Rent();

        rent.setId(rs.getLong("id"));
        rent.setCustomer(customerManger.getCustomerById(rs.
                getLong("customer_id")));
        rent.setCar(carManager.getCarById(rs.getLong("car_id")));
        rent.setPricePerDay(rs.getInt("price_per_day"));
        rent.setBeginningDate(toLocalDate(rs.getDate("beginning_date")));
        rent.setExpectedReturnDate(toLocalDate(rs.
                getDate("expected_return_date")));
        rent.setRealReturnDate(toLocalDate(rs.getDate("real_return_date")));

        return rent;
    }

    private static Long rentIdForCar(Connection connection, Car car, LocalDate beginingDate, LocalDate returnDate) {
        try (PreparedStatement st = connection.prepareStatement(
                "SELECT id FROM rent WHERE (? <= real_return_date OR real_return_date IS NULL) AND (? >= beginning_date OR ? IS NULL) AND car_id = ?"
        )) {

            st.setDate(1, toSqlDate(beginingDate));
            st.setDate(2, toSqlDate(returnDate));
            st.setDate(3, toSqlDate(returnDate));
            st.setLong(4, car.getId());
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return rs.getLong("id");
            }
            return null;

        } catch (SQLException ex) {
            Logger.getLogger(CarManagerImpl.class
                    .getName()).log(Level.SEVERE,
                            "Error when checking if car is rented", ex);
            throw new ServiceFailureException(
                    "Error when checking if car is rented", ex);
        }
    }

    private static Date toSqlDate(LocalDate localDate) {
        return localDate == null ? null : Date.valueOf(localDate);
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

}
