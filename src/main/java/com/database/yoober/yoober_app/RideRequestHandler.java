package com.database.yoober.yoober_app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.springframework.stereotype.Component;

@Component
public class RideRequestHandler {

    public int displayAndChooseFavoriteDestinations(Connection connection, String passengerEmail)
            throws RideRequestException {

        Scanner s = new Scanner(System.in);

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT favourite_destination_id, custom_name, street, city, province, postal_code, favourite_destination.address_id "
                        +
                        "FROM favourite_destination " +
                        "JOIN address ON favourite_destination.address_id = address.address_id " +
                        "WHERE user_id = (SELECT user_id FROM user WHERE email = ?)")) {
            statement.setString(1, passengerEmail);

            ResultSet resultSet = statement.executeQuery();
            List<Map<String, String>> resultList = new ArrayList<>();

            while (resultSet.next()) {
                Map<String, String> userMap = new HashMap<>();
                userMap.put("id", String.valueOf(resultSet.getInt("favourite_destination_id")));
                userMap.put("custom name", resultSet.getString("custom_name"));
                userMap.put("fullAddress", resultSet.getString("street") + ", " +
                        resultSet.getString("city") + ", " +
                        resultSet.getString("province") + ", " +
                        resultSet.getString("postal_code"));
                userMap.put("address_id", resultSet.getString("address_id"));
                resultList.add(userMap);
                System.out.println("ID: " + userMap.get("id") + ", \nName: " + userMap.get("custom name")
                        + ", \nAddress: " + userMap.get("fullAddress"));
            }
            if (resultList.isEmpty()) {
                System.out.print("No favorite destinations found for this user, Please enter the destination manually\n");
                return -1;
            }

            System.out.print("Enter the ID of your chosen destination: ");
            int chosenDestinationId = s.nextInt();
            return resultList.stream()
                    .filter(userMap -> Integer.parseInt(userMap.get("id")) == chosenDestinationId)
                    .map(userMap -> Integer.parseInt(userMap.get("address_id")))
                    .findFirst()
                    .orElse(-1);
        } catch (SQLException e) {
            throw new RideRequestException("Could not fetch favourite destination", e);
        }
    }

    public int insertRideRequestIntoDatabase(Connection connection, int userId, int dropOffAddressId,
            String pickupDateTime,
            int totalRiders) throws RideRequestException {
        // SQL query to insert a new ride request
        String sql = "INSERT INTO trip_request (pick_up_address_id, drop_off_address_id, pick_up_date, number_of_passengers, user_id) VALUES (?, ?, ?, ?, ?)";

        int addresId = getPickUpAddressId(connection, userId);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // Set values for the parameters

            preparedStatement.setInt(1, addresId);
            preparedStatement.setInt(2, dropOffAddressId);

            preparedStatement.setString(3, pickupDateTime);

            preparedStatement.setInt(4, totalRiders);
            preparedStatement.setInt(5, userId);

            // Execute the update
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            // Close the connection
            connection.close();
        } catch (SQLException e) {
            throw new RideRequestException("Failed to add ride Request", e);
        }
        return -1;
    }

    private int getPickUpAddressId(Connection connection, int userId) {
        // SQL query to retrieve address_id based on user_id
        String sql = "SELECT address_id FROM user WHERE user_id = ?";
        int addressId = -1; // Default value if the user or address is not found

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // Set the user_id as a parameter in the query
            preparedStatement.setInt(1, userId);

            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if a record is found
                if (resultSet.next()) {
                    // Retrieve the address_id from the result set
                    addressId = resultSet.getInt("address_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }

        return addressId;

    }

    public int addNewFavoriteDestination(Connection connection, int userId, int destinationId, String customName)
            throws RideRequestException {

        // Insert new favorite destination
        String insertQuery = "INSERT INTO favourite_destination (address_id, custom_name, user_id) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setInt(1, destinationId);
            preparedStatement.setString(2, customName);
            preparedStatement.setInt(3, userId);

            // Execute the insert statement
            preparedStatement.executeUpdate();
            System.out.println("New favourite destination has been added successfully");
        } catch (SQLException e) {
            throw new RideRequestException("Failed to add new favourite destination", e);
        }
        return userId;
    }

    public int getUserIdByEmail(Connection connection, String passengerEmail) throws SQLException {
        String selectQuery = "SELECT user_id FROM user WHERE email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, passengerEmail);

            // Execute the select statement
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the result set has a user ID
                if (resultSet.next()) {
                    return resultSet.getInt("user_id");
                }
            } catch (SQLException e) {
                // Handle SQL exception for result set
                e.printStackTrace();
                throw e;
            }
        } catch (SQLException e) {
            // Handle SQL exception for prepared statement
            e.printStackTrace();
            throw e;
        }

        return -1;
    }

    public void assignDriver(Connection connection, String pickupDateTime, int request_id) throws RideRequestException {
        try {
            // Find an available driver for the given pickup time
            int driverId = findAvailableDriver(connection, pickupDateTime);

            if (driverId != -1) {
                System.out.println("Available driver Id : "+ driverId+"\n");
                assignDriverToTrip(connection, driverId, request_id, pickupDateTime);
            } else {
                System.out.println("No available driver for the given pickup time.");
            }
        } catch (SQLException e) {
            throw new RideRequestException("Failed to assign driver for the request", e);
        }
    }

    private void assignDriverToTrip(Connection connection, int driverId, int request_id, String pickupDateTime)
            throws SQLException {
        String sql = "INSERT INTO trip (driver_id, request_id, start_date_time) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, driverId);
            statement.setInt(2, request_id);
            statement.setString(3, pickupDateTime);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Driver assigned to trip successfully.");
            } else {
                System.out.println("Error assigning driver to trip.");
            }
        }
    }

    private int findAvailableDriver(Connection connection, String pickupDateTime) throws SQLException {

        // Assuming you have a Timestamp object
        Timestamp timestamp = Timestamp.valueOf(pickupDateTime);

        // Convert the Timestamp to LocalTime
        LocalTime localTime = timestamp.toLocalDateTime().toLocalTime();

        String sql = "SELECT da.driver_id " +
                "FROM driver_availability da " +
                "JOIN availability a ON da.availability_id = a.availabilty_id " +
                "WHERE ? BETWEEN a.start_time AND a.end_time " +
                "AND strftime('%w', ?) + 1 BETWEEN " +
                "(CASE a.start_day_of_week " +
                "    WHEN 'Sunday' THEN 1 " +
                "    WHEN 'Monday' THEN 2 " +
                "    WHEN 'Tuesday' THEN 3 " +
                "    WHEN 'Wednesday' THEN 4 " +
                "    WHEN 'Thursday' THEN 5 " +
                "    WHEN 'Friday' THEN 6 " +
                "    WHEN 'Saturday' THEN 7 " +
                "    ELSE NULL " +
                " END) " +
                "AND " +
                "(CASE a.end_day_of_week " +
                "    WHEN 'Sunday' THEN 1 " +
                "    WHEN 'Monday' THEN 2 " +
                "    WHEN 'Tuesday' THEN 3 " +
                "    WHEN 'Wednesday' THEN 4 " +
                "    WHEN 'Thursday' THEN 5 " +
                "    WHEN 'Friday' THEN 6 " +
                "    WHEN 'Saturday' THEN 7 " +
                "    ELSE NULL " +
                " END)";

        String pickupTimeString = localTime.toString();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pickupTimeString);
            statement.setString(2, pickupDateTime);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int driver_id = resultSet.getInt("driver_id");
                    if (!isDriverInTrip(connection, driver_id))
                        return driver_id;
                }
                return -1;
            }
        }
    }

    private boolean isDriverInTrip(Connection connection, int driverId) throws SQLException {
        String sql = "SELECT 1 FROM trip WHERE driver_id = ? AND end_date_time IS NULL";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, driverId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Return true if the driver is in a trip
            }
        }
    }

    public Address getDestinationDetails() {

        Address destinationAddress = new Address();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter street: ");
        destinationAddress.setStreet(scanner.nextLine());
        System.out.print("Enter city: ");
        destinationAddress.setCity(scanner.nextLine());
        System.out.print("Enter province: ");
        destinationAddress.setProvince(scanner.nextLine());
        System.out.print("Enter postal code: ");
        destinationAddress.setPostal_code(scanner.nextLine());
        return destinationAddress;

    }

}
