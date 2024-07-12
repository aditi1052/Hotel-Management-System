import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Main {
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "aditi@1052";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        //to load the drivers
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(ClassNotFoundException e){
            System.out.println(e.getMessage());
            return;
        }

        //now to establish the connection
        try
            (Connection con = DriverManager.getConnection(url, username, password)){
            while(true){
                //for main menu
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                Scanner sc = new Scanner((System.in));
                System.out.println("press 1 to Reserve a Room");
                System.out.println("press 2 to View Reservation");
                System.out.println("press 3 to Get Room Number");
                System.out.println("press 4 to Update Reservation");
                System.out.println("press 5 to Delete Reservation");
                System.out.println("press 0 to Exit");
                System.out.println("Choose an Option: ");
                int choice = sc.nextInt();
                switch (choice){
                    case 1:
                        reserveRoom(con, sc);
                        break;
                    case 2:
                        viewReservation(con);
                        break;
                    case 3:
                        getRoomNumber(con, sc);
                        break;
                    case 4:
                        updateReservation(con,sc);
                        break;
                    case 5:
                        deleteReservation(con, sc);
                        break;
                    case 0:
                        exit();
                        sc.close();
                        return;
                    default:
                    System.out.println("Invalid choice, try again");
                }
            }
        }catch(SQLException e){
            System.out.println("Databsse connection error: " + e.getMessage());
        }catch(InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    private static void reserveRoom(Connection con, Scanner sc) {
        System.out.println("Enter Guest Name: ");
        String guestName = sc.next();
        sc.nextLine();
        System.out.println("Enter Room Number: ");
        int roomNumber = sc.nextInt();
        System.out.println("Enter contact Number: ");
        String contactNumber = sc.next();

        String sql = "INSERT INTO reservations (guest_name, room_number, contact_number)" +
                "VALUES ('" + guestName + "', " + roomNumber + ", '" + contactNumber + "')";

        try (Statement st = con.createStatement()) {
            int affectedRows = st.executeUpdate(sql);

            if (affectedRows > 0) {
                System.out.println("Reservation successful");
            } else {
                System.out.println("Reservation failed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void viewReservation(Connection con) throws SQLException{
            String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservations";
            try(Statement st = con.createStatement();
                ResultSet result = st.executeQuery(sql)){

                System.out.println("Current Reservation: ");
                System.out.println("+----------------+----------------+----------------+----------------+----------------+");
                System.out.println("| Reservation ID | Guest          | Room Number    | Contact Number | Reservation Date|");

                while(result.next()){
                    int reservationId = result.getInt("reservation_id");
                    String guestName = result.getString("guest_name");
                    int roomNumber = result.getInt("room_number");
                    String contactNumber = result.getString("contact_number");
                    String reservationDate = result.getTimestamp("reservation_date").toString();

                    //formating
                    System.out.printf("| %-14d |%-15s | %-13d | %-20s | %-19s   |\n",
                            reservationId, guestName, roomNumber, contactNumber,reservationDate);
                }

                System.out.println("+-----------------+----------------+----------------+---------------+----------------+");
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private static void getRoomNumber(Connection con, Scanner sc){
            try{
                System.out.print("Enter Reservation ID: ");
                int reservationID = sc.nextInt();
                System.out.print("Enter Guest name: ");
                String guestName = sc.next();

                String sql = "SELECT room_number FROM reservations " +
                        "WHERE reservation_id = " + reservationID +
                        " AND guest_name = '" + guestName + "'";

                try(Statement st = con.createStatement(); ResultSet result = st.executeQuery(sql)){

                    if(result.next()){
                        int roomNumber = result.getInt("room_number");
                        System.out.println("Room Number for Reservation ID " + reservationID +
                                " and Guest " + guestName + " is: " + roomNumber);
                    }else {
                        System.out.println("Resrevation not found for the given ID and guest name:(");
                    }
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
        }
        private static void updateReservation(Connection con, Scanner sc){
            try {
                System.out.print("Enter reseravtion ID to update: ");
                int reservationID = sc.nextInt();
                sc.nextLine(); //Consume the newline character

                if (!reservationExists(con, reservationID)) {
                    System.out.println("Reservation not found for the given ID.");
                    return;
                }

                System.out.print("Enter new guest name: ");
                String newGuestName = sc.nextLine();
                System.out.print("Enter new room number: ");
                int newRoomNumber = sc.nextInt();
                System.out.print("Enter new contact number: ");
                String newContactNumber = sc.next();

                String sql = "UPDATE reservations SET guest_name = '" + newGuestName + "', " +
                        "room_number = " + newRoomNumber + ", " + "contact_number = '" + newContactNumber + "' " +
                        "WHERE reservation_id = " + reservationID;

                try (Statement st = con.createStatement()) {
                    int affesctedRows = st.executeUpdate(sql);

                    if (affesctedRows > 0) {
                        System.out.println("Reservation update seccessfully!");
                    } else {
                        System.out.println("Reservation update failed!");
                    }
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
        }

        private static void deleteReservation(Connection con, Scanner sc){
            try{
                System.out.println("Enter reservation ID to delete: ");
                int reservationId = sc.nextInt();

                if(!reservationExists(con, reservationId)){
                    System.out.println("Reservation not found for the given ID.");
                    return;
                }
                String sql = "DELETE FROM reservations WHERE reservation_id = " + reservationId;

                try(Statement st = con.createStatement()) {
                    int affectedRows = st.executeUpdate(sql);

                    if (affectedRows > 0) {
                        System.out.println("Reservation deleted successfully!");
                    } else {
                        System.out.println("Reservation deletion failed.");
                    }
                }
            }catch(SQLException e){
                e.printStackTrace();
            }
        }

        private static boolean reservationExists(Connection con, int reservationId){
            try{
                String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = " + reservationId;

                try(Statement st = con.createStatement();
                    ResultSet result = st.executeQuery(sql)){

                    return result.next();//if there is a result, the reservation exists
                }
            }catch(SQLException e){
                e.printStackTrace();
                return false; //handle db errors as needed
            }
        }

        public static void exit() throws InterruptedException {
            System.out.print("Exiting System");
            int i=5;
            while(i !=0){
                System.out.print(".");
                Thread.sleep(450);
                i--;
            }
            System.out.println();
            System.out.println("ThankYou for using Hotel Reservation System :)");
        }

    }




























