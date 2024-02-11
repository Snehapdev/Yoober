package com.database.yoober.yoober_app;

import org.springframework.stereotype.Service;
import java.util.Scanner;

@Service
public class MenuService {

    // Scanner object for user input
    private Scanner scanner = new Scanner(System.in);

    // Method to display the main menu options
    public void displayMenu() {
        System.out.println("Welcome to Your Ride Service. Please select an option");
        System.out.println("1. View all account details");
        System.out.println("2. Calculate the average rating for a specific driver");
        System.out.println("3. Calculate the total money spent by a specific passenger");
        System.out.println("4. Create a new account");
        System.out.println("5. Submit a ride request");
        System.out.println("6. Complete a ride");
    }

    // Method to get the user's choice from the menu
    public int getUserChoice() {
        System.out.print("Enter your choice (1-6): ");

        // Validate user input to ensure it is an integer
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number between 1 and 6.");
            scanner.next(); // consume invalid input
        }

        int choice = scanner.nextInt();

        // Validate user choice to ensure it is within the valid range
        if (choice < 1 || choice > 6) {
            System.out.println("Invalid choice. Please enter a number between 1 and 6.");
            return getUserChoice(); // recursively call until a valid choice is entered
        }

        return choice;
    }

    // Method to display a welcome message with group members' names
    public void displayWelcomeMessage() {
        System.out.println("Welcome to Yoober\n");
        System.out.println("Developed by: Sneha Puthukkudi and Celia Mary Augusty\n");
    }
}
