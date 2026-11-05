package com.dianora.backend.returns.repository;

import com.dianora.backend.returns.entity.ReturnRequest;
import com.dianora.backend.returns.entity.ReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findAllByUser_IdOrderByCreatedAtDesc(Long userId);
    List<ReturnRequest> findAllByOrder_IdOrderByCreatedAtDesc(Long orderId);
    List<ReturnRequest> findAllByOrderByCreatedAtDesc();
    List<ReturnRequest> findAllByOrderItem_IdAndStatusIn(Long orderItemId, List<ReturnStatus> statuses);
}
