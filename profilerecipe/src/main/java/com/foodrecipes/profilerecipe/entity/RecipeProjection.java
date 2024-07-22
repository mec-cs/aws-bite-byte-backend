package com.foodrecipes.profilerecipe.entity;

import java.time.LocalDateTime;

public interface RecipeProjection {
    Long getId();
    String getName();
    String getDescription();
    LocalDateTime getDateCreated();
    String getImage();
    Long getOwnerId();
}