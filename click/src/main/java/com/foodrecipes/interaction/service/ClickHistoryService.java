package com.foodrecipes.interaction.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodrecipes.interaction.entity.ClickHistory;
import com.foodrecipes.interaction.repository.ClickHistoryRepository;

import jakarta.transaction.Transactional;

@Service
public class ClickHistoryService {

	@Autowired
	private ClickHistoryRepository clickHistoryRepository;

	private static final String CLICK_COUNT_KEY = "recipe_click_counts";
	private static final String MOST_CLICKED_KEY = "most_clicked_recipes";

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	// Method to initialize cache
	public void initializeCache() throws JsonProcessingException {
	    // Fetch all click counts
	    List<Object[]> clickCounts = clickHistoryRepository.findClickCounts();

	    // Store click counts in Redis hash
	    for (Object[] entry : clickCounts) {
	        Long recipeId = (Long) entry[0];
	        Long clickCount = (Long) entry[1];
	        redisTemplate.opsForHash().put(CLICK_COUNT_KEY, recipeId.toString(), clickCount);
	    }

	    // Sort and get the list of recipe IDs based on click counts
	    List<Long> allMostClickedRecipeIds = clickCounts.stream()
	        .sorted((e1, e2) -> Long.compare((Long) e2[1], (Long) e1[1]))
	        .map(e -> (Long) e[0])
	        .collect(Collectors.toList());

	    // Serialize the full list of recipe IDs to a JSON string
	    String allMostClickedJson = objectMapper.writeValueAsString(allMostClickedRecipeIds);

	    // Store the full list in Redis
	    redisTemplate.opsForValue().set(MOST_CLICKED_KEY, allMostClickedJson);
	}


	// Method to get most clicked recipes
	// Method to get paginated most clicked recipes
	public List<Long> getMostClickedRecipes(int pageNumber, int pageSize) {
	    String mostClickedJson = (String) redisTemplate.opsForValue().get(MOST_CLICKED_KEY);
	    if (mostClickedJson != null) {
	        try {
	            // Deserialize JSON string back to List<Long>
	            List<Long> allMostClickedRecipeIds = objectMapper.readValue(mostClickedJson, new TypeReference<List<Long>>() {});
	            
	            // Calculate start and end indices for pagination
	            int totalSize = allMostClickedRecipeIds.size();
	            int startIndex = pageNumber * pageSize;
	            int endIndex = Math.min(startIndex + pageSize, totalSize);
	            
	            // Return the paginated list
	            if (startIndex < totalSize) {
	                return allMostClickedRecipeIds.subList(startIndex, endIndex);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	            System.err.println("Error deserializing JSON: " + mostClickedJson);
	        }
	    }
	    return Collections.emptyList(); // Return an empty list if no data or deserialization fails
	}




	@Transactional
	public ClickHistory addClick(Long userId, Long recipeId) {
	    // Add click to the database
	    ClickHistory clickHistory = new ClickHistory();
	    clickHistory.setUserId(userId);
	    clickHistory.setRecipeId(recipeId);
	    clickHistoryRepository.save(clickHistory);

	    // Update Redis cache
	    Long currentCount = (Long) redisTemplate.opsForHash().get(CLICK_COUNT_KEY, recipeId.toString());
	    if (currentCount == null) {
	        currentCount = 0L;
	    }
	    redisTemplate.opsForHash().put(CLICK_COUNT_KEY, recipeId.toString(), currentCount + 1);

	    // Optionally, update most clicked recipes
	    try {
	        updateMostClickedRecipes();
	    } catch (Exception e) {
	        e.printStackTrace();
	        // Log error details
	        System.err.println("Error updating most clicked recipes.");
	    }

	    return clickHistory;
	}
	

	private void updateMostClickedRecipes() {
	    // Get all click counts from Redis
	    Map<Object, Object> clickCounts = redisTemplate.opsForHash().entries(CLICK_COUNT_KEY);

	    // Sort and get the top recipes
	    List<Long> mostClickedRecipeIds = clickCounts.entrySet().stream()
	        .sorted((e1, e2) -> Long.compare((Long) e2.getValue(), (Long) e1.getValue()))
	        .map(e -> Long.valueOf((String) e.getKey())) // Ensure correct casting
	        .collect(Collectors.toList());

	    // Serialize List<Long> to JSON string
	    String mostClickedJson;
	    try {
	        mostClickedJson = objectMapper.writeValueAsString(mostClickedRecipeIds);
	        redisTemplate.opsForValue().set(MOST_CLICKED_KEY, mostClickedJson);
	    } catch (JsonProcessingException e) {
	        e.printStackTrace();
	        // Log error details
	        System.err.println("Error serializing most clicked recipes.");
	    }
	}


}