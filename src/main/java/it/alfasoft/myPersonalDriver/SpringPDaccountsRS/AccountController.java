package it.alfasoft.myPersonalDriver.SpringPDaccountsRS;

import it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao.DaoAccounts;
import it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao.DaoAccountsUtility;
import it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao.DtoAccountsRS;
import it.alfasoft.myPersonalDriver.common.Exceptions.DaoException;
import it.alfasoft.myPersonalDriver.common.Exceptions.ErrorCodes;
import it.alfasoft.myPersonalDriver.common.dao.dto.DtoAccounts;
import it.alfasoft.myPersonalDriver.common.dao.dto.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private DaoAccounts daoAccounts;

    @GetMapping
    public ResponseEntity<Object> getAccounts(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String filter
    ) {
        logger.info("Fetching accounts, offset={}, limit={}, textSearch={}",
                offset, limit, filter);

        if(limit <= 0 || offset < 0){
            logger.error("Valori limit o offset non corretti: limit = " + limit + " e offset = " + offset);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).header("PDerror", "valori input non corretti").build();
        }

        try {

            List<DtoAccounts> accounts = daoAccounts.read(offset, limit, filter);


            List<DtoAccountsRS> accountsRS = accounts.stream()
                    .map(account -> new DtoAccountsRS(
                            account.getEmail(),
                            account.getPassword(),
                            account.getRole(),
                            account.getStatus()
                    ))
                    .collect(Collectors.toList());

            logger.info("Accounts fetched successfully. Count: {}", accountsRS.size());
            return ResponseEntity.ok(accountsRS);

        } catch (DaoException e) {
            logger.error("DaoException: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header("PDerror", e.toString()).build();
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("PDerror", ErrorCodes.READ_ERROR.toString()).build();
        }
    }

    /*
    @GetMapping("/email/{email}")
    public ResponseEntity<Object> getByEmail(@PathVariable String email) {



        logger.info("Fetching account by email: {}", email);
        try {
            //initial check
            if(!DaoAccountsUtility.validateEmail(email)){
                logger.error("Email format is not correct: " + email);
                return ResponseEntity.badRequest().build();
            }

            List<DtoAccounts> accounts = daoAccounts.read(email);


            if (accounts == null || accounts.isEmpty()) {
                logger.warn("No account found with email: {}", email);
                return ResponseEntity.status(400).body("Error: " + "Account not found");
            }


            DtoAccounts account = accounts.get(0);
            DtoAccountsRS accountRS = new DtoAccountsRS(
                    account.getEmail(),
                    account.getPassword(),
                    account.getRole(),
                    account.getStatus()
            );
            accountRS.setIdAccount(account.getIdAccount());

            return ResponseEntity.ok(accountRS);
        } catch (DaoException e) {
            logger.error("Error fetching account by email: ", e.getMessage());
            return ResponseEntity.status(400).body("ErrorDao: Could not get account with email: " + email );
        } catch (Exception e) {
            logger.error("Unexpected error: ", e.getMessage());
            return ResponseEntity.status(500).body("Error: Internal server error");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAccountById(@PathVariable Integer id) {

        logger.info("Fetching account by ID: {}", id);
        try {
            //initial check
            if(id <= 0 ){
                logger.error("ID cannot be a negative integer");
                return ResponseEntity.badRequest().build();
            }

            DtoAccounts account = daoAccounts.search(id);
            if (account== null ) {
                logger.warn("No account found with id: {}", id);
                return ResponseEntity.status(404).body("Error: Account not found");
            }
            DtoAccountsRS accountRS = new DtoAccountsRS(
                    account.getEmail(),
                    account.getPassword(),
                    account.getRole(),
                    account.getStatus()
            );
            return ResponseEntity.ok(accountRS);
        } catch (DaoException e) {
            logger.error("Error fetching account with ID {}: " + id + e.getMessage());
            return ResponseEntity.status(404).body("ErrorDao: Could not get account with id: " + id);
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(500).body("Error: Internal server error");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchAccounts(@RequestParam String filter) {

        //initial check da fare dopo che aggiungio i enum

        logger.info("Searching accounts via /accounts/search?filter= with filter: {}", filter);
        try {
            List<DtoAccounts> accounts = daoAccounts.read(filter);
            List<DtoAccountsRS> accountsRS = accounts.stream()
                    .map(account -> new DtoAccountsRS(
                            account.getEmail(),
                            account.getPassword(),
                            account.getRole(),
                            account.getStatus()
                    ))
                    .collect(Collectors.toList());
            logger.info("Accounts found: {}", accountsRS.size());
            return ResponseEntity.ok(accountsRS);
        } catch (DaoException e) {
            logger.error("Error searching accounts: " + e.getMessage());
            return ResponseEntity.status(400).body("ErrorDao: Could not get accounts with filter : "+filter);
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: Internal Server Error");
        }
    }

    @PostMapping
    public ResponseEntity<String> createAccount(@RequestBody DtoAccountsRS dtoAccountRS) {
        logger.info("Creating a new account");
        try {
            DtoAccounts dtoAccount = new DtoAccounts(
                    dtoAccountRS.getEmail(),
                    dtoAccountRS.getPassword(),
                    dtoAccountRS.getRole(),
                    dtoAccountRS.getStatus()
            );

            if(!DaoAccountsUtility.verifyCredentials(dtoAccount)){
                logger.error("Credentials are not in a valid format");
                return ResponseEntity.badRequest().build();
            }
            if(!DaoAccountsUtility.validateRole(dtoAccount.getRole()) || !DaoAccountsUtility.validateStatus(dtoAccount.getStatus())){
                logger.error("Status or role does not exist");
                return ResponseEntity.badRequest().build();
            }

            Integer generatedId = daoAccounts.create(dtoAccount);
            logger.info("Account created successfully with ID: {}", generatedId);
            return ResponseEntity.status(201).body("Account created with ID: " + generatedId);
        } catch (DaoException e) {
            logger.error("Error creating account: ", e.getMessage());
            return ResponseEntity.status(400).body("ErrorDao: Create account failed!");
        } catch (Exception e) {
            logger.error("Unexpected error: ", e.getMessage());
            return ResponseEntity.status(500).body("Error: Internal Server Error ");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateAccount(@PathVariable Integer id, @RequestBody DtoAccountsRS dtoAccountRS) {
        logger.info("Updating account with ID: {}", id);
        try {

            DtoAccounts dtoAccount = new DtoAccounts(
                    id,
                    dtoAccountRS.getEmail(),
                    dtoAccountRS.getPassword(),
                    dtoAccountRS.getRole(),
                    dtoAccountRS.getStatus()
            );

            if(!DaoAccountsUtility.verifyCredentials(dtoAccount)){
                logger.error("Credentials are not in a valid format");
                return ResponseEntity.badRequest().build();
            }
            if(!DaoAccountsUtility.validateRole(dtoAccount.getRole()) || !DaoAccountsUtility.validateStatus(dtoAccount.getStatus())){
                logger.error("Status or role does not exist");
                return ResponseEntity.badRequest().build();
            }

            int rowsAffected = daoAccounts.update(dtoAccount, id);

            if (rowsAffected > 0) {
                logger.info("Account updated successfully.");
                return ResponseEntity.ok("Account updated successfully.");
            } else {
                logger.warn("Account with ID {} not found.", id);
                return ResponseEntity.status(404).body("Error: Account not found");
            }
        } catch (DaoException e) {
            logger.error("Error updating account: ", e.getMessage());
            return ResponseEntity.status(400).body("ErrorDao: Failed to modify account.");
        } catch (Exception e) {
            logger.error("Unexpected error: ", e.getMessage());
            return ResponseEntity.status(500).body("Error: Internal Server Error.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Integer id) {

        logger.info("Deleting account with ID: {}", id);
        try {

            //initial check
            if(id <= 0 ){
                logger.error("ID cannot be a negative integer");
                return ResponseEntity.badRequest().build();
            }


            int rowsDeleted = daoAccounts.delete(id);
            if (rowsDeleted > 0) {
                logger.info("Account deleted successfully.");
                return ResponseEntity.ok("Account deleted successfully.");

            } else {
                logger.warn("Account with ID {} not found.", id);
                return ResponseEntity.status(404).body("Error: Account not found");
            }
        } catch (DaoException e) {
            logger.error("Error deleting account: ",  e.getMessage());
            return ResponseEntity.status(400).body("ErrorDao: Could not delete account with id:" + id );
        } catch (Exception e) {
            logger.error("Unexpected error: ", e.getMessage());
            return ResponseEntity.status(500).body("Error: Internal Server error.");
        }
    }

     */
}
