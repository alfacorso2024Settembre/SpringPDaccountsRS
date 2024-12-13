package it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao;

import it.alfasoft.myPersonalDriver.common.Exceptions.DaoException;
import it.alfasoft.myPersonalDriver.common.Exceptions.ErrorCodes;
import it.alfasoft.myPersonalDriver.common.dao.ICrud;
import it.alfasoft.myPersonalDriver.common.dao.dto.DtoAccounts;
import it.alfasoft.myPersonalDriver.common.dao.dto.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
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
        }catch (CannotGetJdbcConnectionException ex) {
            throw new DaoException(ErrorCodes.CONNECTION_ERROR, "read()", ex.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching all accounts: ", e);
            throw new DaoException(ErrorCodes.READ_ERROR, "read" , e.getMessage());
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
    public List<DtoAccounts> read(String s) throws DaoException {
        return List.of();
    }

    @Override
    public Integer create(DtoAccounts dtoAccounts) throws DaoException {
        return 0;
    }

    @Override
    public int update(DtoAccounts dtoAccounts, Integer integer) throws DaoException {
        return 0;
    }

    @Override
    public int delete(Integer integer) throws DaoException {
        return 0;
    }

    @Override
    public DtoAccounts search(Integer integer) throws DaoException {
        return null;
    }
    public List<DtoAccounts> read(int offset, int limit, String textSearch) throws DaoException {
        logger.info("Fetching accounts with offset={}, limit={}, textSearch={}", offset, limit, textSearch);

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM accounts");
        List<Object> params = new ArrayList<>();


        if (textSearch != null && !textSearch.isEmpty()) {


            if (Arrays.stream(RoleType.values()).anyMatch(r -> r.name().equals(textSearch))) {
                sqlBuilder.append(" WHERE idRole = ?");
                params.add(RoleType.valueOf(textSearch).getId());


            } else if (Arrays.stream(StatusType.values()).anyMatch(s -> s.name().equals(textSearch))) {
                sqlBuilder.append(" WHERE idStatus = ?");
                params.add(StatusType.valueOf(textSearch).getId());


            } else if (textSearch.matches(".*@.*")) {
                sqlBuilder.append(" WHERE email = ?");
                params.add(textSearch);

            } else {

                logger.error("Invalid search criteria: {}", textSearch);
                throw new DaoException(ErrorCodes.INPUT_FORMAT_ERROR, "read()","Filter invalid format!");
            }
        }


        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        String sql = sqlBuilder.toString();
        logger.info("Final SQL Query: {}", sql);

        try {
            return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) ->
                    new DtoAccounts(
                            rs.getInt("idAccount"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("idRole"),
                            rs.getString("idStatus")
                    )
            );
        } catch (CannotGetJdbcConnectionException ex) {
            throw new DaoException(ErrorCodes.CONNECTION_ERROR, "read()", ex.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching accounts: ", e);
            throw new DaoException(ErrorCodes.READ_ERROR, "read", e.getMessage());
        }
    }

/*
    public List<DtoAccounts> read(int offset, int limit, String email, String role, String status) throws DaoException {
        logger.info("Fetching accounts with offset={}, limit={}, email={}, role={}, status={}",
                offset, limit, email, role, status);

        // Build the SQL query dynamically
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM accounts WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // -- Filter by email
        if (email != null && !email.isEmpty()) {
            sqlBuilder.append(" AND email LIKE ?");
            params.add("%" + email + "%");
        }

        // -- Filter by role: parse the string into enum, then use its id
        if (role != null && !role.isEmpty()) {
            try {
                RoleType roleEnum = RoleType.valueOf(role);  // e.g. "User", "Driver", "Admin"
                sqlBuilder.append(" AND idRole = ?");
                params.add(roleEnum.getId());               // Store the enum’s int value
            } catch (IllegalArgumentException e) {
                // This exception happens if the string doesn't match any enum constant
                logger.error("Invalid role specified: {}", role);
                throw new DaoException(ErrorCodes.READ_ERROR, "read",
                        "Invalid role specified: " + role);
            }
        }

        // -- Filter by status (assuming StatusType is also an enum with an integer ID)
        if (status != null && !status.isEmpty()) {
            try {
                StatusType statusEnum = StatusType.valueOf(status); // e.g. "Active", "Disabled", etc.
                sqlBuilder.append(" AND idStatus = ?");
                params.add(statusEnum.getId()); // or however you map statusEnum to its DB value
            } catch (IllegalArgumentException e) {
                logger.error("Invalid status specified: {}", status);
                throw new DaoException(ErrorCodes.READ_ERROR, "read",
                        "Invalid status specified: " + status);
            }
        }

        // -- Pagination
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        String sql = sqlBuilder.toString();
        logger.info("Final SQL Query: {}", sql);

        try {
            return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) ->
                    new DtoAccounts(
                            rs.getInt("idAccount"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("idRole"),
                            rs.getString("idStatus")
                    )
            );
        } catch (CannotGetJdbcConnectionException ex) {
            throw new DaoException(ErrorCodes.CONNECTION_ERROR, "read()", ex.getMessage());
        } catch (Exception e) {
            logger.error("Error fetching accounts: ", e);
            throw new DaoException(ErrorCodes.READ_ERROR, "read", e.getMessage());
        }
    }
*/
    /*
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

        if(!DaoAccountsUtility.verifyCredentials(dtoAccount)){
            logger.error("Dao:" + "email password format not valid");
            throw new DaoException("Credentials format not valid");
        }

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
     */
}
