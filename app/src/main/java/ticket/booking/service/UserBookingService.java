package ticket.booking.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserBookingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private List<User> userList;
    private User user;

    private final String USER_FILE_PATH =
            "app/src/main/java/ticket/booking/localDB/users.json";


    // =========================
    // CONSTRUCTORS
    // =========================

    public UserBookingService(User user) throws IOException {
        this.user = user;
        loadUserListFromFile();
    }

    public UserBookingService() throws IOException {
        loadUserListFromFile();
    }


    // =========================
    // LOAD USERS
    // =========================

    private void loadUserListFromFile() throws IOException {

        File usersFile = new File(USER_FILE_PATH);

        if (!usersFile.exists()) {
            throw new IOException(
                    "Users database not found: "
                            + usersFile.getAbsolutePath()
            );
        }

        userList = objectMapper.readValue(
                usersFile,
                new TypeReference<List<User>>() {}
        );
    }


    // =========================
    // LOGIN
    // =========================

    public Boolean loginUser() {

        if (user == null) {
            return Boolean.FALSE;
        }

        Optional<User> foundUser = userList.stream()
                .filter(user1 ->
                        user1.getName().equals(user.getName())
                                &&
                        UserServiceUtil.checkPassword(
                                user.getPassword(),
                                user1.getHashedPassword()
                        )
                )
                .findFirst();

        if (foundUser.isPresent()) {

            // Use actual user from users.json
            user = foundUser.get();

            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    // =========================
    // SIGN UP
    // =========================

    public Boolean signUp(User user1) {

        if (user1 == null) {
            return Boolean.FALSE;
        }

        boolean userExists = userList.stream()
                .anyMatch(existingUser ->
                        existingUser.getName()
                                .equalsIgnoreCase(user1.getName())
                );

        if (userExists) {

            System.out.println(
                    "Username already exists."
            );

            return Boolean.FALSE;
        }

        try {

            if (user1.getTicketsBooked() == null) {
                user1.setTicketsBooked(new ArrayList<>());
            }

            userList.add(user1);

            saveUserListToFile();

            return Boolean.TRUE;

        } catch (IOException ex) {

            System.out.println(
                    "Unable to save user."
            );

            ex.printStackTrace();

            return Boolean.FALSE;
        }
    }


    // =========================
    // SAVE USERS
    // =========================

    private void saveUserListToFile() throws IOException {

        File usersFile = new File(USER_FILE_PATH);

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(usersFile, userList);
    }


    // =========================
    // FETCH BOOKINGS
    // =========================

    public void fetchBookings() {

        if (user == null) {

            System.out.println(
                    "Please login first."
            );

            return;
        }

        Optional<User> foundUser = userList.stream()
                .filter(user1 ->
                        user1.getName().equals(user.getName())
                                &&
                        UserServiceUtil.checkPassword(
                                user.getPassword(),
                                user1.getHashedPassword()
                        )
                )
                .findFirst();

        if (!foundUser.isPresent()) {

            System.out.println(
                    "User not found."
            );

            return;
        }

        User actualUser = foundUser.get();

        if (actualUser.getTicketsBooked() == null
                || actualUser.getTicketsBooked().isEmpty()) {

            System.out.println(
                    "You have no bookings."
            );

            return;
        }

        System.out.println();
        System.out.println("----- YOUR BOOKINGS -----");

        actualUser.printTickets();
    }


    // =========================
    // BOOK TRAIN SEAT
    // =========================

    public Boolean bookTrainSeat(
            Train train,
            int row,
            int seat,
            String source,
            String destination,
            String dateOfTravel
    ) {

        if (user == null) {

            System.out.println(
                    "Please login first."
            );

            return Boolean.FALSE;
        }

        if (train == null) {

            System.out.println(
                    "Please select a train first."
            );

            return Boolean.FALSE;
        }

        try {

            // -------------------------
            // Validate train seats
            // -------------------------

            List<List<Integer>> seats =
                    train.getSeats();

            if (seats == null) {

                System.out.println(
                        "Seat information is unavailable."
                );

                return Boolean.FALSE;
            }

            if (row < 0 || row >= seats.size()) {

                System.out.println(
                        "Invalid row number."
                );

                return Boolean.FALSE;
            }

            if (seat < 0 ||
                    seat >= seats.get(row).size()) {

                System.out.println(
                        "Invalid seat number."
                );

                return Boolean.FALSE;
            }


            // -------------------------
            // Check seat
            // -------------------------

            if (seats.get(row).get(seat) == 1) {

                System.out.println(
                        "This seat is already booked."
                );

                return Boolean.FALSE;
            }


            // -------------------------
            // Find actual user
            // -------------------------

            Optional<User> foundUser = userList.stream()
                    .filter(user1 ->
                            user1.getName()
                                    .equals(user.getName())
                                    &&
                            UserServiceUtil.checkPassword(
                                    user.getPassword(),
                                    user1.getHashedPassword()
                            )
                    )
                    .findFirst();

            if (!foundUser.isPresent()) {

                System.out.println(
                        "User not found."
                );

                return Boolean.FALSE;
            }

            User actualUser = foundUser.get();


            // -------------------------
            // Create ticket
            // -------------------------

            String ticketId =
                    UUID.randomUUID().toString();

            Ticket ticket = new Ticket(
                    ticketId,
                    actualUser.getUserId(),
                    source,
                    destination,
                    dateOfTravel,
                    train
            );


            // -------------------------
            // Add ticket to user
            // -------------------------

            if (actualUser.getTicketsBooked() == null) {

                actualUser.setTicketsBooked(
                        new ArrayList<>()
                );
            }

            actualUser.getTicketsBooked()
                    .add(ticket);


            // -------------------------
            // Book seat
            // -------------------------

            seats.get(row).set(seat, 1);

            train.setSeats(seats);


            // -------------------------
            // Save train
            // -------------------------

            TrainService trainService =
                    new TrainService();

            trainService.addTrain(train);


            // -------------------------
            // Save user
            // -------------------------

            saveUserListToFile();

            user = actualUser;


            // -------------------------
            // Success
            // -------------------------

            System.out.println();
            System.out.println(
                    "Seat booked successfully!"
            );

            System.out.println(
                    "Ticket ID: " + ticketId
            );

            System.out.println(
                    "Enjoy your journey!"
            );

            return Boolean.TRUE;

        } catch (IOException ex) {

            System.out.println(
                    "Unable to book seat."
            );

            ex.printStackTrace();

            return Boolean.FALSE;
        }
    }


    // =========================
    // OLD METHOD
    // =========================
    // Kept only for compatibility
    // with any old code.

    public Boolean bookTrainSeat(
            Train train,
            int row,
            int seat
    ) {

        System.out.println(
                "Please use the complete booking method."
        );

        return Boolean.FALSE;
    }


    // =========================
    // FETCH SEATS
    // =========================

    public List<List<Integer>> fetchSeats(
            Train train
    ) {

        if (train == null) {
            return new ArrayList<>();
        }

        if (train.getSeats() == null) {
            return new ArrayList<>();
        }

        return train.getSeats();
    }


    // =========================
    // SEARCH TRAINS
    // =========================

    public List<Train> getTrains(
            String source,
            String destination
    ) {

        try {

            TrainService trainService =
                    new TrainService();

            return trainService.searchTrains(
                    source,
                    destination
            );

        } catch (IOException ex) {

            System.out.println(
                    "Unable to load train database."
            );

            ex.printStackTrace();

            return new ArrayList<>();
        }
    }


    // =========================
    // CANCEL BOOKING
    // =========================

    public Boolean cancelBooking(String ticketId) {

        if (user == null) {

            System.out.println(
                    "Please login first."
            );

            return Boolean.FALSE;
        }

        if (ticketId == null ||
                ticketId.trim().isEmpty()) {

            System.out.println(
                    "Ticket ID cannot be empty."
            );

            return Boolean.FALSE;
        }

        Optional<User> foundUser = userList.stream()
                .filter(user1 ->
                        user1.getName()
                                .equals(user.getName())
                                &&
                        UserServiceUtil.checkPassword(
                                user.getPassword(),
                                user1.getHashedPassword()
                        )
                )
                .findFirst();

        if (!foundUser.isPresent()) {

            System.out.println(
                    "User not found."
            );

            return Boolean.FALSE;
        }

        User actualUser = foundUser.get();

        if (actualUser.getTicketsBooked() == null ||
                actualUser.getTicketsBooked().isEmpty()) {

            System.out.println(
                    "You have no bookings."
            );

            return Boolean.FALSE;
        }

        boolean removed =
                actualUser.getTicketsBooked()
                        .removeIf(ticket ->
                                ticket.getTicketId()
                                        .equals(ticketId)
                        );

        if (!removed) {

            System.out.println(
                    "No ticket found with ID: "
                            + ticketId
            );

            return Boolean.FALSE;
        }

        try {

            saveUserListToFile();

            user = actualUser;

            System.out.println(
                    "Ticket with ID "
                            + ticketId
                            + " has been cancelled."
            );

            return Boolean.TRUE;

        } catch (IOException ex) {

            System.out.println(
                    "Unable to save cancellation."
            );

            ex.printStackTrace();

            return Boolean.FALSE;
        }
    }
}