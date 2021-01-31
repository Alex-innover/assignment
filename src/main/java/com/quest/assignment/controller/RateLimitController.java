package com.quest.assignment.controller;

import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1")
public class RateLimitController {

    private Bucket bucket;

    @GetMapping("limit")
    public ResponseEntity testRateLimit(@RequestHeader(value = "X-api-key") String apiKey){

        return ResponseEntity.ok("access successful");
    }

}
