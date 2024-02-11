package com.database.yoober.yoober_app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component
public class UserRegistrationHandler {

    private static final Logger log = LogManager.getLogger(UserRegistrationHandler.class);

    public int registerUser(Connection connection, String firstName, String lastName,
            String birthdate, String phoneNumber, String emailAddress, String streetAddress, String city,
            String province, String postalCode) throws Exception {
        try {
            return insertPassenger(connection, firstName, lastName, birthdate,
                    phoneNumber, emailAddress,
                    saveAddress(connection, new Address(streetAddress, city, province, postalCode)));

        } catch (SQLException | UserRegistrationException e) {
            // Log the exception 
            log.error("Failed to register user", e);
            throw e;
        } catch (Exception e){
            log.error("Failed to register user", e);
            throw e;
        }
    }

    public int saveAddress(Connection connection, Address destinationAddress) throws UserRegistrationException {
        String insertQuery = "INSERT INTO address (street, city, province, postal_code) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery,
                Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, destinationAddress.getStreet());
            preparedStatement.setString(2, destinationAddress.getCity());
            preparedStatement.setString(3, destinationAddress.getProvince());
            preparedStatement.setString(4, destinationAddress.getPostal_code());

            // Execute the insert statement
            preparedStatement.executeUpdate();

            // Retrieve the generated address ID
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to save address", e);
            // Handle SQL exception 
            throw new UserRegistrationException("Failed to Save Adrress");
        }
        return -1;
    }

    public void saveCreditCard(Connection connection, int userId, String creditCardNumber) throws Exception {
        String sql = "INSERT INTO credit_card (credit_card_number, user_id) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, creditCardNumber);
            statement.setInt(2, userId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1) {
                throw new UserRegistrationException("Failed to add credit card details");
            }
        } catch (SQLException | UserRegistrationException e) {
            throw e;
        }
    }

    public void insertDriver(Connection connection, int userId, String licenseNumber, String licenseExpiryDate)
            throws Exception {
        String sql = "INSERT INTO driver (license_number, expiry_date, status, user_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, licenseNumber);
            statement.setString(2, licenseExpiryDate);
            statement.setString(3, YooberConstants.ACTIVE);
            statement.setInt(4, userId);
            System.out.println(licenseExpiryDate);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected != 1) {
                throw new UserRegistrationException("Failed to insert driver details");
            }
        } catch (SQLException | UserRegistrationException e) {
            throw e;
        }
    }

    private int insertPassenger(Connection connection, String firstName, String lastName, String birthdate,
            String phoneNumber, String emailAddress, int addressId) throws SQLException, UserRegistrationException {
        String sql = "INSERT INTO user (first_name, last_name, birthday, email, phone_number, address_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, birthdate);
            statement.setString(4, emailAddress);
            statement.setString(5, phoneNumber);
            statement.setInt(6, addressId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 1) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // This is the generated address_id
                }
            } else {
                throw new UserRegistrationException("Failed to insert address");
            }
            return -1;
        } catch (SQLException | UserRegistrationException e) {
            throw e;
        }
    }

}
