package com.database.yoober.yoober_app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.util.Scanner;

@Component
public class BusinessLogic {

    @Value("${spring.datasource.url}")
    private String dataSourcePrefix;

    @Autowired
    private UserRegistrationHandler userRegistrationHandler;

    @Autowired
    private RideRequestHandler rideRequestHandler;

    private static final Logger log = LogManager.getLogger(BusinessLogic.class);

    public List<Map<String, String>> retrieveUserData() {
        // List to store the result set
        List<Map<String, String>> resultList = new ArrayList<>();

        // SQL query to retrieve user data with account type
        String sqlQuery = "SELECT " +
                "u.first_name, " +
                "u.last_name, " +
                "a.street, " +
                "a.city, " +
                "a.province, " +
                "a.postal_code, " +
                "u.phone_number, " +
                "u.email, " +
                "CASE " +
                "    WHEN d.user_id IS NOT NULL THEN 'Driver/Passenger' " +
                "    ELSE 'Passenger' " +
                "END AS account_type " +
                "FROM " +
                "user u " +
                "JOIN " +
                "address a ON u.address_id = a.address_id " +
                "LEFT JOIN " +
                "driver d ON u.user_id = d.user_id ";

        // Create a connection using the provided database URL
        try {
            Connection connection = DriverManager.getConnection(getDataBaseURL());

            // Use PreparedStatement to execute the SQL query with the given connection
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

                // Execute the query and process the result set
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // Map to store user data
                        Map<String, String> userMap = new HashMap<>();
                        userMap.put("First Name", resultSet.getString("first_name"));
                        userMap.put("Last Name", resultSet.getString("last_name"));
                        userMap.put("Street", resultSet.getString("street"));
                        userMap.put("City", resultSet.getString("city"));
                        userMap.put("Province", resultSet.getString("province"));
                        userMap.put("Postal Code", resultSet.getString("postal_code"));
                        userMap.put("Phone Number", resultSet.getString("phone_number"));
                        userMap.put("Email", resultSet.getString("email"));
                        userMap.put("Account Type", resultSet.getString("account_type"));
                        resultList.add(userMap);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Failed to fetch user INFO");
            e.printStackTrace();
        }

        return resultList;
    }

