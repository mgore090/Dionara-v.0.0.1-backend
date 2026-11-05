package com.dianora.backend.returns.controller;

import com.dianora.backend.returns.dto.AdminReturnUpdateRequest;
import com.dianora.backend.returns.dto.ReturnResponse;
import com.dianora.backend.returns.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/returns")
public class AdminReturnController {

    private final ReturnService returnService;

    public AdminReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping
    public List<ReturnResponse> getAllReturns() {
        return returnService.getAllReturnsForAdmin();
    }

    @PatchMapping("/{returnId}")
    public ReturnResponse updateReturn(@PathVariable("returnId") Long returnId,
                                      @Valid @RequestBody AdminReturnUpdateRequest request) {
        return returnService.updateReturnForAdmin(returnId, request);
    }
}
