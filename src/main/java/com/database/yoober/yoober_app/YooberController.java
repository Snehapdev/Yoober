package com.database.yoober.yoober_app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

@RestController
@RequestMapping("/api")
public class YooberController {

    @Autowired
    private final MenuService menuService;
    private final BusinessLogic businessLogic;
    private final UIService uiService;

    private final Scanner scanner = new Scanner(System.in);

    @Autowired
    public YooberController(MenuService menuService, BusinessLogic businessLogic, UIService uiService) {
        this.menuService = menuService;
        this.businessLogic = businessLogic;
        this.uiService = uiService;
    }

    @GetMapping("/retrieveUserData")
    public ResponseEntity<List<Map<String, String>>> retrieveUserData() {
        List<Map<String, String>> userData = businessLogic.retrieveUserData();
        uiService.displayAccountInfo(userData);
        return new ResponseEntity<>(userData, HttpStatus.OK);
    }

    @GetMapping("/calculateAverageRatingForDriver")
    public ResponseEntity<List<Map<String, String>>> calculateAverageRatingForDriver(@RequestParam String driverEmail) {
        List<Map<String, String>> averageRating = businessLogic.calculateAverageRatingForDriver(driverEmail);
        return new ResponseEntity<>(averageRating, HttpStatus.OK);
    }

     @PostMapping("/createNewUserAccount")
    public ResponseEntity<String> createNewUserAccount(@RequestBody UserInfoRequest userInfo) {
        try {
            businessLogic.createNewUserAccount(userInfo);
            return new ResponseEntity<>("New account created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create new user account", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/requestRide")
    public ResponseEntity<String> requestRide() {
        try {
            businessLogic.requestRide();
            return new ResponseEntity<>("Ride request posted successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to post ride request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/completeRide")
    public ResponseEntity<String> completeRide() {
        try {
            businessLogic.completeRide();
            return new ResponseEntity<>("Ride completed successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to complete ride", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/calculateTotalAmountChargedForPassenger")
    public ResponseEntity<List<Map<String, String>>> calculateTotalAmountChargedForPassenger(
            @RequestParam String passengerEmail) {
        List<Map<String, String>> totalAmountCharged = businessLogic
                .calculateTotalAmountChargedForPassenger(passengerEmail);
        return new ResponseEntity<>(totalAmountCharged, HttpStatus.OK);
    }


}
