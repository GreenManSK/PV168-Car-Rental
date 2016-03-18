package com.balkurcarrental.backend;

import com.balkurcarrental.backend.exceptions.EntityNotFoundException;
import com.balkurcarrental.backend.exceptions.InvalidEntityException;
import java.sql.Connection;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.apache.derby.jdbc.EmbeddedDataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.junit.After;

/**
 * Tests for CarManagerImpl
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class CarManagerImplTest {

    private static final Comparator<Car> CAR_ID_COMPARATOR = (c1, c2) -> c1.getId().compareTo(c2.getId());

    private CarManagerImpl managerImpl;
    private DataSource dataSource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("CREATE TABLE car ("
                    + "id BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,"
                    + "brand VARCHAR(50) NOT NULL,"
                    + "registration_number VARCHAR(50) NOT NULL)").executeUpdate();
        }
        managerImpl = new CarManagerImpl(dataSource);
    }

    @After
    public void tearDown() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.prepareStatement("DROP TABLE car").executeUpdate();
        }
    }

    @Test
    public void createAndGetCar() throws InvalidEntityException, EntityNotFoundException {
        Car car = createNewCar("BMW", "BB126");
        managerImpl.createCar(car);

        Long carId = car.getId();
        assertNotNull(carId);
        Car result = managerImpl.getCarById(carId);
        assertEquals(car, result);
        assertNotSame(car, result);
        assertDeepEquals(car, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullCar() throws InvalidEntityException {
        managerImpl.createCar(null);
    }

    @Test
    public void createCarWithSetId() throws InvalidEntityException {
        Car car = createNewCar(12L, "BMW", "BB126");
        expectedException.expect(IllegalArgumentException.class);
        managerImpl.createCar(car);
    }

    @Test
    public void createCarWithNullBrand() throws InvalidEntityException {
        Car car = createNewCar(null, "BB126");
        expectedException.expect(InvalidEntityException.class);
        managerImpl.createCar(car);
    }

    @Test
    public void createCarWithEmptyBrand() throws InvalidEntityException {
        Car car = createNewCar("  ", "BB126");
        expectedException.expect(InvalidEntityException.class);
        managerImpl.createCar(car);
    }

    @Test
    public void createCarWithNullRegistrationNumber() throws InvalidEntityException {
        Car car = createNewCar("BMW", null);
        expectedException.expect(InvalidEntityException.class);
        managerImpl.createCar(car);
    }

    @Test
    public void createCarWithEmptyRegistrationNumber() throws InvalidEntityException {
        Car car = createNewCar("BMW", "    ");
        expectedException.expect(InvalidEntityException.class);
        managerImpl.createCar(car);
    }

    @Test
    public void createCarWithUsedRegistrationNumber() throws InvalidEntityException {
        String regNumber = "ABC123";
        Car carBMW = createNewCar("BMW", regNumber);
        Car carPeugeot = createNewCar("Peugeot", regNumber);

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

    @Test
    public void updateCar() throws InvalidEntityException, EntityNotFoundException {
        Car carBmw = createDefaultCar();
        Car carPeugeot = createNewCar("Peugeot", "CD456");
        managerImpl.createCar(carPeugeot);

        Long carId = carBmw.getId();

        carBmw.setBrand("Mercedes");
        carBmw.setRegistrationNumber("FG789");
        managerImpl.updateCar(carBmw);

        carBmw = managerImpl.getCarById(carId);
        assertEquals("Mercedes", carBmw.getBrand());
        assertEquals("FG789", carBmw.getRegistrationNumber());

        // Check if updates didn't affected other records
        assertDeepEquals(carPeugeot, managerImpl.getCarById(carPeugeot.getId()));
    }

    @Test
    public void updateCarBrand() throws InvalidEntityException, EntityNotFoundException {
        Car carBmw = createDefaultCar();
        Car carPeugeot = createNewCar("Peugeot", "CD456");
        managerImpl.createCar(carPeugeot);

        Long carId = carBmw.getId();

        carBmw.setBrand("Mercedes");
        managerImpl.updateCar(carBmw);

        carBmw = managerImpl.getCarById(carId);
        assertEquals("Mercedes", carBmw.getBrand());
        assertEquals("AB123", carBmw.getRegistrationNumber());

        // Check if updates didn't affected other records
        assertDeepEquals(carPeugeot, managerImpl.getCarById(carPeugeot.getId()));
    }

    @Test
    public void updateCarRegistrationNumber() throws InvalidEntityException, EntityNotFoundException {
        Car carBmw = createDefaultCar();
        Car carPeugeot = createNewCar("Peugeot", "CD456");
        managerImpl.createCar(carPeugeot);

        Long carId = carBmw.getId();

        carBmw.setRegistrationNumber("FG789");
        managerImpl.updateCar(carBmw);

        carBmw = managerImpl.getCarById(carId);
        assertEquals("BMW", carBmw.getBrand());
        assertEquals("FG789", carBmw.getRegistrationNumber());

        // Check if updates didn't affected other records
        assertDeepEquals(carPeugeot, managerImpl.getCarById(carPeugeot.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullCar() throws InvalidEntityException, EntityNotFoundException {
        managerImpl.updateCar(null);
    }

    @Test
    public void updateCarWithNullId() throws InvalidEntityException, EntityNotFoundException {
        Car car = createNewCar(null, "BMW", "ABC123");
        car.setBrand("BCC");
        expectedException.expect(IllegalArgumentException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithNonExistingId() throws InvalidEntityException, EntityNotFoundException {
        Car car = createNewCar(128L, "BMW", "ABC123");
        car.setBrand("BCC");
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithNullBrand() throws InvalidEntityException, EntityNotFoundException {
        Car car = createDefaultCar();
        car.setBrand(null);
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithEmptyBrand() throws InvalidEntityException, EntityNotFoundException {
        Car car = createDefaultCar();
        car.setBrand("   ");
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithNullRegistrationNumber() throws InvalidEntityException, EntityNotFoundException {
        Car car = createDefaultCar();
        car.setRegistrationNumber(null);
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithEmptyRegistrationNumber() throws InvalidEntityException, EntityNotFoundException {
        Car car = createDefaultCar();
        car.setRegistrationNumber("   ");
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(car);
    }

    @Test
    public void updateCarWithUsedRegistrationNumber() throws InvalidEntityException, EntityNotFoundException {
        String regNumber = "ABC123";
        Car carBMW = createNewCar("BMW", regNumber);
        Car carPeugeot = createNewCar("Peugeot", "BCD126");

        managerImpl.createCar(carBMW);
        managerImpl.createCar(carPeugeot);

        carPeugeot.setRegistrationNumber(regNumber);
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCar(carPeugeot);
    }

    @Test
    public void deleteCar() throws InvalidEntityException, EntityNotFoundException {
        Car carBmw = createDefaultCar();
        Car carPeugeot = createNewCar("Peugeot", "BCD126");
        managerImpl.createCar(carPeugeot);

        managerImpl.getCarById(carBmw.getId());
        managerImpl.getCarById(carPeugeot.getId());

        managerImpl.deleteCar(carBmw);

        managerImpl.getCarById(carPeugeot.getId());
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.getCarById(carBmw.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullCar() throws EntityNotFoundException {
        managerImpl.deleteCar(null);
    }

    @Test
    public void deleteCarWithNullId() throws EntityNotFoundException {
        Car car = createNewCar(null, "BMW", "ABC123");
        expectedException.expect(IllegalArgumentException.class);
        managerImpl.deleteCar(car);
    }

    @Test
    public void deleteCarWithNonExistingId() throws EntityNotFoundException {
        Car car = createNewCar(128L, "BMW", "ABC123");
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.deleteCar(car);
    }

    @Test
    public void findAllCars() throws InvalidEntityException {
        assertTrue(managerImpl.findAllCars().isEmpty());

        Car c1 = createDefaultCar();
        Car c2 = createNewCar("Peugeot", "lkjs");
        managerImpl.createCar(c2);

        List<Car> expected = Arrays.asList(c1, c2);
        List<Car> actual = managerImpl.findAllCars();

        Collections.sort(actual, CAR_ID_COMPARATOR);
        Collections.sort(expected, CAR_ID_COMPARATOR);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void findAllCarsFromEmpty() {
        assertTrue(managerImpl.findAllCars().isEmpty());
    }

    @Test
    public void findCarsByBrand() throws InvalidEntityException {
        Car c1 = createDefaultCar();
        Car c2 = createNewCar("Peugeot", "lkjs");
        Car c3 = createNewCar("Peugeot", "APO888");
        managerImpl.createCar(c2);
        managerImpl.createCar(c3);

        List<Car> expected = Arrays.asList(c2, c3);
        List<Car> actual = managerImpl.findCarsByBrand("Peugeot");

        Collections.sort(actual, CAR_ID_COMPARATOR);
        Collections.sort(expected, CAR_ID_COMPARATOR);

        assertEquals(expected, actual);
        assertDeepEquals(expected, actual);
    }

    @Test
    public void findCarsByNonExistingBrand() throws InvalidEntityException {
        Car c1 = createDefaultCar();
        assertTrue(managerImpl.findCarsByBrand("Lenovo").isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findCarsByNullBrand() throws InvalidEntityException {
        managerImpl.findCarsByBrand(null);
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:carrental-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    private Car createNewCar(Long id, String brand, String registrationNumber) {
        Car car = new Car();
        car.setId(id);
        car.setBrand(brand);
        car.setRegistrationNumber(registrationNumber);
        return car;
    }

    private Car createNewCar(String brand, String registrationNumber) {
        return createNewCar(null, brand, registrationNumber);
    }

    private Car createDefaultCar() throws InvalidEntityException {
        Car car = createNewCar("BMW", "AB123");
        managerImpl.createCar(car);
        return car;
    }

    private void assertDeepEquals(List<Car> expectedList, List<Car> actualList) {
        assertSame(expectedList.size(), actualList.size());
        for (int i = 0; i < expectedList.size(); i++) {
            Car expected = expectedList.get(i);
            Car actual = actualList.get(i);
            assertDeepEquals(expected, actual);
        }
    }

    private void assertDeepEquals(Car expected, Car actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getBrand(), actual.getBrand());
        assertEquals(expected.getRegistrationNumber(), actual.getRegistrationNumber());
    }

}
