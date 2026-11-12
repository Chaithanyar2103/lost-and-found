package com.example.lostfound;

import java.time.Instant;

public class Item {

    private final long id;
    private final String name;
    private final String description;
    private final String location;
    private volatile String status;
    private final String contact;
    private final Instant createdAt;

    public Item(long id, String name, String description, String location, String status, String contact) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.status = status;
        this.contact = contact;
        this.createdAt = Instant.now();
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public String getContact() { return contact; }
    public Instant getCreatedAt() { return createdAt; }

    public void claim() {
        this.status = "CLAIMED";
    }
}
