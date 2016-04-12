/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.balkurcarrental.backend;

import com.balkurcarrental.common.DBUtils;
import com.balkurcarrental.common.EntityNotFoundException;
import com.balkurcarrental.common.InvalidEntityException;
import com.balkurcarrental.common.ServiceFailureException;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Šimon
 */
public class CustomerManagerImplTest {    

    private CustomerManagerImpl managerImpl;
    private DataSource dataSource;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws SQLException {
        dataSource = prepareDataSource();
        DBUtils.executeSqlScript(dataSource, CustomerManager.class.getResource(
                "createTables.sql"));
        managerImpl = new CustomerManagerImpl();
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
    
    private CustomerBuilder createCustomerFrodo() {
        return new CustomerBuilder().id(null).name("Frodo").surname("Baggins").
                phoneNumber("0931587493");
    }
    
    private CustomerBuilder createCustomerJames() {
        return new CustomerBuilder().id(null).name("James").surname("Bond").
                phoneNumber("0985723798");
    }
    
    @Test
    public void createAndGetCustomer() {
        Customer customer = createCustomerFrodo().build();
        managerImpl.createCustomer(customer);

        Long customerId = customer.getId();
        assertThat(customerId).isNotNull();
        
        assertThat(managerImpl.getCustomerById(customerId)).isEqualTo(customer).
                isNotSameAs(customer).
                isEqualToComparingFieldByField(customer);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void createNullCustomer() throws InvalidEntityException {
        managerImpl.createCustomer(null);
    }
    
    private void testCreateUnsuccessfully(Consumer<CustomerBuilder> setOperation) {
        testCreateUnsuccessfully(setOperation, IllegalArgumentException.class);
    }
    
    private void testCreateUnsuccessfully(Consumer<CustomerBuilder> setOperation,
            Class<? extends Exception> exceptionClass) {
        CustomerBuilder customerBuilder = createCustomerFrodo();
        setOperation.accept(customerBuilder);
        
        Customer customer = customerBuilder.build();
        expectedException.expect(exceptionClass);
        managerImpl.createCustomer(customer);
    }
    
    @Test
    public void createCustomerWithSetId() {
        testCreateUnsuccessfully((cb) -> cb.id(12L));
    }
    
    @Test
    public void createCustomerWithNullName() {
        testCreateUnsuccessfully((cb) -> cb.name(null),
                InvalidEntityException.class);
    }
    
    @Test
    public void createCustomerWithEmptyName() {
        testCreateUnsuccessfully((cb) -> cb.name("   "),
                InvalidEntityException.class);
    }
    
    @Test
    public void createCustomerWithNullSurname() {
        testCreateUnsuccessfully((cb) -> cb.surname(null),
                InvalidEntityException.class);
    }
    
    @Test
    public void createCustomerWithEmptySurname() {
        testCreateUnsuccessfully((cb) -> cb.surname("   "),
                InvalidEntityException.class);
    }
    
    @Test
    public void createCustomerWithNullPhoneNumber() {
        testCreateUnsuccessfully((cb) -> cb.phoneNumber(null),
                InvalidEntityException.class);
    }
    
    @Test
    public void createCustomerWithEmptyPhoneNumber() {
        testCreateUnsuccessfully((cb) -> cb.phoneNumber("    "),
                InvalidEntityException.class);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getCustomerByNullId() {
        Customer customer = managerImpl.getCustomerById(null);
    }
    
    @Test(expected = EntityNotFoundException.class)
    public void getCustomerByNotExistingId() {
        Customer customer = managerImpl.getCustomerById(1024L);
    }
    
    private void testUpdateSuccessfully(Consumer<Customer> updateOperation) {
        Customer customerFrodo = createCustomerFrodo().build();
        Customer customerJames = createCustomerJames().build();
        managerImpl.createCustomer(customerFrodo);
        managerImpl.createCustomer(customerJames);

        updateOperation.accept(customerFrodo);
        managerImpl.updateCustomer(customerFrodo);

        assertThat(managerImpl.getCustomerById(customerFrodo.getId())).
                isEqualToComparingFieldByField(customerFrodo);
        assertThat(managerImpl.getCustomerById(customerJames.getId())).
                isEqualToComparingFieldByField(customerJames);
    }
    
    @Test
    public void updateCustomer() {
        testUpdateSuccessfully((c) -> {
            c.setPhoneNumber("0912478657");
            c.setSurname("Jonhson");
            c.setName("Dwayne");
        });
    }
    
    @Test
    public void updateCustomerName() {
        testUpdateSuccessfully((c) -> {
            c.setName("Dwayne");
        });
    }
    
    @Test
    public void updateCustomerSurname() {
        testUpdateSuccessfully((c) -> {
            c.setSurname("Jonhson");
        });
    }
    
    @Test
    public void updateCustomerPhoneNumber() {
        testUpdateSuccessfully((c) -> {
            c.setPhoneNumber("0912478657");
        });
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void updateNullCustomer() {
        managerImpl.updateCustomer(null);
    }
    
    @Test
    public void updateCustomerWithNullId() {
        Customer customer = createCustomerFrodo().build();        
        expectedException.expect(IllegalArgumentException.class);
        managerImpl.updateCustomer(customer);
    }
    
    @Test
    public void updateCustomerWithNonExistingId() {
        Customer customer = createCustomerFrodo().id(128L).build();        
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.updateCustomer(customer);
    }
    
    @Test
    public void updateCustomerWithNullName() {
        Customer customer = createCustomerFrodo().name(null).build();        
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCustomer(customer);
    }
    
    @Test
    public void updateCustomerWithEmptyName() {
        Customer customer = createCustomerFrodo().name("   ").build();        
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCustomer(customer);
    }
    
    @Test
    public void updateCustomerWithNullSurname() {
        Customer customer = createCustomerFrodo().surname(null).build();        
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCustomer(customer);
    }
    
    @Test
    public void updateCustomerWithEmptySurname() {
        Customer customer = createCustomerFrodo().surname("  ").build();        
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCustomer(customer);
    }
    
    @Test
    public void updateCustomerWithNullPhoneNumber() {
        Customer customer = createCustomerFrodo().phoneNumber(null).build();       
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCustomer(customer);
    }
    
    @Test
    public void updateCustomerWithEmptyPhoneNumber() {
        Customer customer = createCustomerFrodo().phoneNumber("  ").build();        
        expectedException.expect(InvalidEntityException.class);
        managerImpl.updateCustomer(customer);
    }
    
    @Test
    public void deleteCustomer() {
        Customer customerFrodo = createCustomerFrodo().build();
        Customer customerJames = createCustomerJames().build();
        
        managerImpl.createCustomer(customerFrodo);
        managerImpl.createCustomer(customerJames);

        managerImpl.getCustomerById(customerFrodo.getId());
        managerImpl.getCustomerById(customerJames.getId());

        managerImpl.deleteCustomer(customerFrodo);

        managerImpl.getCustomerById(customerJames.getId());
        
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.getCustomerById(customerFrodo.getId());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void deleteNullCustomer() {
        managerImpl.deleteCustomer(null);
    }
    
    @Test
    public void deleteCustomerWithNullId() {
        Customer customer = createCustomerFrodo().build();
        expectedException.expect(IllegalArgumentException.class);
        managerImpl.deleteCustomer(customer);
    }
    
    @Test
    public void deleteCustomerWithNonExistingId() {
        Customer customer = createCustomerFrodo().id(128L).build();
        expectedException.expect(EntityNotFoundException.class);
        managerImpl.deleteCustomer(customer);
    }
    
    @Test
    public void findAllCustomers() {
        assertThat(managerImpl.findAllCustomers()).isEmpty();

        Customer customerFrodo = createCustomerFrodo().build();
        Customer customerJames = createCustomerJames().build();
        
        managerImpl.createCustomer(customerFrodo);
        managerImpl.createCustomer(customerJames);

        assertThat(managerImpl.findAllCustomers())
                .usingFieldByFieldElementComparator()
                .containsOnly(customerFrodo, customerJames);
    }
    
    @Test
    public void findAllCustomersFromEmpty() {
        assertThat(managerImpl.findAllCustomers()).isEmpty();
    }
    
    @Test
    public void findCustomersByName() {
        Customer customerFrodo = createCustomerFrodo().build();
        Customer customerJames = createCustomerJames().build();
        Customer customer3 = new CustomerBuilder().name("James").surname("Sheppard").phoneNumber(
                "0935478952").build();        
        
        managerImpl.createCustomer(customerFrodo);
        managerImpl.createCustomer(customerJames);
        managerImpl.createCustomer(customer3);

        assertThat(managerImpl.findCustomersByName("James"))
                .usingFieldByFieldElementComparator()
                .containsOnly(customerJames, customer3);
    }
    
    @Test
    public void findCustomersBySurname() {
        Customer customerFrodo = createCustomerFrodo().build();
        Customer customerJames = createCustomerJames().build();
        Customer customer3 = new CustomerBuilder().name("Alfred").surname("Bond").phoneNumber(
                "0935478952").build();    
        
        managerImpl.createCustomer(customerFrodo);
        managerImpl.createCustomer(customerJames);
        managerImpl.createCustomer(customer3);

        assertThat(managerImpl.findCustomersBySurname("Bond"))
                .usingFieldByFieldElementComparator()
                .containsOnly(customerJames, customer3);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void findCustomerByNullName() {
        managerImpl.findCustomersByName(null);
    }
    
    @Test
    public void findCustomersByNonExistingName() {
        Customer customer = createCustomerFrodo().build();
        managerImpl.createCustomer(customer);
        assertThat(managerImpl.findCustomersByName("Rofl")).isEmpty();        
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void findCustomerByNullSurname() {
        managerImpl.findCustomersBySurname(null);
    }
    
    @Test
    public void findCustomersByNonExistingSurname() {
        Customer customer = createCustomerFrodo().build();
        managerImpl.createCustomer(customer);
        assertThat(managerImpl.findCustomersBySurname("Rofl")).isEmpty();        
    }   
    
    private void testExpectedServiceFailureException(
            Consumer<CustomerManager> operation) throws SQLException {
        
        SQLException sqlException = new SQLException();
        DataSource failingDataSource = mock(DataSource.class);
        
        when(failingDataSource.getConnection()).thenThrow(sqlException);
        managerImpl.setDataSource(failingDataSource);
        assertThatThrownBy(() -> operation.accept(managerImpl))
                .isInstanceOf(ServiceFailureException.class)
                .hasCause(sqlException);
    }

    @Test
    public void createCustomerWithSqlExceptionThrown() throws SQLException {
        Customer customer = createCustomerFrodo().build();
        testExpectedServiceFailureException((m) -> m.createCustomer(customer));
    }

    @Test
    public void getCustomerByIdWithSqlExceptionThrown() throws SQLException {
       Customer customer = createCustomerFrodo().build();
        managerImpl.createCustomer(customer);
        testExpectedServiceFailureException((m) -> m.getCustomerById(customer.getId()));
    }

    @Test
    public void updateCustomerWithSqlExceptionThrown() throws SQLException {
        Customer customer = createCustomerFrodo().build();
        managerImpl.createCustomer(customer);
        testExpectedServiceFailureException((m) -> m.updateCustomer(customer));
    }

    @Test
    public void deleteCustomerWithSqlExceptionThrown() throws SQLException {
        Customer customer = createCustomerFrodo().build();
        managerImpl.createCustomer(customer);
        testExpectedServiceFailureException((m) -> m.deleteCustomer(customer));
    }

    @Test
    public void findAllCustomersWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findAllCustomers());
    }

    @Test
    public void findCustomersByNamedWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findCustomersByName("Alfred"));
    }
    
    @Test
    public void findCustomersBySurnameWithSqlExceptionThrown() throws SQLException {
        testExpectedServiceFailureException((m) -> m.findCustomersBySurname("Lincoln"));
    }
    
}
