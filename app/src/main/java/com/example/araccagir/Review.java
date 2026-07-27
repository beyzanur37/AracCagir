package com.example.araccagir;

public class Review {
    private String passengerId;
    private float rating;
    private String comment;
    private long timestamp;

    public Review() {
        // Firebase için boş yapıcı
    }

    public Review(String passengerId, float rating, String comment, long timestamp) {
        this.passengerId = passengerId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getPassengerId() { return passengerId; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public long getTimestamp() { return timestamp; }
}
