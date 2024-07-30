package com.foodrecipes.likedislike.proxy;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.foodrecipes.likedislike.entity.UserProfile;

@FeignClient(name = "profile-getter")
public interface UserProfileProxy {
	
	@GetMapping("/profile-getter/get-user-profiles")
    List<UserProfile> getUserProfiles(@RequestParam List<Long> ids);
    
}

