package com.foodrecipes.recipegetter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profile")  // Ensure the table name is correctly specified
public class UserProfile {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    
    private String profilePicture;
    
    public UserProfile() {
		super();
	}

	// Getters and setters

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
		return profilePicture;
	}

	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}

	public Long getId() {
		return id;
	}
 
	public UserProfile(String username, String profilePicture) {
		super();
		this.username = username;
		this.profilePicture = profilePicture;
	}

	public void setUsername(String username) {
        this.username = username;
    }

    
}
