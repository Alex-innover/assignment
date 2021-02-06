package com.quest.assignment.controller;

import com.quest.assignment.common.ratelimit.SlidingWindowLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1")
public class RateLimitController {

    private SlidingWindowLimit slidingWindowLimit = new SlidingWindowLimit(20,10,10);

    @GetMapping("limit")
    public ResponseEntity testRateLimit(){
        if(!slidingWindowLimit.tryCount()){
            return ResponseEntity.ok("Over request count access");
        }
        return ResponseEntity.ok("access successful");
    }

}