    /**
     * Calculates the average rating for a driver based on passenger ratings.
     *
     * @param driverEmail The email address of the driver.
     * @return A list of maps containing the driver's email and their average
     *         rating.
     */
    public List<Map<String, String>> calculateAverageRatingForDriver(String driverEmail) {
        // List to store the result
        List<Map<String, String>> resultList = new ArrayList<>();

        // SQL query to calculate average rating for the specified driver
        String sqlQuery = "select dt.email as DriverEmail, AVG(dt.passenger_rating) as AverageRating " +
                "from(" +
                "SELECT t.passenger_rating, u.email " +
                "FROM trip t " +
                "JOIN driver d ON t.driver_id = d.driver_id " +
                "JOIN user u ON d.user_id = u.user_id " +
                "WHERE u.email = ? and t.passenger_rating is not null )as dt " +
                "group by dt.email";

        try (Connection connection = DriverManager.getConnection(getDataBaseURL());
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            // Set the driver's email as a parameter in the prepared statement
            preparedStatement.setString(1, driverEmail);

            // Execute the query and process the result set
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // Retrieve the result for further processing
                    String resultDriverEmail = resultSet.getString("DriverEmail");
                    double averageRating = resultSet.getDouble("AverageRating");

                    // Process the result and add it to the list
                    Map<String, String> resultMap = new HashMap<>();
                    resultMap.put("DriverEmail", resultDriverEmail);
                    resultMap.put("AverageRating", String.valueOf(averageRating));
                    resultList.add(resultMap);
                }
            }
        } catch (SQLException e) {
            // Log error
            log.error("Failed to fetch average rating");
            e.printStackTrace();
        }

        return resultList;
    }

    public void createNewUserAccount() throws Exception {

        Scanner scanner = new Scanner(System.in);

        // Collect user input for creating a new account
        System.out.println("Enter First Name:");
        String firstName = scanner.nextLine();

        System.out.println("Enter Last Name:");
        String lastName = scanner.nextLine();

        System.out.println("Enter Birthdate (yyyy-MM-dd):");
        String birthdate = scanner.nextLine();

        System.out.println("Enter Phone Number:");
        String phoneNumber = scanner.nextLine();

        System.out.println("Enter Email Address:");
        String emailAddress = scanner.nextLine();

        System.out.println("Enter Street Address:");
        String streetAddress = scanner.nextLine();

        System.out.println("Enter City:");
        String city = scanner.nextLine();

        System.out.println("Enter Province:");
        String province = scanner.nextLine();

        System.out.println("Enter Postal Code:");
        String postalCode = scanner.nextLine();

        System.out
                .println("Will the new account be used by a passenger, driver, or both? (passenger/driver/both):");
        String accountType = scanner.nextLine().toLowerCase();
        Connection connection = DriverManager.getConnection(getDataBaseURL());

        try {
            // Disable auto-commit mode to allow manual transaction control
            connection.setAutoCommit(false);

            // Register the user and get the user ID
            int userId = userRegistrationHandler.registerUser(connection, accountType, firstName, lastName, birthdate,
                    phoneNumber, emailAddress, streetAddress, city, province, postalCode);

            if (userId != -1) {
                System.out.println("\nNew account created successfully.\n");

                if (accountType.equals("passenger") || accountType.equals("both")) {
                    // Process credit card details for passengers
                    System.out.println("Enter Credit Card Number:");
                    String creditCardNumber = scanner.nextLine();
                    userRegistrationHandler.saveCreditCard(connection, userId, creditCardNumber);
                    System.out.println("Passenger credit card details added successfully.");
                }
                if (accountType.equals("driver") || accountType.equals("both")) {
                    // Process driver details for drivers
                    System.out.println("Enter Driver's License Number:");
                    String licenseNumber = scanner.nextLine();
                    System.out.println("Enter Driver's License Expiry Date (yyyy-MM-dd):");
                    String licenseExpiryDateStr = scanner.nextLine();

                    userRegistrationHandler.insertDriver(connection, userId, licenseNumber, licenseExpiryDateStr);
                    System.out.println("Driver account created successfully.");

                }
                connection.commit();
                connection.setAutoCommit(true);
            } else {
                System.out.println("User registration failed. Please try again.");
            }

        } catch (Exception e) {
            // Log error
            log.error("Failed to create user account", e);
            e.printStackTrace();
            // Rollback the transaction
            connection.rollback();

        }

    }

    /**
     * Facilitates the process of a user requesting a ride. Guides the user through
     * entering necessary details,
     * choosing from favorite destinations, or entering a new destination. After
     * obtaining all required information,
     * the method inserts a ride request into the database and assigns a driver for
     * the requested ride.
     *
     * @throws Exception if any error occurs during the process.
     */
    public void requestRide() {
        try (Connection connection = DriverManager.getConnection(getDataBaseURL())) {
            Scanner scanner = new Scanner(System.in);

            int destinationAddressId = -1;
            boolean passengerExists = false;
            String passengerEmail;

            // Step 1: Prompt user for passenger email until a valid email is provided
            do {
                System.out.print("Enter the email address of the passenger: ");
                passengerEmail = scanner.nextLine();
                // Check if passenger exists
                passengerExists = checkUserExists(passengerEmail);
                if (!passengerExists)
                    System.out.println("You have entered an invalid email Id\n");
            } while (!passengerExists);

            // Get user ID based on email
            int userId = rideRequestHandler.getUserIdByEmail(connection, passengerEmail);

            // Prompt if user want to choose from favorite destinations
            System.out.print("Do you want to choose from your favorite destinations? (yes/no): ");
            String choice = scanner.nextLine();

            // Step 2: Choose from favorite destinations or enter new destination details
            if (choice.equalsIgnoreCase("yes")) {
                // Display and choose from favorite destinations (retrieve from the database)
                destinationAddressId = rideRequestHandler.displayAndChooseFavoriteDestinations(connection,
                        passengerEmail);
            }
            if ((choice.equalsIgnoreCase("no")) || (choice.equalsIgnoreCase("yes") && destinationAddressId == -1)) {
                // Enter new destination details
                Address destinationAddress = rideRequestHandler.getDestinationDetails();
                destinationAddressId = userRegistrationHandler.saveAddress(connection, destinationAddress);

                // Ask if they want to make it a favorite
                System.out.print("Do you want to make this destination a new favorite destination? (yes/no): ");
                String makeFavoriteChoice = scanner.nextLine();

                if (makeFavoriteChoice.equalsIgnoreCase("yes")) {
                    System.out.print("Enter Custom name for this favorite destination: ");
                    String customName = scanner.nextLine();
                    rideRequestHandler.addNewFavoriteDestination(connection, userId, destinationAddressId, customName);
                }
            }

            // Step 3: Enter Additional Details
            String pickupDateTime;
            String formattedDate;

            System.out.print("Enter Pickup date and time (YYYY-MM-DD HH:MM:SS): ");
            do {
                pickupDateTime = scanner.nextLine();
                formattedDate = DateUtil.validateDateFormat(pickupDateTime);
            } while (formattedDate.equals("Incorrect Format"));
            System.out.print("Enter the number of passengers: ");
            int totalRiders = scanner.nextInt();

            // Step 4: Insert into Database
            int request_id = rideRequestHandler.insertRideRequestIntoDatabase(connection, userId, destinationAddressId,
                    formattedDate,
                    totalRiders);

            // Step 5: Assign a driver to the ride request
            rideRequestHandler.assignDriver(connection, formattedDate, request_id);
        } catch (Exception e) {
            log.error("Failed to post ride request", e);
            e.printStackTrace();
        }
    }

    /**
     * Allows a user to complete a ride by selecting from a list of uncompleted
     * rides, providing necessary details such as end date & time, total distance,
     * amount
     * charged,driver's rating, and passenger's rating.
     *
     * @throws SQLException            if a database access error occurs.
     * @throws RideCompletionException if an error occurs during the ride completion
     *                                 process.
     */
    public void completeRide() {
        try (Connection connection = DriverManager.getConnection(getDataBaseURL())) {

            String uncompletedRidesQuery = "SELECT trip_id, u.first_name AS passenger_first_name, u.last_name AS passenger_last_name, pickup.street AS pickup_street, pickup.city AS pickup_city, dropoff.street AS dropoff_street, dropoff.city AS dropoff_city, tr.pick_up_date AS pickup_date_time "
                    +
                    "FROM trip_request tr " +
                    "JOIN trip t ON t.request_id = tr.request_id " +
                    "JOIN address AS dropoff ON dropoff.address_id = tr.drop_off_address_id " +
                    "JOIN address AS pickup ON pickup.address_id = tr.pick_up_address_id " +
                    "JOIN user u ON u.user_id = tr.user_id " +
                    "WHERE t.end_date_time IS NULL OR t.end_date_time = '' ";
            try (PreparedStatement uncompletedRidesStatement = connection.prepareStatement(uncompletedRidesQuery);
                    ResultSet uncompletedRidesResult = uncompletedRidesStatement.executeQuery()) {

                // Display details of uncompleted rides
                System.out.println("Uncompleted Rides:");

                System.out.printf("%-8s%-20s%-30s%-30s%-30s%n",
                        "Trip Id", "Passenger Name",
                        "Pickup Address",
                        "Destination Address",
                        "Pickup Date Time");

                while (uncompletedRidesResult.next()) {
                    int tripId = uncompletedRidesResult.getInt("trip_id");
                    String passengerFirstName = uncompletedRidesResult.getString("passenger_first_name");
                    String passengerLastName = uncompletedRidesResult.getString("passenger_last_name");
                    String pickupStreet = uncompletedRidesResult.getString("pickup_street");
                    String pickupCity = uncompletedRidesResult.getString("pickup_city");
                    String destinationStreet = uncompletedRidesResult.getString("dropoff_street");
                    String destinationCity = uncompletedRidesResult.getString("dropoff_city");
                    String pickupDateTime = uncompletedRidesResult.getString("pickup_date_time");

                    System.out.printf("%-8d%-20s%-30s%-30s%-30s%n",
                            tripId, passengerFirstName + " " + passengerLastName,
                            pickupStreet + ", " + pickupCity,
                            destinationStreet + ", " + destinationCity,
                            pickupDateTime);
                }

                // Prompt user to complete a ride
                Scanner scanner = new Scanner(System.in);
                System.out.print("\nEnter the ID of the ride you want to complete (or 0 to exit): ");
                int chosenRideId = scanner.nextInt();

                if (chosenRideId != -1) {

                    System.out.print("Enter the end date & time (YYYY-MM-DD HH:mm:ss): ");
                    scanner.nextLine(); // Consume the newline character
                    String endDate = scanner.nextLine();

                    System.out.print("Enter the total distance: ");
                    int distance = scanner.nextInt();

                    System.out.print("Enter the amount charged: ");
                    int cost = scanner.nextInt();

                    System.out.print("Enter the driver's rating (1-5): ");
                    int driverRating = scanner.nextInt();

                    System.out.print("Enter the passenger's rating (1-5): ");
                    int passengerRating = scanner.nextInt();

                    // Example method to update the completed ride in the database
                    updateRideAsCompleted(connection, chosenRideId, endDate,
                            distance, cost, driverRating, passengerRating);

                }
            }

            // Close the connection
            connection.close();

        } catch (SQLException | RideCompletionException e) {
            e.printStackTrace();

        }

    }

    private static void updateRideAsCompleted(Connection connection, int rideId, String endDate,
            int distance, int cost, int driverRating, int passengerRating)
            throws SQLException, RideCompletionException {

        String updateRideQuery = "UPDATE trip SET end_date_time = ?, total_distance = ?, amount_charged = ?, " +
                "driver_rating = ?, passenger_rating = ? " +
                "WHERE trip_id = ?";

        try (PreparedStatement updateRideStatement = connection.prepareStatement(updateRideQuery)) {
            updateRideStatement.setString(1, endDate);
            updateRideStatement.setInt(2, distance);
            updateRideStatement.setInt(3, cost);
            updateRideStatement.setInt(4, driverRating);
            updateRideStatement.setInt(5, passengerRating);
            updateRideStatement.setInt(6, rideId);

            int rowsAffected = updateRideStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Ride completed successfully!");
            } else {
                System.out.println("Failed to complete the ride. Please check the provided details.");
            }
        } catch (SQLException e) {
            throw new RideCompletionException("Failed to end the ride", e);

        }

    }

    /**
     * Calculates the total amount charged for a passenger based on completed trips.
     * Retrieves the passenger's email and the sum of amounts charged from the
     * database.
     *
     * @param passengerEmail The email address of the passenger for whom the total
     *                       amount is calculated.
     * @return A List of Maps containing the result, where each Map includes keys
     *         "PassengerEmail"
     *         and "TotalAmountCharged" with corresponding values.
     */
    public List<Map<String, String>> calculateTotalAmountChargedForPassenger(
            String passengerEmail) {
        List<Map<String, String>> resultList = new ArrayList<>();

        String sqlQuery = "SELECT " +
                "    u.email AS passenger_email, " +
                "    SUM(t.amount_charged) AS total_amount_charged " +
                "FROM " +
                "    user u " +
                "JOIN " +
                "    trip_request tr ON u.user_id = tr.user_id " +
                "JOIN " +
                "    trip t ON tr.request_id = t.request_id " +
                "WHERE " +
                "    u.email = ?";

        try (Connection connection = DriverManager.getConnection(getDataBaseURL());
                PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {

            preparedStatement.setString(1, passengerEmail);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String resultPassengerEmail = resultSet.getString("passenger_email");
                    double totalAmountCharged = resultSet.getDouble("total_amount_charged");

                    // Process the result if needed
                    Map<String, String> resultMap = new HashMap<>();
                    resultMap.put("PassengerEmail", resultPassengerEmail);
                    resultMap.put("TotalAmountCharged", String.valueOf(totalAmountCharged));
                    resultList.add(resultMap);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch total amount charged");
            e.printStackTrace();
        }

        return resultList;
    }

    private String getDataBaseURL() {
        Path filePath = Paths.get( "src", "main", "resources",
                "Yoober_DB_group10.db");
        return dataSourcePrefix + filePath.toAbsolutePath().toString();
    }

    /**
     * Check if a user with the given email exists in the database.
     * 
     * @param userEmail The email of the user to check.
     * @return True if the user exists, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    public boolean checkUserExists(String userEmail) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (Connection connection = DriverManager.getConnection(getDataBaseURL());
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userEmail);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
        return false;
    }

    /**
     * Check if a driver with the given email exists in the database.
     * 
     * @param driverEmail The email of the driver to check.
     * @return True if the driver exists, false otherwise.
     * @throws SQLException If a database error occurs.
     */
    public boolean checkDriverExists(String driverEmail) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user u join driver d on d.user_id = u.user_id WHERE u.email = ? and d.user_id IS NOT NULL";
        try (Connection connection = DriverManager.getConnection(getDataBaseURL());
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, driverEmail);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        }
        return false;
    }

}
