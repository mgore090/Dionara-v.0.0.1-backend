package com.dianora.backend.address.repository;

import com.dianora.backend.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUser_IdOrderByDefaultAddressDescIdDesc(
            Long userId
    );

    Optional<Address> findByIdAndUser_Id(
            Long addressId,
            Long userId
    );

    boolean existsByUser_Id(Long userId);

    long countByUser_Id(Long userId);
}