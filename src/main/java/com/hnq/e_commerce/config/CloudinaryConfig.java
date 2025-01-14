package com.hnq.e_commerce.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(){
        final Map<String,String> config = new HashMap<>();
        config.put("cloud_name","dqr9fj9eg");
        config.put("api_key","263753546146779");
        config.put("api_secret","sjnZ3jx1n7pR-UjnXYWQ3ftakAg");
        return new Cloudinary(config);
    }
}
