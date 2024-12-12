package it.alfasoft.myPersonalDriver.SpringPDaccountsRS;

import it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao.DaoAccounts;
import it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao.DtoAccountsRS;
import it.alfasoft.myPersonalDriver.common.dao.DaoException;
import it.alfasoft.myPersonalDriver.common.dao.dto.DtoAccounts;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<Object> getAllAccounts() {
        logger.info("Fetching all accounts via /accounts.");
        try {
            List<DtoAccounts> accounts = daoAccounts.read();
            //per non esporere l id
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
            logger.error("ErrorDao: ", e.getMessage());
            return ResponseEntity.status(400).body("ErrorDao: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error: ", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }
    @GetMapping("/email/{email}")
    public ResponseEntity<Object> getByEmail(@PathVariable String email) {
        logger.info("Fetching account by email: {}", email);
        try {
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
            return ResponseEntity.status(400).body("ErrorDao: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAccountById(@PathVariable Integer id) {
        logger.info("Fetching account by ID: {}", id);
        try {
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
            return ResponseEntity.status(404).body("ErrorDao: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchAccounts(@RequestParam String filter) {
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
            return ResponseEntity.status(400).body("ErrorDao: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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
            Integer generatedId = daoAccounts.create(dtoAccount);
            logger.info("Account created successfully with ID: {}", generatedId);
            return ResponseEntity.status(201).body("Account created with ID: " + generatedId);
        } catch (DaoException e) {
            logger.error("Error creating account: ", e);
            return ResponseEntity.status(400).body("ErrorDao: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
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
            int rowsAffected = daoAccounts.update(dtoAccount, id);

            if (rowsAffected > 0) {
                logger.info("Account updated successfully.");
                return ResponseEntity.ok("Account updated successfully.");
            } else {
                logger.warn("Account with ID {} not found.", id);
                return ResponseEntity.status(404).body("Error: Account not found");
            }
        } catch (DaoException e) {
            logger.error("Error updating account: ", e);
            return ResponseEntity.status(400).body("ErrorDao: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Integer id) {

        logger.info("Deleting account with ID: {}", id);
        try {
            int rowsDeleted = daoAccounts.delete(id);
            if (rowsDeleted > 0) {
                logger.info("Account deleted successfully.");
                return ResponseEntity.ok("Account deleted successfully.");

            } else {
                logger.warn("Account with ID {} not found.", id);
                return ResponseEntity.status(404).body("Error: Account not found");
            }
        } catch (DaoException e) {
            logger.error("Error deleting account: ", e);
            return ResponseEntity.status(400).body("ErrorDao: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
