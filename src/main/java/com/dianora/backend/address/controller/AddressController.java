package com.dianora.backend.address.controller;

import com.dianora.backend.address.dto.AddressRequest;
import com.dianora.backend.address.dto.AddressResponse;
import com.dianora.backend.address.service.AddressService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> addAddress(
            @PathVariable Long userId,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.addAddress(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                addressService.getAddresses(userId)
        );
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> getAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        return ResponseEntity.ok(
                addressService.getAddress(userId, addressId)
        );
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(
                addressService.updateAddress(userId, addressId, request)
        );
    }

    @PatchMapping("/{addressId}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        return ResponseEntity.ok(
                addressService.setDefaultAddress(userId, addressId)
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}