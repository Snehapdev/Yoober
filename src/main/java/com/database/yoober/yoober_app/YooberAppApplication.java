package com.database.yoober.yoober_app;

import java.util.Scanner;

import javax.print.DocFlavor.STRING;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YooberAppApplication implements CommandLineRunner {

    private final MenuService menuService;
    private final BusinessLogic businessLogic;
    private final UIService uiService;

    private final Scanner scanner = new Scanner(System.in);

    @Autowired
    public YooberAppApplication(MenuService menuService, BusinessLogic businessLogic, UIService uiService) {
        this.menuService = menuService;
        this.businessLogic = businessLogic;
        this.uiService = uiService;
    }

    public static void main(String[] args) {
        SpringApplication.run(YooberAppApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        boolean continueMenu = true;
        menuService.displayWelcomeMessage();

        // Loop for displaying the menu and handling user choices
        while (continueMenu) {
            // Display the main menu
            menuService.displayMenu();

            // Get the user's choice
            int choice = menuService.getUserChoice();

            // Perform actions based on the user's choice
            switch (choice) {
                case 1:
                    // Fetch all account details
                    List<Map<String, String>> resultList = businessLogic.retrieveUserData();
                    // Display account details
                    uiService.displayAccountInfo(resultList);
                    break;
                case 2:
                    boolean driverExists = false;
                    String driverEmail;
                    do {
                        // Calculate average rating logic
                        System.out.print("Enter a valid driver's email ID: ");
                        driverEmail = scanner.next();
                        // Check if driver exists
                        driverExists = businessLogic.checkDriverExists(driverEmail);
                        if (!driverExists)
                            System.out.println("You have entered an invalid email Id\n");
                    } while (!driverExists);
                    
                    // Fetch Average Rating
                    List<Map<String, String>> resultListAvg = businessLogic
                            .calculateAverageRatingForDriver(driverEmail);
                    // Display Results
                    uiService.displayDriverRating(resultListAvg);
                    break;
                case 3:
                    boolean passengerExists = false;
                    String passengerEmail;
                    do {
                        System.out.print("Enter the passenger's email: ");
                        passengerEmail = scanner.next();
                        // Check if driver exists
                        passengerExists = businessLogic.checkUserExists(passengerEmail);
                        if (!passengerExists)
                            System.out.println("You have entered an invalid email Id\n");
                    } while (!passengerExists);
                    
                    // Calculate total money spent
                    List<Map<String, String>> resultListTotalAmount = businessLogic
                            .calculateTotalAmountChargedForPassenger(passengerEmail);
                    // Display results
                    uiService.displayTotalAmountCharged(resultListTotalAmount);
                    break;
                case 4:
                    // Create a new account logic
                    businessLogic.createNewUserAccount();
                    break;
                case 5:
                    // Submit a ride request logic
                    businessLogic.requestRide();
                    break;
                case 6:
                    // Complete a ride logic
                    businessLogic.completeRide();
                    break;
                default:
                    System.out.println("Invalid choice.");
            }

            // Ask the user if they want to continue or exit
            System.out.print("Press enter to continue or type 'Exit' to exit: ");
            Scanner userInputScanner = new Scanner(System.in);
            String userInput = userInputScanner.nextLine();
            System.out.println(); // Add a line break for better readability

            // Check if the user wants to continue
            continueMenu = !userInput.equalsIgnoreCase("EXIT");
        }

    }
}
