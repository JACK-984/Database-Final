package com.example.models;

import com.example.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Doctor {

  private int doctorId;
  private String name;
  private String specialty;
  private int yearsOfExperience;
  private String contactInformation;

  // Constructors
  public Doctor() {}

  public Doctor(
    String name,
    String specialty,
    int yearsOfExperience,
    String contactInformation
  ) {
    this.name = name;
    this.specialty = specialty;
    this.yearsOfExperience = yearsOfExperience;
    this.contactInformation = contactInformation;
  }

  // Getters and Setters
  public int getDoctorId() {
    return doctorId;
  }

  public void setDoctorId(int doctorId) {
    this.doctorId = doctorId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSpecialty() {
    return specialty;
  }

  public void setSpecialty(String specialty) {
    this.specialty = specialty;
  }

  public int getYearsOfExperience() {
    return yearsOfExperience;
  }

  public void setYearsOfExperience(int yearsOfExperience) {
    this.yearsOfExperience = yearsOfExperience;
  }

  public String getContactInformation() {
    return contactInformation;
  }

  public void setContactInformation(String contactInformation) {
    this.contactInformation = contactInformation;
  }

  public static class SpecialtyStats {

    private String specialty;
    private int appointmentCount;

    public SpecialtyStats(String specialty, int appointmentCount) {
      this.specialty = specialty;
      this.appointmentCount = appointmentCount;
    }

    @Override
    public String toString() {
      return String.format(
        "Specialty: %s, Appointments: %d",
        specialty,
        appointmentCount
      );
    }
  }

  public static List<SpecialtyStats> getPopularSpecialties()
    throws SQLException {
    List<SpecialtyStats> specialtyStats = new ArrayList<>();
    String sql =
      "SELECT d.Specialty, COUNT(a.AppointmentID) AS AppointmentCount " +
      "FROM Doctors d " +
      "JOIN Appointments a ON d.DoctorID = a.DoctorID " +
      "GROUP BY d.Specialty " +
      "ORDER BY AppointmentCount DESC " +
      "LIMIT 5";

    try (
      Connection conn = DBConnection.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql)
    ) {
      while (rs.next()) {
        specialtyStats.add(
          new SpecialtyStats(
            rs.getString("Specialty"),
            rs.getInt("AppointmentCount")
          )
        );
      }
    }
    return specialtyStats;
  }

  // Database operations
  public void save() throws SQLException {
    if (this.doctorId == 0) {
      // This is a new doctor, so insert
      String sql =
        "INSERT INTO Doctors (Name, Specialty, YearsOfExperience, ContactInformation) VALUES (?, ?, ?, ?)";
      try (
        Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(
          sql,
          Statement.RETURN_GENERATED_KEYS
        )
      ) {
        pstmt.setString(1, this.name);
        pstmt.setString(2, this.specialty);
        pstmt.setInt(3, this.yearsOfExperience);
        pstmt.setString(4, this.contactInformation);

        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
          try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
              this.doctorId = generatedKeys.getInt(1);
            }
          }
        }
      }
    } else {
      // This is an existing doctor, so update
      String sql =
        "UPDATE Doctors SET Name = ?, Specialty = ?, YearsOfExperience = ?, ContactInformation = ? WHERE DoctorID = ?";
      try (
        Connection conn = DBConnection.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)
      ) {
        pstmt.setString(1, this.name);
        pstmt.setString(2, this.specialty);
        pstmt.setInt(3, this.yearsOfExperience);
        pstmt.setString(4, this.contactInformation);
        pstmt.setInt(5, this.doctorId);

        pstmt.executeUpdate();
      }
    }
  }

  public void delete() throws SQLException {
    String sql = "DELETE FROM Doctors WHERE DoctorID = ?";
    try (
      Connection conn = DBConnection.getConnection();
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, this.doctorId);
      pstmt.executeUpdate();
    }
  }

  public static Doctor getById(int doctorId) throws SQLException {
    String sql = "SELECT * FROM Doctors WHERE DoctorID = ?";
    try (
      Connection conn = DBConnection.getConnection();
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, doctorId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          Doctor doctor = new Doctor();
          doctor.setDoctorId(rs.getInt("DoctorID"));
          doctor.setName(rs.getString("Name"));
          doctor.setSpecialty(rs.getString("Specialty"));
          doctor.setYearsOfExperience(rs.getInt("YearsOfExperience"));
          doctor.setContactInformation(rs.getString("ContactInformation"));
          return doctor;
        }
      }
    }
    return null;
  }

  public static List<Doctor> getAll() throws SQLException {
    List<Doctor> doctors = new ArrayList<>();
    String sql = "SELECT * FROM Doctors";
    try (
      Connection conn = DBConnection.getConnection();
      Statement stmt = conn.createStatement();
      ResultSet rs = stmt.executeQuery(sql)
    ) {
      while (rs.next()) {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getInt("DoctorID"));
        doctor.setName(rs.getString("Name"));
        doctor.setSpecialty(rs.getString("Specialty"));
        doctor.setYearsOfExperience(rs.getInt("YearsOfExperience"));
        doctor.setContactInformation(rs.getString("ContactInformation"));
        doctors.add(doctor);
      }
    }
    return doctors;
  }

  public List<Appointment> getDoctorAppointments(Date date)
    throws SQLException {
    List<Appointment> appointments = new ArrayList<>();
    String sql =
      "SELECT a.AppointmentID, p.Name AS PatientName, a.Time, a.Status " +
      "FROM Appointments a " +
      "JOIN Patients p ON a.PatientID = p.PatientID " +
      "WHERE a.DoctorID = ? AND a.Date = ?";

    try (
      Connection conn = DBConnection.getConnection();
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, this.doctorId);
      pstmt.setDate(2, date);

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Appointment appointment = new Appointment();
          appointment.setAppointmentId(rs.getInt("AppointmentID"));
          appointment.setDoctorId(this.doctorId);
          appointment.setDoctorName(this.getName());
          appointment.setPatientName(rs.getString("PatientName"));
          appointment.setTime(rs.getTime("Time"));
          appointment.setStatus(rs.getString("Status"));
          appointment.setDate(date);
          appointments.add(appointment);
        }
      }
    }
    return appointments;
  }

  /**
   * Calculates the total revenue generated by the doctor for a specific month and year
   *
   * month The month for which to calculate revenue (1-12)
   * year The year for which to calculate revenue
   * return DoctorRevenue object containing the doctor's name and total revenue
   * SQLException if there's an error executing the database query
   * IllegalArgumentException
   */
  public DoctorRevenue listRevenueForDoctor(int month, int year)
    throws SQLException {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("Month must be between 1 and 12");
    }

    String sql =
      "SELECT d.Name AS DoctorName, SUM(b.TotalAmount) AS TotalRevenue" +
      " FROM Doctors d " +
      " JOIN Appointments a ON d.DoctorID = a.DoctorID " +
      " JOIN Bills b ON a.AppointmentID = b.AppointmentID " +
      " WHERE d.DoctorID = ? " +
      " AND MONTH(a.Date) = ? " +
      " AND YEAR(a.Date) = ? " +
      " AND b.PaymentStatus = 'Paid' " +
      " GROUP BY d.DoctorID";

    try (
      Connection conn = DBConnection.getConnection();
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, this.doctorId);
      pstmt.setInt(2, month);
      pstmt.setInt(3, year);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new DoctorRevenue(
            rs.getString("DoctorName"),
            rs.getDouble("TotalRevenue")
          );
        }
        // Return zero revenue if no results found
        return new DoctorRevenue(this.name, 0.0);
      }
    }
  }

  @Override
  public String toString() {
    return String.format(
      "Doctor Details:\n" +
      "  ID:                  %d\n" +
      "  Name:                %s\n" +
      "  Specialty:           %s\n" +
      "  Years of Experience: %d\n" +
      "  Contact Information: %s",
      doctorId,
      name,
      specialty,
      yearsOfExperience,
      contactInformation
    );
  }
}
