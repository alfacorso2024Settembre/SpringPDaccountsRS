package it.alfasoft.myPersonalDriver.SpringPDaccountsRS;

import it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao.DaoAccounts;
import it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao.DtoAccountsRS;
import it.alfasoft.myPersonalDriver.common.dao.DaoException;
import it.alfasoft.myPersonalDriver.common.dao.dto.DtoAccounts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class SpringPDaccountsRsApplicationTests {


	@LocalServerPort
	private int port;

	@Autowired private TestRestTemplate aController;

	@MockitoBean private DaoAccounts da;

	private String baseUrl;

	@BeforeEach
	void setUp(){
		baseUrl = "http://localhost:" + port + "/accounts";
	}

	@Test
	void contextLoads() {
		assertThat(aController).isNotNull();
	}


	@Test
	void testGetAccounts() {
		//stubbing
		when(da.read()).thenReturn(List.of(new DtoAccounts()));

		//call
		ResponseEntity<DtoAccounts[]> accounts = this.aController.getForEntity(baseUrl,DtoAccounts[].class);

		//test
		verify(da,times(1)).read();
		Assertions.assertEquals(HttpStatus.OK,accounts.getStatusCode());
		Assertions.assertNotEquals(0,accounts.getBody().length);

	}



	@Test
	void testGetAccountByID(){

		//stubing
		when(da.search(1)).thenReturn(new DtoAccounts(1,"test@email.com","passw!sSed1","User","Active"));

		//call
		ResponseEntity<DtoAccounts> acc = this.aController.getForEntity(baseUrl + "/1",DtoAccounts.class);

		//test
		verify(da,times(1)).search(1);
		Assertions.assertEquals(HttpStatus.OK,acc.getStatusCode());
		Assertions.assertNotNull(acc.getBody());
		Assertions.assertEquals("test@email.com",acc.getBody().getEmail());

	}

	// da configurare con i enum

	@Test
	void testGetAccountsByFilters(){
		//stubing
		when(da.read("Active")).thenReturn(List.of( new DtoAccounts(1,"test67@email.com","passw!sSed1","User","Active"),new DtoAccounts(1,"tes222t@email.com","passw!sSed1","User","Active")));

		ResponseEntity<DtoAccounts[]> acc = this.aController.getForEntity(baseUrl+"/search?filter=Active",DtoAccounts[].class);

		//test
		verify(da,times(1)).read("Active");
		Assertions.assertEquals(HttpStatus.OK,acc.getStatusCode());
		Assertions.assertNotNull(acc.getBody());
		Assertions.assertEquals("test67@email.com",acc.getBody()[0].getEmail());

	}

	@Test
	void testGetAccountsByFiltersRole(){
		//stubing
		when(da.read("Driver")).thenReturn(List.of( new DtoAccounts(1,"test67@email.com","passw!sSed1","Driver","Active"),new DtoAccounts(1,"tes222t@email.com","passw!sSed1","Driver","Active")));

		ResponseEntity<DtoAccounts[]> acc = this.aController.getForEntity(baseUrl+"/search?filter=Driver",DtoAccounts[].class);

		//test
		verify(da,times(1)).read("Driver");
		Assertions.assertEquals(HttpStatus.OK,acc.getStatusCode());
		Assertions.assertNotNull(acc.getBody());
		Assertions.assertEquals("test67@email.com",acc.getBody()[0].getEmail());

	}

	@Test
	void testCreateAccount() {
		// Prepare mock data

		DtoAccounts mockAcc = new DtoAccounts("test@testCreate.com", "Password1!", "Admin", "Active");
		when(da.create(any(DtoAccounts.class))).thenReturn(1);

		// Perform the POST request
		ResponseEntity<String> response = this.aController.postForEntity(baseUrl, mockAcc, String.class);

		// Assertions
		Assertions.assertNotNull(response);
		Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
		Assertions.assertEquals(1, Integer.valueOf(response.getBody().split(":")[1].strip()));
	}


	//Testing with wrong credentials format
	@Test
	void testCreateAccountErrorCase(){
		//stubbing
		DtoAccounts mockAcc = new DtoAccounts(1,"testtestCreate.com","Password1","Admin","Active");
		when(da.create(any(DtoAccounts.class))).thenThrow(DaoException.class);

		//calling
		ResponseEntity<String> response = this.aController.postForEntity(baseUrl,mockAcc,String.class);

		//testing
		Assertions.assertNotNull(response);
		Assertions.assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());

	}

	@Test
	void testUpdateAccount() {
		// Stubbing
		when(da.update(any(DtoAccounts.class), anyInt())).thenReturn(1);

		DtoAccounts updatedAccount = new DtoAccounts("email@email.com", "Password2@!", "Driver", "Active");

		HttpEntity<DtoAccounts> request = new HttpEntity<>(updatedAccount);

		//call
		ResponseEntity<String> response = aController.exchange(
				baseUrl + "/1",
				HttpMethod.PUT,
				request,
				String.class
		);


		verify(da, times(1)).update(any(DtoAccounts.class), anyInt());
		Assertions.assertNotNull(response, "Response should not be null");
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code should be OK (200)");
		Assertions.assertEquals("Account updated successfully.", response.getBody(), "Response body should confirm successful update");
	}

	@Test
	void testUpdateAccountError() {
		// Stubbing
		when(da.update(any(DtoAccounts.class), anyInt())).thenReturn(0);

		DtoAccountsRS updatedAccount = new DtoAccountsRS("emailemail.com", "Password2@!", "Driver", "Active");

		HttpEntity<DtoAccountsRS> request = new HttpEntity<>(updatedAccount);

		//call
		ResponseEntity<String> response = aController.exchange(
				baseUrl + "/1",
				HttpMethod.PUT,
				request,
				String.class
		);



		Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Status code should be BAD_REQUEST (400)");

	}

	@Test
	void testDeleteAccountSuccess() {
		// Mocking
		when(da.delete(anyInt())).thenReturn(1);

		// Call
		ResponseEntity<String> response = aController.exchange(
				baseUrl + "/1",
				HttpMethod.DELETE,
				null,
				String.class
		);

		// Verify and test
		verify(da, times(1)).delete(1);
		Assertions.assertNotNull(response, "Response should not be null");
		Assertions.assertEquals(HttpStatus.OK, response.getStatusCode(), "Status code should be OK (200)");
		Assertions.assertEquals("Account deleted successfully.", response.getBody(), "Response body should confirm successful deletion");
	}

	@Test
	//account not found on delete da.delete returns 0
	void testDeleteAccountFailure() {
		// Mocking
		when(da.delete(anyInt())).thenReturn(0);

		// Call
		ResponseEntity<String> response = aController.exchange(
				baseUrl + "/1",
				HttpMethod.DELETE,
				null,
				String.class
		);

		// Verify and test
		verify(da, times(1)).delete(1);

		Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status code should be (404)");
		Assertions.assertEquals("Error: Account not found", response.getBody(), "Response body should confirm successful deletion");
	}





}
