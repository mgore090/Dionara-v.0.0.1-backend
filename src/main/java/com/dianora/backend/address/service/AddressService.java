package com.dianora.backend.address.service;

import com.dianora.backend.address.dto.AddressRequest;
import com.dianora.backend.address.dto.AddressResponse;
import com.dianora.backend.address.entity.Address;
import com.dianora.backend.address.repository.AddressRepository;
import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(
            AddressRepository addressRepository,
            UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    // Add address for a user.
    public AddressResponse addAddress(
            Long userId,
            AddressRequest request) {

        User user = findActiveUser(userId);

        Address address = new Address();
        copyRequestToAddress(request, address);

        if (request.defaultAddress()
                || !addressRepository.existsByUser_Id(userId)) {
            setDefaultAddressForUser(userId, null);
            address.setDefaultAddress(true);
        } else {
            address.setDefaultAddress(false);
        }

        user.addAddress(address);

        Address savedAddress = addressRepository.save(address);

        return mapToResponse(savedAddress);
    }

    // List all addresses for a user.
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(Long userId) {

        findActiveUser(userId);

        return addressRepository
                .findAllByUser_IdOrderByDefaultAddressDescIdDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get one address belonging to the specified user.
    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long userId, Long addressId) {

        findActiveUser(userId);

        Address address = findAddress(userId, addressId);
        return mapToResponse(address);
    }

    // Update address details.
    public AddressResponse updateAddress(
            Long userId,
            Long addressId,
            AddressRequest request) {

        findActiveUser(userId);

        Address address = findAddress(userId, addressId);
        copyRequestToAddress(request, address);

        if (request.defaultAddress()) {
            setDefaultAddressForUser(userId, addressId);
            address.setDefaultAddress(true);
        }

        return mapToResponse(addressRepository.save(address));
    }

    // Set an existing address as default.
    public AddressResponse setDefaultAddress(
            Long userId,
            Long addressId) {

        findActiveUser(userId);

        Address address = findAddress(userId, addressId);

        setDefaultAddressForUser(userId, addressId);
        address.setDefaultAddress(true);

        return mapToResponse(addressRepository.save(address));
    }

    // Delete an address.
    public void deleteAddress(Long userId, Long addressId) {

        findActiveUser(userId);

        Address address = findAddress(userId, addressId);
        boolean wasDefault = address.isDefaultAddress();

        addressRepository.delete(address);

        // If the default address was deleted, promote another address.
        if (wasDefault) {
            List<Address> remaining =
                    addressRepository
                            .findAllByUser_IdOrderByDefaultAddressDescIdDesc(userId);

            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setDefaultAddress(true);
                addressRepository.save(newDefault);
            }
        }
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found: " + userId
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is inactive"
            );
        }

        return user;
    }

    private Address findAddress(Long userId, Long addressId) {
        return addressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Address not found for this user"
                ));
    }

    private void setDefaultAddressForUser(
            Long userId,
            Long exceptAddressId) {

        List<Address> addresses =
                addressRepository
                        .findAllByUser_IdOrderByDefaultAddressDescIdDesc(userId);

        for (Address address : addresses) {
            if (exceptAddressId == null
                    || !address.getId().equals(exceptAddressId)) {
                address.setDefaultAddress(false);
            }
        }

        addressRepository.saveAll(addresses);
    }

    private void copyRequestToAddress(
            AddressRequest request,
            Address address) {

        address.setFullName(request.fullName().trim());
        address.setPhone(request.phone().trim());
        address.setAddressLine1(request.addressLine1().trim());
        address.setAddressLine2(
                request.addressLine2() == null
                        ? null
                        : request.addressLine2().trim()
        );
        address.setCity(request.city().trim());
        address.setState(request.state().trim());
        address.setCountry(request.country().trim());
        address.setPostalCode(request.postalCode().trim());
        address.setAddressType(request.addressType().trim().toUpperCase());
    }

    private AddressResponse mapToResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getUser().getId(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode(),
                address.getAddressType(),
                address.isDefaultAddress(),
                address.getCreatedAt()
        );
    }
}