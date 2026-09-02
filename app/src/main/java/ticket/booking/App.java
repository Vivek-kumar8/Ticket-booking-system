package ticket.booking;

import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.service.UserBookingService;
import ticket.booking.util.UserServiceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class App {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("     TRAIN BOOKING SYSTEM");
        System.out.println("=================================");

        Scanner scanner = new Scanner(System.in);

        UserBookingService userBookingService;

        try {
            userBookingService = new UserBookingService();
        } catch (IOException ex) {
            System.out.println("Unable to load users database.");
            ex.printStackTrace();
            return;
        }

        User loggedInUser = null;
        Train trainSelectedForBooking = null;

        int option = 0;

        while (option != 7) {

            System.out.println();
            System.out.println("Choose option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");
            System.out.print("Enter your choice: ");

            try {
                option = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Please enter a number between 1 and 7.");
                scanner.nextLine();
                continue;
            }

            switch (option) {

                // =========================
                // SIGN UP
                // =========================
                case 1:

                    System.out.println();
                    System.out.println("----- SIGN UP -----");

                    System.out.print("Enter username: ");
                    String nameToSignUp = scanner.next();

                    System.out.print("Enter password: ");
                    String passwordToSignUp = scanner.next();

                    User userToSignup = new User(
                            nameToSignUp,
                            passwordToSignUp,
                            UserServiceUtil.hashPassword(passwordToSignUp),
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );

                    if (userBookingService.signUp(userToSignup)) {
                        System.out.println("Signup successful!");
                    } else {
                        System.out.println("Signup failed.");
                    }

                    break;


                // =========================
                // LOGIN
                // =========================
                case 2:

                    System.out.println();
                    System.out.println("----- LOGIN -----");

                    System.out.print("Enter username: ");
                    String nameToLogin = scanner.next();

                    System.out.print("Enter password: ");
                    String passwordToLogin = scanner.next();

                    User userToLogin = new User(
                            nameToLogin,
                            passwordToLogin,
                            "",
                            new ArrayList<>(),
                            UUID.randomUUID().toString()
                    );

                    try {

                        UserBookingService loginService =
                                new UserBookingService(userToLogin);

                        if (loginService.loginUser()) {

                            loggedInUser = userToLogin;
                            userBookingService = loginService;

                            System.out.println("Login successful!");
                            System.out.println("Welcome, " + nameToLogin);

                        } else {

                            System.out.println("Invalid username or password.");

                            loggedInUser = null;
                        }

                    } catch (IOException ex) {

                        System.out.println("Unable to login.");
                        ex.printStackTrace();
                    }

                    break;


                // =========================
                // FETCH BOOKINGS
                // =========================
                case 3:

                    if (loggedInUser == null) {
                        System.out.println("Please login first.");
                        break;
                    }

                    System.out.println();
                    System.out.println("----- YOUR BOOKINGS -----");

                    userBookingService.fetchBookings();

                    break;


                // =========================
                // SEARCH TRAINS
                // =========================
                case 4:

                    System.out.println();
                    System.out.println("----- SEARCH TRAINS -----");

                    System.out.print("Enter source station: ");
                    String source = scanner.next();

                    System.out.print("Enter destination station: ");
                    String destination = scanner.next();

                    List<Train> trains =
                            userBookingService.getTrains(source, destination);

                    if (trains.isEmpty()) {
                        System.out.println("No trains found.");
                        break;
                    }

                    System.out.println();
                    System.out.println("Available trains:");

                    int index = 1;

                    for (Train train : trains) {

                        System.out.println();
                        System.out.println(
                                index + ". Train ID: "
                                        + train.getTrainId()
                        );

                        System.out.println(
                                "   Train No: "
                                        + train.getTrainNo()
                        );

                        for (Map.Entry<String, String> entry :
                                train.getStationTimes().entrySet()) {

                            System.out.println(
                                    "   Station "
                                            + entry.getKey()
                                            + " time: "
                                            + entry.getValue()
                            );
                        }

                        index++;
                    }

                    System.out.print(
                            "Select a train (1-" + trains.size() + "): "
                    );

                    int trainChoice;

                    try {
                        trainChoice = scanner.nextInt();
                    } catch (Exception e) {
                        System.out.println("Invalid train selection.");
                        scanner.nextLine();
                        break;
                    }

                    if (trainChoice < 1 ||
                            trainChoice > trains.size()) {

                        System.out.println("Invalid train selection.");
                        break;
                    }

                    trainSelectedForBooking =
                            trains.get(trainChoice - 1);

                    System.out.println(
                            "Train "
                                    + trainSelectedForBooking.getTrainId()
                                    + " selected successfully."
                    );

                    break;


                // =========================
                // BOOK SEAT
                // =========================
                case 5:

                    if (loggedInUser == null) {
                        System.out.println("Please login first.");
                        break;
                    }

                    if (trainSelectedForBooking == null) {
                        System.out.println(
                                "Please search and select a train first."
                        );
                        break;
                    }

                    System.out.println();
                    System.out.println("----- BOOK A SEAT -----");

                    List<List<Integer>> seats =
                            userBookingService.fetchSeats(
                                    trainSelectedForBooking
                            );

                    if (seats == null || seats.isEmpty()) {
                        System.out.println("No seats available.");
                        break;
                    }

                    System.out.println();
                    System.out.println("Seat layout:");
                    System.out.println("0 = Available");
                    System.out.println("1 = Booked");
                    System.out.println();

                    for (int i = 0; i < seats.size(); i++) {

                        System.out.print("Row " + i + ": ");

                        for (Integer seat : seats.get(i)) {
                            System.out.print(seat + " ");
                        }

                        System.out.println();
                    }

                    System.out.print("Enter row: ");
                    int row;

                    try {
                        row = scanner.nextInt();
                    } catch (Exception e) {
                        System.out.println("Invalid row.");
                        scanner.nextLine();
                        break;
                    }

                    System.out.print("Enter column: ");
                    int col;

                    try {
                        col = scanner.nextInt();
                    } catch (Exception e) {
                        System.out.println("Invalid column.");
                        scanner.nextLine();
                        break;
                    }

                    if (row < 0 ||
                            row >= seats.size() ||
                            col < 0 ||
                            col >= seats.get(row).size()) {

                        System.out.println("Invalid seat position.");
                        break;
                    }

                    if (seats.get(row).get(col) == 1) {
                        System.out.println("This seat is already booked.");
                        break;
                    }

                    System.out.println("Booking your seat...");

                    Boolean booked =
                            userBookingService.bookTrainSeat(
                                    trainSelectedForBooking,
                                    row,
                                    col
                            );

                    if (Boolean.TRUE.equals(booked)) {
                        System.out.println(
                                "Seat booked successfully!"
                        );
                        System.out.println(
                                "Enjoy your journey!"
                        );
                    } else {
                        System.out.println(
                                "Unable to book the seat."
                        );
                    }

                    break;


                // =========================
                // CANCEL BOOKING
                // =========================
                case 6:

                    if (loggedInUser == null) {
                        System.out.println("Please login first.");
                        break;
                    }

                    System.out.println();
                    System.out.println("----- CANCEL BOOKING -----");

                    System.out.print("Enter ticket ID: ");
                    String ticketId = scanner.next();

                    Boolean cancelled =
                            userBookingService.cancelBooking(ticketId);

                    if (Boolean.TRUE.equals(cancelled)) {
                        System.out.println(
                                "Booking cancelled successfully."
                        );
                    } else {
                        System.out.println(
                                "Unable to cancel booking."
                        );
                    }

                    break;


                // =========================
                // EXIT
                // =========================
                case 7:

                    System.out.println();
                    System.out.println(
                            "Thank you for using Train Booking System!"
                    );

                    break;


                // =========================
                // INVALID OPTION
                // =========================
                default:

                    System.out.println(
                            "Invalid option. Please choose 1-7."
                    );

                    break;
            }
        }

        scanner.close();
    }
}