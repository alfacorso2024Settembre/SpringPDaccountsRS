package it.alfasoft.myPersonalDriver.SpringPDaccountsRS.dao;

import it.alfasoft.myPersonalDriver.common.dao.dto.DtoAccounts;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DaoAccountsUtility {

        public static boolean validatePassword(String password) {
            if (password.length() < 8) {
                return false;
            }
            if (!password.matches(".*[A-Z].*")) {
                return false;
            }
            if (!password.matches(".*\\d.*")) {
                return false;
            }
            if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                return false;
            }
            return true;
        }

        public static boolean validateEmail(String email) {
            String emailRegex = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
            Pattern pattern = Pattern.compile(emailRegex);
            Matcher matcher = pattern.matcher(email);
            return matcher.matches();
        }

        public static boolean validatePhoneNumber(String phoneNumber) {
            String phoneRegex = "^(\\+39|0039)\\d{9,10}$";
            Pattern pattern = Pattern.compile(phoneRegex);
            Matcher matcher = pattern.matcher(phoneNumber);
            return matcher.matches();
        }

        public static boolean validateDateOfBirth(String dateOfBirth) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate birthDate = LocalDate.parse(dateOfBirth, formatter);
                LocalDate today = LocalDate.now();
                int age = today.getYear() - birthDate.getYear();
                if (today.getDayOfYear() < birthDate.getDayOfYear()) {
                    age--;
                }
                return age >= 18;
            } catch (DateTimeParseException e) {
                return false;
            }
        }

        public static boolean verifyCredentials(DtoAccounts acc){
            boolean verif = false;
            verif = DaoAccountsUtility.validateEmail(acc.getEmail());
            verif = DaoAccountsUtility.validatePassword(acc.getPassword());
            return verif;
        }





}
