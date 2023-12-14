package com.database.yoober.yoober_app;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class UIService {

    public void displayAccountInfo(List<Map<String, String>> resultList) {
        // Process the resultList as needed
        if (resultList.isEmpty()) {
            System.out.println("Sorry! You have entered an invalid user ID");
        } else {
            System.out.println("User Details:");
            System.out.println("-------------");
            // Iterate over the list of user details
            System.out.println(
                    "+--------------------+---------------------------------------------------------+-------------------+---------------------------+------------------+");
            System.out.println( 
                    "|        Name        |                        Full Address                     |   Phone Number    |          Email            |   Account Type   |");
            System.out.println(
                    "+--------------------+---------------------------------------------------------+-------------------+---------------------------+----------- ------+");

            for (Map<String, String> userMap : resultList) {
                System.out.printf("| %-18s | %-55s | %-17s | %-25s | %-16s |%n",
                        userMap.get("First Name") + " " + userMap.get("Last Name"),
                        userMap.get("Street") + ", " + userMap.get("City") + ", " + userMap.get("Province") + ", "
                                + userMap.get("Postal Code"),
                        userMap.get("Phone Number"),
                        userMap.get("Email"),
                        userMap.get("Account Type"));
                System.out.println(
                     "+--------------------+---------------------------------------------------------+--------------------+---------------------------+----------------+");                        
            }
        }
    }

    

    /**
     * Displays the rating information for a driver based on the provided result
     * list.
     *
     * @param resultList A list of maps containing the driver's email and their
     *                   average rating.
     */
    public void displayDriverRating(List<Map<String, String>> resultList) {
        // Check if the result list is empty
        if (resultList.isEmpty()) {
            System.out.println("No data found for the specified driver email.");
        } else {
            for (Map<String, String> userMap : resultList) {
                System.out.println("\nDriver Rating");
                System.out.println("----------------");

                // Display Driver's Email and Average Rating
                for (Map.Entry<String, String> entry : userMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    System.out.println(key + ": " + value);
                }
            }
        }
    }

    public void displayTotalAmountCharged(List<Map<String, String>> resultList) {

        for (Map<String, String> resultMap : resultList) {
            if (resultMap.get("PassengerEmail") == null) {
                System.out.println("No ride info found for the specified passenger email.");
                break;
            }
            System.out.println("\nTotal money spent by passenger");
            System.out.println("----------------------------------");
            System.out.println("Passenger Email: " + resultMap.get("PassengerEmail"));
            System.out.println("Total Amount Charged: $" + resultMap.get("TotalAmountCharged"));
        }
    }
}
