package com.balkurcarrental.backend;

import com.balkurcarrental.common.DBUtils;
import com.balkurcarrental.common.EntityNotFoundException;
import com.balkurcarrental.common.InvalidEntityException;
import com.balkurcarrental.common.ServiceFailureException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import static org.mockito.Mockito.*;

/**
 * Tests for RentManagerImpl
 *
 * @author Lukáš Kurčík <lukas.kurcik at gmail.com>
 */
public class RentManagerImplTest {

    private RentManagerImpl manager;
    private CustomerManager customerManager;
    private CarManager carManager;
    private DataSource dataSource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, RentManager.class.getResource(
                "createTables.sql"));
        manager = new RentManagerImpl();
        manager.setDataSource(dataSource);
        carManager = mockCarManager();
        customerManager = mockCustomerManager();
        manager.setCarManager(carManager);
        manager.setCustomerManager(customerManager);
    }

    @After
    public void tearDown() throws SQLException {
        DBUtils.executeSqlScript(dataSource, RentManager.class.getResource(
                "dropTables.sql"));
    }

    private static DataSource prepareDataSource() throws SQLException {
        EmbeddedDataSource ds = new EmbeddedDataSource();
        //we will use in memory database
        ds.setDatabaseName("memory:carrental-test");
        ds.setCreateDatabase("create");
        return ds;
    }

    private static CarManager mockCarManager() {
        CarManager carManager = mock(CarManager.class);

        when(carManager.getCarById(12L)).thenReturn(new CarBuilder().id(12L).
                brand("BMW").build());
        when(carManager.getCarById(24L)).thenReturn(new CarBuilder().id(24L).
                brand("Mercedes").build());

        return carManager;
    }

    private static CustomerManager mockCustomerManager() {
        CustomerManager customerManager = mock(CustomerManager.class);

        when(customerManager.getCustomerById(64L)).thenReturn(
                new CustomerBuilder().id(64L).name("Lukas").build());
        when(customerManager.getCustomerById(24L)).thenReturn(
                new CustomerBuilder().id(24L).name("Simon").build());

        return customerManager;
    }

    private RentBuilder createLukasBmwRent() {
        return new RentBuilder()
                .id(null)
                .car(carManager.getCarById(12L))
                .customer(customerManager.getCustomerById(64L))
                .beginningDate(LocalDate.of(2016, 3, 24))
                .expectedReturnDate(LocalDate.of(2016, 3, 28))
                .realReturnDate(LocalDate.of(2016, 3, 29))
                .pricePerDay(150);
    }

    private RentBuilder createSimonMercedesRent() {
        return new RentBuilder()
                .id(null)
                .car(carManager.getCarById(24L))
                .customer(customerManager.getCustomerById(24L))
                .beginningDate(LocalDate.of(2010, 2, 15))
                .expectedReturnDate(LocalDate.of(2010, 2, 25))
                .realReturnDate(LocalDate.of(2010, 2, 20))
                .pricePerDay(250);
    }

    @Test
    public void createAndGetRent() {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);

        Long rentId = rent.getId();
        assertThat(rentId).isNotNull();

        assertThat(manager.getRentById(rentId)).isEqualTo(rent).
                isNotSameAs(rent).isEqualToComparingFieldByField(rent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNullRent() {
        manager.createRent(null);
    }

    private void testCreateUnsuccessfully(Consumer<RentBuilder> setOperation) {
        RentManagerImplTest.this.
                testCreateUnsuccessfully(setOperation, InvalidEntityException.class);
    }

    private void testCreateUnsuccessfully(Consumer<RentBuilder> setOperation,
            Class<? extends Exception> exceptionClass) {
        RentBuilder rentBuilder = createLukasBmwRent();
        setOperation.accept(rentBuilder);
        Rent rent = rentBuilder.build();
        expectedException.expect(exceptionClass);
        manager.createRent(rent);
    }

    @Test
    public void createRentWithSetId() {
        RentManagerImplTest.this.testCreateUnsuccessfully((rb) -> rb.id(55L),
                IllegalArgumentException.class);
    }

    @Test
    public void createRentWithInvalidCustomer() {
        testCreateUnsuccessfully((rb) -> rb.customer(new Customer()));
    }

    @Test
    public void createRentWithInvalidCar() {
        testCreateUnsuccessfully((rb) -> rb.car(new Car()));
    }

    @Test
    public void createRentWithZeroPricePerDay() {
        testCreateUnsuccessfully((rb) -> rb.pricePerDay(0));
    }

    @Test
    public void createRentWithNegativePricePerDay() {
        testCreateUnsuccessfully((rb) -> rb.pricePerDay(-15));
    }

    @Test
    public void createRentWithNullBeginningDate() {
        testCreateUnsuccessfully((rb) -> rb.beginningDate(null));
    }

    @Test
    public void createRentWithExpectedReturnDateBeforeBeginningDate() {
        testCreateUnsuccessfully((rb) -> rb.expectedReturnDate(LocalDate.
                of(2015,
                        2, 11)));
    }

    @Test
    public void createRentWithRealReturnDateBeforeBeginningDate() {
        testCreateUnsuccessfully((rb) -> rb.realReturnDate(LocalDate.of(2015,
                2, 11)));
    }

    @Test
    public void createRentWithUnrenturnedCarOngoingRent() {
        Rent unfinishedRent = createLukasBmwRent()
                .beginningDate(LocalDate.of(2015, 2, 11))
                .realReturnDate(null).build();
        manager.createRent(unfinishedRent);
        testCreateUnsuccessfully((rb) -> rb.beginningDate(LocalDate.of(2015, 3,
                11)));
    }

    @Test
    public void createRentWithUnrenturnedCarUnfinishedRent() {
        Rent unfinishedRent = createLukasBmwRent()
                .beginningDate(LocalDate.of(2015, 2, 11))
                .realReturnDate(LocalDate.of(2017, 2, 11)).build();
        manager.createRent(unfinishedRent);
        testCreateUnsuccessfully((rb) -> rb.beginningDate(LocalDate.of(2015, 3,
                11)));
    }

    @Test
    public void createRentWithUnrenturnedCarColision() {
        Rent unfinishedRent = createLukasBmwRent()
                .beginningDate(LocalDate.of(2015, 3, 11))
                .realReturnDate(LocalDate.of(2017, 2, 11)).build();
        manager.createRent(unfinishedRent);
        testCreateUnsuccessfully((rb) -> rb.beginningDate(LocalDate.of(2015, 2,
                11)).realReturnDate(LocalDate.of(2015, 3, 15)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRentByNullId() {
        manager.getRentById(null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getRentByNotExistingId() {
        manager.getRentById(1024L);
    }

    private void testUpdateSuccessfully(Consumer<Rent> updateOperation) {
        Rent lukasBmwRent = createLukasBmwRent().build();
        Rent simonMercedesRent = createSimonMercedesRent().build();
        manager.createRent(lukasBmwRent);
        manager.createRent(simonMercedesRent);

        updateOperation.accept(lukasBmwRent);
        manager.updateRent(lukasBmwRent);

        assertThat(manager.getRentById(lukasBmwRent.getId())).
                isEqualToComparingFieldByField(lukasBmwRent);
        assertThat(manager.getRentById(simonMercedesRent.getId())).
                isEqualToComparingFieldByField(simonMercedesRent);
    }

    @Test
    public void updateRent() {
        testUpdateSuccessfully((r) -> {
            r.setPricePerDay(2000);
            r.setRealReturnDate(null);
        });
    }

    @Test
    public void updateRentCustomer() {
        testUpdateSuccessfully((r) -> r.setCustomer(customerManager.
                getCustomerById(24L)));
    }

    @Test
    public void updateRentCar() {
        testUpdateSuccessfully((r) -> r.setCar(carManager.
                getCarById(24l)));
    }

    @Test
    public void updateRentPricePerDay() {
        testUpdateSuccessfully((r) -> r.setPricePerDay(100));
    }

    @Test
    public void updateRentBeginningDate() {
        testUpdateSuccessfully((r) -> r.setBeginningDate(LocalDate.
                of(2016, 3, 25)));
    }

    @Test
    public void updateRentExpectedReturnDate() {
        testUpdateSuccessfully((r) -> r.setExpectedReturnDate(LocalDate.of(2016,
                3, 29)));
    }

    @Test
    public void updateRentRealReturnDate() {
        testUpdateSuccessfully((r) -> r.setRealReturnDate(LocalDate.of(2016, 3,
                30)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateNullRent() {
        manager.updateRent(null);
    }

    private void testUpdateUnsuccessfully(Consumer<Rent> updateOpration) {
        testUpdateUnsuccessfully(updateOpration, InvalidEntityException.class);
    }

    private void testUpdateUnsuccessfully(Consumer<Rent> updateOpration,
            Class<? extends Exception> exceptionClass) {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);
        updateOpration.accept(rent);
        expectedException.expect(exceptionClass);
        manager.updateRent(rent);
    }

    @Test
    public void updateRentWithNullId() {
        testUpdateUnsuccessfully((r) -> r.setId(null), IllegalArgumentException.class);
    }

    @Test
    public void updateRentWithNonExistingId() {
        Rent rent = createLukasBmwRent().id(1024L).build();
        expectedException.expect(EntityNotFoundException.class);
        manager.updateRent(rent);
    }

    @Test
    public void updateRentWithInvalidCustomer() {
        testUpdateUnsuccessfully((r) -> r.setCustomer(new Customer()));
    }

    @Test
    public void updateRentWithInvalidCar() {
        testUpdateUnsuccessfully((r) -> r.setCar(new Car()));
    }

    @Test
    public void updateRentWithZeroPricePerDay() {
        testUpdateUnsuccessfully((rb) -> rb.setPricePerDay(0));
    }

    @Test
    public void updateRentWithNegativePricePerDay() {
        testUpdateUnsuccessfully((rb) -> rb.setPricePerDay(-15));
    }

    @Test
    public void updateRentWithNullBeginningDate() {
        testUpdateUnsuccessfully((rb) -> rb.setBeginningDate(null));
    }

    @Test
    public void updateRentWithExpectedReturnDateBeforeBeginningDate() {
        testUpdateUnsuccessfully((rb) -> rb.setExpectedReturnDate(LocalDate.
                of(2015, 2, 11)));
    }

    @Test
    public void updateRentWithRealReturnDateBeforeBeginningDate() {
        testUpdateUnsuccessfully((rb)
                -> rb.setRealReturnDate(LocalDate.of(2015, 2, 11)));
    }

    @Test
    public void updateRentWithUnrenturnedCarOngoingRent() {
        Rent rent = createLukasBmwRent()
                .beginningDate(LocalDate.of(2014, 2, 11))
                .realReturnDate(LocalDate.of(2014, 2, 15))
                .build();
        manager.createRent(rent);
        Rent unfinishedRent = createLukasBmwRent()
                .beginningDate(LocalDate.of(2015, 2, 11))
                .realReturnDate(null).build();
        manager.createRent(unfinishedRent);

        rent.setRealReturnDate(null);
        rent.setBeginningDate(LocalDate.of(2015, 3, 11));
        expectedException.expect(InvalidEntityException.class);
        manager.updateRent(rent);
    }

    @Test
    public void updateRentWithUnrenturnedCarUnfinishedRent() {
        Rent unfinishedRent = createLukasBmwRent()
                .beginningDate(LocalDate.of(2015, 2, 11))
                .realReturnDate(LocalDate.of(2015, 2, 13)).build();
        manager.createRent(unfinishedRent);
        testUpdateUnsuccessfully((rb)
                -> rb.setBeginningDate(LocalDate.of(2015, 2, 12)));
    }

    @Test
    public void updateRentWithUnrenturnedCarColision() {
        Rent unfinishedRent = createLukasBmwRent()
                .beginningDate(LocalDate.of(2015, 3, 11))
                .realReturnDate(LocalDate.of(2015, 3, 13)).build();
        manager.createRent(unfinishedRent);
        testUpdateUnsuccessfully((rb) -> {
            rb.setBeginningDate(LocalDate.of(2015, 2,
                    11));
            rb.setRealReturnDate(LocalDate.of(2015, 3, 15));
        });
    }

    @Test
    public void deleteRent() {
        Rent lukasBmwRent = createLukasBmwRent().build();
        Rent simonMercedesRent = createSimonMercedesRent().build();
        manager.createRent(lukasBmwRent);
        manager.createRent(simonMercedesRent);

        manager.getRentById(lukasBmwRent.getId());
        manager.getRentById(simonMercedesRent.getId());

        manager.deleteRent(lukasBmwRent);

        manager.getRentById(simonMercedesRent.getId());
        expectedException.expect(EntityNotFoundException.class);
        manager.getRentById(lukasBmwRent.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNullRent() {
        manager.deleteRent(null);
    }

    @Test
    public void deleteRentWithNullId() {
        Rent rent = createLukasBmwRent().build();
        expectedException.expect(IllegalArgumentException.class);
        manager.deleteRent(rent);
    }

    @Test
    public void deleteRentWithNonExistingId() {
        Rent rent = createLukasBmwRent().id(1024L).build();
        expectedException.expect(EntityNotFoundException.class);
        manager.deleteRent(rent);
    }

    @Test
    public void findAllRents() {
        assertThat(manager.findAllRents()).isEmpty();

        Rent r1 = createLukasBmwRent().build();
        Rent r2 = createSimonMercedesRent().build();
        manager.createRent(r1);
        manager.createRent(r2);

        assertThat(manager.findAllRents()).usingFieldByFieldElementComparator().
                containsOnly(r1, r2);
    }

    @Test
    public void findAllRentsFromEmpty() {
        assertThat(manager.findAllRents()).isEmpty();
    }

    @Test
    public void findRentsForCustomer() {
        assertThat(manager.findAllRents()).isEmpty();

        Rent r1 = createLukasBmwRent().build();
        Rent r3 = createLukasBmwRent()
                .beginningDate(LocalDate.of(2017, 3, 24))
                .realReturnDate(null)
                .expectedReturnDate(null)
                .build();
        Rent r2 = createSimonMercedesRent().build();
        manager.createRent(r1);
        manager.createRent(r3);
        manager.createRent(r2);

        assertThat(manager.findRentsForCustomer(r1.getCustomer()))
                .usingFieldByFieldElementComparator().containsOnly(r1, r3);
    }

    @Test
    public void findRentsForCustomerFromEmpty() {
        assertThat(manager.findRentsForCustomer(new CustomerBuilder().id(1200L).
                build())).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void findRentsForCustomerNull() {
        manager.findRentsForCustomer(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findRentsForCustomerNullId() {
        manager.findRentsForCustomer(new Customer());
    }

    @Test
    public void findRentsForCar() {
        assertThat(manager.findAllRents()).isEmpty();

        Rent r1 = createLukasBmwRent().build();
        Rent r3 = createLukasBmwRent()
                .beginningDate(LocalDate.of(2017, 3, 24))
                .realReturnDate(null)
                .expectedReturnDate(null)
                .build();
        Rent r2 = createSimonMercedesRent().build();
        manager.createRent(r1);
        manager.createRent(r3);
        manager.createRent(r2);

        assertThat(manager.findRentsForCar(r1.getCar()))
                .usingFieldByFieldElementComparator().containsOnly(r1, r3);
    }

    @Test
    public void findRentsForCarFromEmpty() {
        assertThat(manager.findRentsForCar(new CarBuilder().id(1200L).
                build())).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void findRentsCarNull() {
        manager.findRentsForCar(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findRentsForCarNullId() {
        manager.findRentsForCar(new Car());
    }

    private void testExpectedServiceFailureException(
            Consumer<RentManager> operation) throws SQLException {
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        manager.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.accept(manager))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void createRentWithSqlExceptionThrown() throws SQLException {
        Rent rent = createLukasBmwRent().build();
        testExpectedServiceFailureException((m) -> m.createRent(rent));
    }

    @Test
    public void getRentByIdWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.getRentById(12L));
    }

    @Test
    public void updateRentWithSqlExceptionThrown() throws SQLException {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);
        testExpectedServiceFailureException((m) -> m.updateRent(rent));
    }

    @Test
    public void deleteRentWithSqlExceptionThrown() throws SQLException {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);
        testExpectedServiceFailureException((m) -> m.deleteRent(rent));
    }

    @Test
    public void findAllRentsWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findAllRents());
    }

    @Test
    public void findRentsForCustomerWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findRentsForCustomer(
                customerManager.getCustomerById(64L)));
    }

    @Test
    public void findRentsForCarWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findRentsForCar(carManager.
                getCarById(12L)));
    }

    @Test
    public void createRentWithoutDataSource() {
        manager.setDataSource(null);
        Rent rent = createLukasBmwRent().build();
        expectedException.expect(IllegalStateException.class);
        manager.createRent(rent);
    }

    @Test
    public void getRentByIdWithoutDataSource() {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);
        manager.setDataSource(null);
        expectedException.expect(IllegalStateException.class);
        manager.getRentById(rent.getId());
    }

    @Test
    public void updateRentWithoutDataSource() {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);
        manager.setDataSource(null);
        expectedException.expect(IllegalStateException.class);
        manager.updateRent(rent);
    }

    @Test
    public void deleteRentWithoutDataSource() {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);
        manager.setDataSource(null);
        expectedException.expect(IllegalStateException.class);
        manager.deleteRent(rent);
    }

    @Test
    public void findAllRentsWithoutDataSource() {
        manager.setDataSource(null);
        expectedException.expect(IllegalStateException.class);
        manager.findAllRents();
    }

    @Test
    public void findRentsForCarWithoutDataSource() {
        manager.setDataSource(null);
        expectedException.expect(IllegalStateException.class);
        manager.findRentsForCar(new CarBuilder().id(1200L).build());
    }

    @Test
    public void findRentsForCustomerWithoutDataSource() {
        manager.setDataSource(null);
        expectedException.expect(IllegalStateException.class);
        manager.findRentsForCustomer(new CustomerBuilder().id(1200L).build());
    }

    @Test
    public void getRentByIdWithoutCarManager() {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);
        manager.setCarManager(null);
        expectedException.expect(IllegalStateException.class);
        manager.getRentById(rent.getId());
    }

    @Test
    public void getRentByIdWithoutCustomerManager() {
        Rent rent = createLukasBmwRent().build();
        manager.createRent(rent);
        manager.setCustomerManager(null);
        expectedException.expect(IllegalStateException.class);
        manager.getRentById(rent.getId());
    }

    @Test
    public void findRentsForCarWithoutCarManager() {
        manager.setCarManager(null);
        expectedException.expect(IllegalStateException.class);
        manager.findRentsForCar(new CarBuilder().id(1200L).build());
    }

    @Test
    public void findRentsForCustomerWithoutCarManager() {
        manager.setCarManager(null);
        expectedException.expect(IllegalStateException.class);
        manager.findRentsForCustomer(new CustomerBuilder().id(1200L).build());
    }

    @Test
    public void findAllRentsWithoutCarManager() {
        manager.setCarManager(null);
        expectedException.expect(IllegalStateException.class);
        manager.findAllRents();
    }

    @Test
    public void findRentsForCarWithoutCustomerManager() {
        manager.setCustomerManager(null);
        expectedException.expect(IllegalStateException.class);
        manager.findRentsForCar(new CarBuilder().id(1200L).build());
    }

    @Test
    public void findRentsForCustomerWithoutCustomerManager() {
        manager.setCustomerManager(null);
        expectedException.expect(IllegalStateException.class);
        manager.findRentsForCustomer(new CustomerBuilder().id(1200L).build());
    }

    @Test
    public void findAllRentsWithoutCustomerManager() {
        manager.setCustomerManager(null);
        expectedException.expect(IllegalStateException.class);
        manager.findAllRents();
    }
}
