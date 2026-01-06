package HospitalManagementSystem;

import com.mysql.cj.jdbc.Driver;

import java.sql.*;
import java.util.Scanner;

public class HospitalManagementSystem {
    private static final String url = "jdbc:mysql://localhost:3306/hospital";
    private static final String username = "****";
    private static final String password = "****";

    public static void main(String[] args) {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        try{
            Connection connection = DriverManager.getConnection(url,username,password);
            Patient patient = new Patient(connection, scanner);
            Doctor doctor = new Doctor(connection);
            while(true){
                System.out.println("HOSPITAL MANAGEMENT SYSTEM");
                System.out.println("1> Add Patients");
                System.out.println("2> View Patients");
                System.out.println("3> View Doctors");
                System.out.println("4> Book Appointment");
                System.out.println("5> Exit");
                System.out.println("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice){
                    case 1:
                        patient.addPatient();
                        System.out.println();
                        break;
                    case 2:
                        patient.viewPatient();
                        System.out.println();
                        break;
                    case 3:
                        doctor.viewDoctors();
                        System.out.println();
                        break;
                    case 4:
                        bookAppointment(patient,doctor,connection,scanner);
                        System.out.println();
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Enter valid choice.");
                        break;
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void bookAppointment(Patient patient,Doctor doctor, Connection connection, Scanner scanner){
        System.out.println("Enter Patient Id: ");
        int patientId = scanner.nextInt();
        System.out.println("Enter Doctor Id: ");
        int doctorId = scanner.nextInt();
        scanner.nextLine(); // consume leftover newline
        System.out.println("Enter appointment date (YYYY-MM-DD): ");
        String appointmentDate = scanner.nextLine().trim();

        if (!appointmentDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD.");
            return;
        }
        if ( patient.getPatientById(patientId) && doctor.getDoctorById(doctorId) ){
            if ( checkDoctorAvailability(doctorId, appointmentDate, connection) ){
                String apppointmentQuery = "INSERT INTO appointments(patient_id, doctor_id, appointment_date) VALUES(?, ?, ?)";
                try{
                    PreparedStatement preparedStatement = connection.prepareStatement(apppointmentQuery);
                    preparedStatement.setInt(1, patientId);
                    preparedStatement.setInt(2, doctorId);
                    preparedStatement.setString(3, appointmentDate);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if ( rowsAffected>0 ){
                        System.out.println("Appointment booked successfully.");
                    }else {
                        System.out.println("Failed to book your appointment");
                    }
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }else {
                System.out.println("Doctor not available for this date.");
            }
        }else {
            System.out.println("Either doctor or patient doesn't exist.");
        }
    }

    public static boolean checkDoctorAvailability(int doctorId, String appointmentDate, Connection connection){
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id  = ? AND appointment_date = ?";
        try{
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setDate(2, java.sql.Date.valueOf(appointmentDate));
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                int count = resultSet.getInt(1);
                if(count==0){
                    return true;
                }else {
                    return false;
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
