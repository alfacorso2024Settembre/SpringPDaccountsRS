package it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao;

import it.alfasoft.myPersonalDriver.common.dao.DaoException;
import it.alfasoft.myPersonalDriver.common.dao.ICrud;
import it.alfasoft.myPersonalDriver.common.dao.dto.DtoAccounts;
import it.alfasoft.myPersonalDriver.common.dao.dto.RoleType;
import it.alfasoft.myPersonalDriver.common.dao.dto.StatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class DaoAccounts implements ICrud<DtoAccounts, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(DaoAccounts.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DaoAccounts(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<DtoAccounts> read() throws DaoException {
        logger.info("Fetching all accounts...");
        String sql = "SELECT * FROM accounts";

        try {
            List<DtoAccounts> accounts = jdbcTemplate.query(sql, (rs, rowNum) ->
                    new DtoAccounts(
                            rs.getInt("idAccount"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("idRole"),
                            rs.getString("idStatus")
                    )
            );
            logger.info("Number of accounts fetched: {}", accounts.size());
            return accounts;
        } catch (Exception e) {
            logger.error("Error fetching all accounts: ", e);
            throw new DaoException("Unable to fetch accounts"+ e.getMessage());
        }
    }

    @Override
    public List<DtoAccounts> read(int i, int i1) throws DaoException {
        return List.of();
    }

    @Override
    public List<DtoAccounts> read(DtoAccounts dtoAccounts) throws DaoException {
        return List.of();
    }

    @Override
    public DtoAccounts search(Integer idAccount) throws DaoException {
        logger.info("Searching account by ID: {}", idAccount);
        String sql = "SELECT * FROM accounts WHERE idAccount = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new DtoAccounts(
                            rs.getInt("idAccount"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("idRole"),
                            rs.getString("idStatus")
                    ), idAccount);
        } catch (Exception e) {
            logger.error("Error fetching account with ID {}: ", idAccount, e);
            throw new DaoException("Unable to find account with ID: " + idAccount +" "+  e.getMessage());
        }
    }

    @Override
    public Integer create(DtoAccounts dtoAccount) throws DaoException {
        logger.info("Creating a new account: {}", dtoAccount);
        String sql = "INSERT INTO accounts (email, password, idRole, idStatus) VALUES (?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql,
                    dtoAccount.getEmail(),
                    dtoAccount.getPassword(),
                    RoleType.valueOf(dtoAccount.getRole()).getId(),
                    StatusType.valueOf(dtoAccount.getStatus()).getId()
            );
            logger.info("Account successfully created.");
            // Fetch the last inserted ID (if needed).
            Integer id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
            return id;
        } catch (Exception e) {
            logger.error("Error creating account: ", e);
            throw new DaoException("Unable to create account"+ e.getMessage());
        }
    }

    @Override
    public int update(DtoAccounts dtoAccount, Integer idAccount) throws DaoException {
        logger.info("Updating account with ID: {}", idAccount);
        String sql = "UPDATE accounts SET email = ?, password = ?, idRole = ?, idStatus = ? WHERE idAccount = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql,
                    dtoAccount.getEmail(),
                    dtoAccount.getPassword(),
                    RoleType.valueOf(dtoAccount.getRole()).getId(),
                    StatusType.valueOf(dtoAccount.getStatus()).getId(),
                    idAccount
            );
            logger.info("Account updated. Rows affected: {}", rowsAffected);
            return rowsAffected;
        } catch (Exception e) {
            logger.error("Error updating account with ID {}: ", idAccount, e);
            throw new DaoException("Unable to update account"+ e.getMessage());
        }
    }

    @Override
    public int delete(Integer idAccount) throws DaoException {
        logger.info("Deleting account with ID: {}", idAccount);
        String sql = "DELETE FROM accounts WHERE idAccount = ?";
        try {
            int rowsDeleted = jdbcTemplate.update(sql, idAccount);
            logger.info("Account deleted. Rows affected: {}", rowsDeleted);
            return rowsDeleted;
        } catch (Exception e) {
            logger.error("Error deleting account with ID {}: ", idAccount, e);
            throw new DaoException("Unable to delete account"+ e.getMessage());
        }
    }

    public List<DtoAccounts> read(String textSearch) throws DaoException {
        logger.info("Searching accounts with criteria: {}", textSearch);
        String sql;
        Object[] params;

        // Determine the search criteria
        if (Arrays.stream(RoleType.values()).anyMatch(t -> t.name().equals(textSearch))) {
            sql = "SELECT * FROM accounts WHERE idRole = ?";
            params = new Object[]{RoleType.valueOf(textSearch).getId()};
        } else if (Arrays.stream(StatusType.values()).anyMatch(t -> t.name().equals(textSearch))) {
            sql = "SELECT * FROM accounts WHERE idStatus = ?";
            params = new Object[]{StatusType.valueOf(textSearch).getId()};
        } else if (textSearch.matches(".*@.*")) {
            sql = "SELECT * FROM accounts WHERE email = ?";
            params = new Object[]{textSearch};
        } else {
            throw new DaoException("Invalid search criteria: " + textSearch);
        }

        try {
            List<DtoAccounts> accounts = jdbcTemplate.query(sql, (rs, rowNum) ->
                    new DtoAccounts(
                            rs.getInt("idAccount"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("idRole"),
                            rs.getString("idStatus")
                    ), params);
            logger.info("Number of accounts found: {}", accounts.size());
            return accounts;
        } catch (Exception e) {
            logger.error("Error searching accounts: "+ e.getMessage());
            throw new DaoException("Unable to search accounts" + e.getMessage());
        }
    }
}
