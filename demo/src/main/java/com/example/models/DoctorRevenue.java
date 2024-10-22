package com.example.models;

public class DoctorRevenue {

  private String doctorName;
  private double totalRevenue;

  // Constructors
  public DoctorRevenue(String doctorName, double totalRevenue) {
    this.doctorName = doctorName;
    this.totalRevenue = totalRevenue;
  }

  // Getters
  public String getDoctorName() {
    return doctorName;
  }

  public double getTotalRevenue() {
    return totalRevenue;
  }
}
