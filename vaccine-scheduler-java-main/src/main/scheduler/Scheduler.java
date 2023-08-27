package scheduler;

import scheduler.db.ConnectionManager;
import scheduler.model.Caregiver;
import scheduler.model.Patient;
import scheduler.model.Vaccine;
import scheduler.util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class Scheduler {

    // objects to keep track of the currently logged-in user
    // Note: it is always true that at most one of currentCaregiver and currentPatient is not null
    //       since only one user can be logged-in at a time
    private static Caregiver currentCaregiver = null;
    private static Patient currentPatient = null;

    public static void main(String[] args) {
        // read input from user
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            // printing greetings text
            System.out.println();
            System.out.println("Welcome to the COVID-19 Vaccine Reservation Scheduling Application!");
            System.out.println("*** Please enter one of the following commands ***");
            System.out.println("> create_patient <username> <password>");  //TODO: implement create_patient (Part 1)
            System.out.println("> create_caregiver <username> <password>");
            System.out.println("> login_patient <username> <password>");  // TODO: implement login_patient (Part 1)
            System.out.println("> login_caregiver <username> <password>");
            System.out.println("> search_caregiver_schedule <date>");  // TODO: implement search_caregiver_schedule (Part 2)
            System.out.println("> reserve <date> <vaccine>");  // TODO: implement reserve (Part 2)
            System.out.println("> upload_availability <date>");
            System.out.println("> cancel <appointment_id>");  // TODO: implement cancel (extra credit)
            System.out.println("> add_doses <vaccine> <number>");
            System.out.println("> show_appointments");  // TODO: implement show_appointments (Part 2)
            System.out.println("> logout");  // TODO: implement logout (Part 2)
            System.out.println("> quit");
            System.out.println();
            System.out.print("> ");
            String response = "";
            try {
                response = r.readLine();
            } catch (IOException e) {
                System.out.println("Please try again!");
            }
            // split the user input by spaces
            String[] tokens = response.split(" ");
            // check if input exists
            if (tokens.length == 0) {
                System.out.println("Please try again!");
                continue;
            }
            // determine which operation to perform
            String operation = tokens[0];
            if (operation.equals("create_patient")) {
                createPatient(tokens);
            } else if (operation.equals("create_caregiver")) {
                createCaregiver(tokens);
            } else if (operation.equals("login_patient")) {
                loginPatient(tokens);
            } else if (operation.equals("login_caregiver")) {
                loginCaregiver(tokens);
            } else if (operation.equals("search_caregiver_schedule")) {
                searchCaregiverSchedule(tokens);
            } else if (operation.equals("reserve")) {
                reserve(tokens);
            } else if (operation.equals("upload_availability")) {
                uploadAvailability(tokens);
            } else if (operation.equals("cancel")) {
                cancel(tokens);
            } else if (operation.equals("add_doses")) {
                addDoses(tokens);
            } else if (operation.equals("show_appointments")) {
                showAppointments(tokens);
            } else if (operation.equals("logout")) {
                logout(tokens);
            } else if (operation.equals("quit")) {
                System.out.println("Bye!");
                return;
            } else {
                System.out.println("Invalid operation name!");
            }
        }
    }

    private static void createPatient(String[] tokens) {
        //Check: Token needs to be exactly a length of 3 (Createpatient, Username, Password)
        if(tokens.length != 3) {
            System.out.println("Failed to create user");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        //Check: Is the username unique
        if (usernameExistsPatient(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        //Checks for secure password
        while(!securePassword(password)) {
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the patient
        try {
            currentPatient = new Patient.PatientBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentPatient.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }

    }

    //Checks a string to make sure if the string meets the requirements
    private static boolean securePassword(String passwordAttempt) {
        boolean isLowerCase = false;
        boolean isUpperCase = false;
        boolean isNumber = false;
        boolean containsSpecial = false;
        boolean legitPass = false;
        for(int i = 0; i < passwordAttempt.length(); i++) {
            char symbol = passwordAttempt.charAt(i);
            if(Character.isDigit(symbol)) {
                isNumber = true;
            } else if(Character.isUpperCase(symbol)) {
                isUpperCase = true;
            } else if(Character.isLowerCase(symbol)) {
                isLowerCase = true;
            } else if(passwordAttempt.contains("!") || passwordAttempt.contains("@") || passwordAttempt.contains("#") || passwordAttempt.contains("?")) {
                containsSpecial = true;
            }
        }
        if(isLowerCase && isUpperCase && isNumber && containsSpecial) {
            legitPass = true;
        }
        if(passwordAttempt.length() > 8 && legitPass == true) {
            return true;
        }
        System.out.println("Please enter a valid password!");
        System.out.println("Please select a password with:");
        System.out.println("At least 8 characters");
        System.out.println("upper and lower case letters");
        System.out.println("A mixture of letters and numbers");
        System.out.println("At least one special character from \"!\", \"@\", \"#\", \"?\"");
        return false;
    }

    private static boolean usernameExistsPatient(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Patients WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void createCaregiver(String[] tokens) {
        // create_caregiver <username> <password>
        // check 1: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Failed to create user.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        // check 2: check if the username has been taken already
        if (usernameExistsCaregiver(username)) {
            System.out.println("Username taken, try again!");
            return;
        }
        //Checks for secure password
        while(!securePassword(password)) {
            return;
        }
        byte[] salt = Util.generateSalt();
        byte[] hash = Util.generateHash(password, salt);
        // create the caregiver
        try {
            currentCaregiver = new Caregiver.CaregiverBuilder(username, salt, hash).build();
            // save to caregiver information to our database
            currentCaregiver.saveToDB();
            System.out.println("Created user " + username);
        } catch (SQLException e) {
            System.out.println("Failed to create user.");
            e.printStackTrace();
        }
    }

    private static boolean usernameExistsCaregiver(String username) {
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();

        String selectUsername = "SELECT * FROM Caregivers WHERE Username = ?";
        try {
            PreparedStatement statement = con.prepareStatement(selectUsername);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            // returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error occurred when checking username");
            e.printStackTrace();
        } finally {
            cm.closeConnection();
        }
        return true;
    }

    private static void loginPatient(String[] tokens) {
        // login_patient <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Patient patient = null;
        try {
            patient = new Patient.PatientGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (patient == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentPatient = patient;
        }
    }

    private static void loginCaregiver(String[] tokens) {
        // login_caregiver <username> <password>
        // check 1: if someone's already logged-in, they need to log out first
        if (currentCaregiver != null || currentPatient != null) {
            System.out.println("User already logged in.");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Login failed.");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];

        Caregiver caregiver = null;
        try {
            caregiver = new Caregiver.CaregiverGetter(username, password).get();
        } catch (SQLException e) {
            System.out.println("Login failed.");
            e.printStackTrace();
        }
        // check if the login was successful
        if (caregiver == null) {
            System.out.println("Login failed.");
        } else {
            System.out.println("Logged in as: " + username);
            currentCaregiver = caregiver;
        }
    }

    private static void searchCaregiverSchedule(String[] tokens) {
        //Checks if there is a user logged in
        if(currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
        //add check to make sure the date is in proper form
        } else if(tokens.length != 2) {
            System.out.println("Please try again!");
        } else {
            String dateAsked = tokens[1];
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();
            String vaccineDoses = ("SELECT * FROM Vaccines");
            String caregiversAvailable = ("SELECT username FROM Availabilities WHERE Time = ? ORDER BY username");
            try {
                PreparedStatement statement1 = con.prepareStatement(vaccineDoses);
                ResultSet set = statement1.executeQuery();
                while(set.next()) {
                    String vacName = set.getString("Name");
                    int vacAmount = set.getInt("Doses");
                    System.out.println("Vaccine:" + vacName + " Doses:" + vacAmount);
                }

                PreparedStatement statement2 = con.prepareStatement(caregiversAvailable);
                statement2.setString(1,dateAsked);
                ResultSet set2 = statement2.executeQuery();
                while(set2.next()) {
                    String careGiver = set2.getString("Username");
                    System.out.println("Caregivers available: " + careGiver);
                }

            } catch (SQLException e) {
                System.out.println("Please try again!");
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter a valid date!");
            } finally {
                cm.closeConnection();
            }
        }
    }

    private static void reserve(String[] tokens) {
        //Checks if user is logged in and if the input is the correct length
        if (currentPatient == null) {
            System.out.println("Please login first!");
        } else if (currentCaregiver != null) {
            System.out.println("Please login as a patient!");
        } else if (tokens.length != 3) {
            System.out.println("Please try again!");
        } else {
            ConnectionManager cm = new ConnectionManager();
            Connection con = cm.createConnection();
            try {
                String date = tokens[1];
                String doseName = tokens[2];

                //Checks if there is an available caregiver
                String caregiversAvailable = ("SELECT username FROM Availabilities WHERE Time = ?");
                PreparedStatement statement1 = con.prepareStatement(caregiversAvailable);
                statement1.setString(1, tokens[1]);
                ResultSet set1 = statement1.executeQuery();
                if (!set1.isBeforeFirst()) {
                    System.out.println("No caregiver is available!");
                    return;
                }
                set1.next();
                String username = set1.getString("username");

                //Checks if there are enough doses for the appointment
                String vaccineDoses = ("SELECT Doses FROM Vaccines WHERE Name = ?");
                PreparedStatement statement2 = con.prepareStatement(vaccineDoses);
                statement2.setString(1, tokens[2]);
                ResultSet set2 = statement2.executeQuery();
                set2.next();
                int doses = set2.getInt("Doses");
                if (doses == 0) {
                    System.out.println("Not enough available doses!");
                    return;
                }

                //Deletes the caregiver from availabilities table when appointment is made
                String deleteUsername = ("DELETE FROM Availabilities WHERE Time = ? AND Username = ?");
                PreparedStatement statement3 = con.prepareStatement(deleteUsername);
                statement3.setString(1, tokens[1]);
                statement3.setString(2, username);
                statement3.executeUpdate();

                //Increments ID number with each new appointment
                String appointmentCount = ("SELECT COUNT(*) AS Count FROM appointments");
                PreparedStatement statement6 = con.prepareStatement(appointmentCount);
                ResultSet set3 = statement6.executeQuery();
                set3.next();
                int ID = set3.getInt("Count");

                //Inserts the appointment info into the appointments table
                String insertValues = ("INSERT INTO Appointments VALUES(?,?,?,?,?)");
                PreparedStatement statement4 = con.prepareStatement(insertValues);
                statement4.setString(1, tokens[1]);
                statement4.setInt(2, ID);
                statement4.setString(3, currentPatient.getUsername());
                statement4.setString(4, username);
                statement4.setString(5, tokens[2]);
                statement4.executeUpdate();

                //Decrease the doses by 1
                String updateDoses = ("UPDATE Vaccines SET Doses = ? WHERE Name = ?");
                PreparedStatement statement5 = con.prepareStatement(updateDoses);
                statement5.setInt(1, doses-1);
                statement5.setString(2, tokens[2]);
                statement5.executeUpdate();

                System.out.println("Appointment ID: " + ID + " Caregiver username: " + username);

            } catch (SQLException e) {
                System.out.println("Please try again!");
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                System.out.println("Please enter a valid input!");
            } finally {
                cm.closeConnection();
            }
        }
    }

    private static void uploadAvailability(String[] tokens) {
        // upload_availability <date>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 2 to include all information (with the operation name)
        if (tokens.length != 2) {
            System.out.println("Please try again!");
            return;
        }
        String date = tokens[1];
        try {
            Date d = Date.valueOf(date);
            currentCaregiver.uploadAvailability(d);
            System.out.println("Availability uploaded!");
        } catch (IllegalArgumentException e) {
            System.out.println("Please enter a valid date!");
        } catch (SQLException e) {
            System.out.println("Error occurred when uploading availability");
            e.printStackTrace();
        }
    }

    private static void cancel(String[] tokens) {
        // TODO: Extra credit
    }

    private static void addDoses(String[] tokens) {
        // add_doses <vaccine> <number>
        // check 1: check if the current logged-in user is a caregiver
        if (currentCaregiver == null) {
            System.out.println("Please login as a caregiver first!");
            return;
        }
        // check 2: the length for tokens need to be exactly 3 to include all information (with the operation name)
        if (tokens.length != 3) {
            System.out.println("Please try again!");
            return;
        }
        String vaccineName = tokens[1];
        int doses = Integer.parseInt(tokens[2]);
        Vaccine vaccine = null;
        try {
            vaccine = new Vaccine.VaccineGetter(vaccineName).get();
        } catch (SQLException e) {
            System.out.println("Error occurred when adding doses");
            e.printStackTrace();
        }
        // check 3: if getter returns null, it means that we need to create the vaccine and insert it into the Vaccines
        //          table
        if (vaccine == null) {
            try {
                vaccine = new Vaccine.VaccineBuilder(vaccineName, doses).build();
                vaccine.saveToDB();
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        } else {
            // if the vaccine is not null, meaning that the vaccine already exists in our table
            try {
                vaccine.increaseAvailableDoses(doses);
            } catch (SQLException e) {
                System.out.println("Error occurred when adding doses");
                e.printStackTrace();
            }
        }
        System.out.println("Doses updated!");
    }

    private static void showAppointments(String[] tokens) {
        if(currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
        } else {
            if (currentCaregiver != null) {
                String printCaregiver = "SELECT ID, Time, Vaccine, PatientUsername AS Username FROM Appointments WHERE CaregiversUsername = ? ORDER BY ID";
                System.out.println(showAppointmentsHelper(currentCaregiver.getUsername(), printCaregiver));
            } else {
                String printPatient = "SELECT ID, Time, Vaccine, CaregiversUsername AS Username FROM Appointments WHERE PatientUsername = ? ORDER BY ID";
                System.out.println(showAppointmentsHelper(currentPatient.getUsername(),printPatient));
            }
        }
    }

    private static String showAppointmentsHelper(String username, String command) {
        String appInfo = "";
        ConnectionManager cm = new ConnectionManager();
        Connection con = cm.createConnection();
        try {
            PreparedStatement statement = con.prepareStatement(command);
            statement.setString(1, username);
            ResultSet set = statement.executeQuery();
            while(set.next()) {
                int ID = set.getInt("ID");
                String vaccine = set.getString("Vaccine");
                String date = set.getString("time");
                String usernameOfPerson = set.getString("Username");
                appInfo += (ID + " " + vaccine + " " + date + " " + usernameOfPerson + "\n");
            }
        } catch (SQLException e) {
            System.out.println("Please try again!");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("Please try again!");
        } finally {
            cm.closeConnection();
        }
        return appInfo;
    }

    private static void logout(String[] tokens) {
        if(currentCaregiver == null && currentPatient == null) {
            System.out.println("Please login first!");
        } else if (tokens.length == 1) {
            System.out.println("Please try again!");
        } else {
            currentCaregiver = null;
            currentPatient = null;
            System.out.println("Successfully logged out!");
        }
    }
}
