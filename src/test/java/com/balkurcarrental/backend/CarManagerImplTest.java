package com.balkurcarrental.backend;

import com.balkurcarrental.common.DBUtils;
import com.balkurcarrental.common.EntityNotFoundException;
import com.balkurcarrental.common.InvalidEntityException;
import com.balkurcarrental.common.ServiceFailureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.apache.derby.jdbc.EmbeddedDataSource;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.junit.After;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for CarManagerImpl
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class CarManagerImplTest {

    private static final Comparator<Car> CAR_ID_COMPARATOR = (c1, c2) -> c1.
            getId().compareTo(
                    c2.getId());

    private CarManagerImpl managerImpl;
    private DataSource dataSource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, CarManager.class.getResource(
                "createTables.sql"));
        managerImpl = new CarManagerImpl();
        managerImpl.setDataSource(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, CarManager.class.getResource(
                "dropTables.sql"));
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:carrental-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    private CarBuilder createCarBMW() {
        return new CarBuilder().id(null).brand("BMW").
                registrationNumber("AB123");
    }

    private CarBuilder createCarMercedes() {
        return new CarBuilder().id(null).brand("Mercedes").
                registrationNumber("FG789");
    }

    @Test
    public void createAndGetCar() throws InvalidEntityException,
            EntityNotFoundException {
        Car car = createCarBMW().build();
        managerImpl.createCar(car);

        Long carId = car.getId();
        assertThat(carId).isNotNull();

        assertThat(managerImpl.getCarById(carId)).isEqualTo(car).
                isNotSameAs(car).
                isEqualToComparingFieldByField(
                        car);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullCar() throws InvalidEntityException {
        managerImpl.createCar(null);
    }

    private void testCreateUnsuccessfuly(Consumer<CarBuilder> setOperation)
            throws
            InvalidEntityException {
        testCreateUnsuccessfuly(setOperation, IllegalArgumentException.class);
    }

    private void testCreateUnsuccessfuly(Consumer<CarBuilder> setOperation,
            Class<? extends Exception> exceptionClass)
            throws
            InvalidEntityException {
        CarBuilder carBuilder = createCarBMW();
        setOperation.accept(carBuilder);
        Car car = carBuilder.build();
        expectedException.expect(exceptionClass);
        managerImpl.createCar(car);
    }

    @Test
    public void createCarWithSetId() throws InvalidEntityException {
        testCreateUnsuccessfuly((cb) -> cb.id(12L));
    }

    @Test
    public void createCarWithNullBrand() throws InvalidEntityException {
        testCreateUnsuccessfuly((cb) -> cb.brand(null),
                InvalidEntityException.class);
    }

    @Test
    public void createCarWithEmptyBrand() throws InvalidEntityException {
        testCreateUnsuccessfuly((cb) -> cb.brand("   "),
                InvalidEntityException.class);
    }

    @Test
    public void createCarWithNullRegistrationNumber() throws
            InvalidEntityException {
        testCreateUnsuccessfuly((cb) -> cb.registrationNumber(null),
                InvalidEntityException.class);
    }

    @Test
    public void createCarWithEmptyRegistrationNumber() throws
            InvalidEntityException {
        testCreateUnsuccessfuly((cb) -> cb.registrationNumber("    "),
                InvalidEntityException.class);
    }

    @Test
    public void createCarWithUsedRegistrationNumber() throws
            InvalidEntityException {
        String regNumber = "ABC123";
        Car carBMW = createCarBMW().registrationNumber(regNumber).build();
        Car carPeugeot = createCarMercedes().registrationNumber(regNumber).
                build();

        managerImpl.createCar(carBMW);
        expectedException.expect(InvalidEntityException.class);
        managerImpl.createCar(carPeugeot);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCarByNullId() throws EntityNotFoundException {
        Car carById = managerImpl.getCarById(null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getCarByNotExistingId() throws EntityNotFoundException {
        Car carById = managerImpl.getCarById(1024L);
    }

    private void testUpdateSuccessfuly(Consumer<Car> updateOperation) throws
            InvalidEntityException, EntityNotFoundException {
        Car carBmw = createCarBMW().build();
        Car carPeugeot = createCarMercedes().build();
        managerImpl.createCar(carBmw);
        managerImpl.createCar(carPeugeot);

        updateOperation.accept(carBmw);
        managerImpl.updateCar(carBmw);

        assertThat(managerImpl.getCarById(carBmw.getId())).
                isEqualToComparingFieldByField(carBmw);
        assertThat(managerImpl.getCarById(carPeugeot.getId())).
                isEqualToComparingFieldByField(carPeugeot);
    }

    @Test
    public void updateCar() throws InvalidEntityException,
            EntityNotFoundException {
        testUpdateSuccessfuly((c) -> {
            c.setRegistrationNumber("ABC123");
            c.setBrand("Peugeot");
        });
    }

    @Test
    public void updateCarBrand() throws InvalidEntityException,
            EntityNotFoundException {
        testUpdateSuccessfuly((c) -> {
            c.setBrand("Peugeot");
        });
    }

    @Test
    public void updateCarRegistrationNumber() throws InvalidEntityException,
            EntityNotFoundException {
        testUpdateSuccessfuly((c) -> {
            c.setRegistrationNumber("ABC123");
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullCar() throws InvalidEntityException,
            EntityNotFoundException {
        managerImpl.updateCar(null);
    }

    @Test
    public void updateCarWithNullId() throws InvalidEntityException,
            EntityNotFoundException {
        Car car = createCarBMW().build();
        car.setBrand("BCC");
        expectedException.expect(IllegalArgumentException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithNonExistingId() throws InvalidEntityException,
            EntityNotFoundException {
        Car car = createCarBMW().id(128L).build();
        car.setBrand("BCC");
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithNullBrand() throws InvalidEntityException,
            EntityNotFoundException {
        Car car = createCarBMW().brand(null).build();
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithEmptyBrand() throws InvalidEntityException,
            EntityNotFoundException {
        Car car = createCarBMW().brand("   ").build();
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithNullRegistrationNumber() throws
            InvalidEntityException, EntityNotFoundException {
        Car car = createCarBMW().registrationNumber(null).build();
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithEmptyRegistrationNumber() throws
            InvalidEntityException, EntityNotFoundException {
        Car car = createCarBMW().registrationNumber("   ").build();
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithUsedRegistrationNumber() throws
            InvalidEntityException, EntityNotFoundException {
        String regNumber = "DDFGGG";
        Car carBMW = createCarBMW().build();
        Car carMercedes = createCarMercedes().registrationNumber(regNumber).
                build();

        managerImpl.createCar(carBMW);
        managerImpl.createCar(carMercedes);

        carBMW.setRegistrationNumber(regNumber);
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(carBMW);
    }

    @Test
    public void deleteCar() throws InvalidEntityException,
            EntityNotFoundException {
        Car carBmw = createCarBMW().build();
        Car carMercedes = createCarMercedes().build();
        managerImpl.createCar(carBmw);
        managerImpl.createCar(carMercedes);

        managerImpl.getCarById(carBmw.getId());
        managerImpl.getCarById(carMercedes.getId());

        managerImpl.deleteCar(carBmw);

        managerImpl.getCarById(carMercedes.getId());
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.getCarById(carBmw.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullCar() throws EntityNotFoundException {
        managerImpl.deleteCar(null);
    }

    @Test
    public void deleteCarWithNullId() throws EntityNotFoundException {
        Car car = createCarBMW().build();
        expectedException.expect(IllegalArgumentException.class);
        managerImpl.deleteCar(car);
    }

    @Test
    public void deleteCarWithNonExistingId() throws EntityNotFoundException {
        Car car = createCarBMW().id(128L).build();
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.deleteCar(car);
    }

    @Test
    public void findAllCars() throws InvalidEntityException {
        assertThat(managerImpl.findAllCars()).isEmpty();

        Car c1 = createCarBMW().build();
        Car c2 = createCarMercedes().build();
        managerImpl.createCar(c1);
        managerImpl.createCar(c2);

        assertThat(managerImpl.findAllCars())
                .usingFieldByFieldElementComparator()
                .containsOnly(c1, c2);
    }

    @Test
    public void findAllCarsFromEmpty() {
        assertThat(managerImpl.findAllCars()).isEmpty();
    }

    @Test
    public void findCarsByBrand() throws InvalidEntityException {
        Car c1 = createCarBMW().build();
        Car c2 = createCarMercedes().build();
        Car c3 = new CarBuilder().brand("Mercedes").registrationNumber(
                "APO888").build();
        managerImpl.createCar(c1);
        managerImpl.createCar(c2);
        managerImpl.createCar(c3);

        assertThat(managerImpl.findCarsByBrand("Mercedes"))
                .usingFieldByFieldElementComparator()
                .containsOnly(c2, c3);
    }

    @Test
    public void findCarsByNonExistingBrand() throws InvalidEntityException {
        Car c1 = createCarBMW().build();
        managerImpl.createCar(c1);
        assertThat(managerImpl.findCarsByBrand("Lenovo")).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void findCarsByNullBrand() throws InvalidEntityException {
        managerImpl.findCarsByBrand(null);
    }

    private void testExpectedServiceFailureException(
            Consumer<CarManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        managerImpl.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.accept(managerImpl))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void createCarWithSqlExceptionThrown() throws SQLException {
        Car car = createCarBMW().build();
        testExpectedServiceFailureException((m) -> m.createCar(car));
    }

    @Test
    public void getCarByIdWithSqlExceptionThrown() throws SQLException {
        Car car = createCarBMW().build();
        managerImpl.createCar(car);
        testExpectedServiceFailureException((m) -> m.getCarById(car.getId()));
    }

    @Test
    public void updateCarWithSqlExceptionThrown() throws SQLException {
        Car car = createCarBMW().build();
        managerImpl.createCar(car);
        testExpectedServiceFailureException((m) -> m.updateCar(car));
    }

    @Test
    public void deleteCarWithSqlExceptionThrown() throws SQLException {
        Car car = createCarBMW().build();
        managerImpl.createCar(car);
        testExpectedServiceFailureException((m) -> m.deleteCar(car));
    }

    @Test
    public void findAllCarsWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findAllCars());
    }

    @Test
    public void findCarsByBrandWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findCarsByBrand("BMW"));
    }
}
