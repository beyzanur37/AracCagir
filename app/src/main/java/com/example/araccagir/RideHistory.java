package com.example.araccagir;

public class RideHistory {
    private String destination;
    private String price;
    private String date;
    private String paymentMethod;
    private String role; // "Yolcu" veya "Sürücü"

    public RideHistory() {
        // Firebase için boş yapıcı metod gerekli
    }

    public RideHistory(String destination, String price, String date, String paymentMethod, String role) {
        this.destination = destination;
        this.price = price;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.role = role;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
