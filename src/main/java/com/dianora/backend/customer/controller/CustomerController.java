
package com.dianora.backend.customer.controller;

import com.dianora.backend.customer.dto.CustomerResponse;
import com.dianora.backend.customer.service.CustomerService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomerById(
            @PathVariable("customerId") Long customerId) {

        return customerService.getCustomerById(customerId);
    }
}
