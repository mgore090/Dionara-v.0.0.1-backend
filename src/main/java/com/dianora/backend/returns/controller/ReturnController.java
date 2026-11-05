package com.dianora.backend.returns.controller;

import com.dianora.backend.returns.dto.CreateReturnRequest;
import com.dianora.backend.returns.dto.ReturnResponse;
import com.dianora.backend.returns.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReturnResponse createReturn(@Valid @RequestBody CreateReturnRequest request) {
        return returnService.createReturn(request);
    }

    @GetMapping("/user/{userId}")
    public List<ReturnResponse> getMyReturns(@PathVariable("userId") Long userId) {
        return returnService.getMyReturns(userId);
    }

    @GetMapping("/order/{orderId}/user/{userId}")
    public List<ReturnResponse> getReturnsForOrder(@PathVariable("orderId") Long orderId,
                                                   @PathVariable("userId") Long userId) {
        return returnService.getReturnsForOrder(orderId, userId);
    }
}
