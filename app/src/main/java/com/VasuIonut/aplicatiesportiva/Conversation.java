package com.VasuIonut.aplicatiesportiva;

public class Conversation {
    private String id;
    private String name;
    private String description;
    private String profileImageUrl;

    public Conversation(String id, String name, String description, String profileImageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.profileImageUrl = profileImageUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
