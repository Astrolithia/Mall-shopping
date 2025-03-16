package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.dto.ReservationCreateRequest;
import com.qvtu.mallshopping.dto.ReservationUpdateRequest;
import com.qvtu.mallshopping.exception.ResourceNotFoundException;
import com.qvtu.mallshopping.model.Reservation;
import com.qvtu.mallshopping.model.Inventory;
import com.qvtu.mallshopping.model.Location;
import com.qvtu.mallshopping.repository.ReservationRepository;
import com.qvtu.mallshopping.repository.InventoryRepository;
import com.qvtu.mallshopping.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

@Slf4j
@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        InventoryRepository inventoryRepository,
        LocationRepository locationRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.inventoryRepository = inventoryRepository;
        this.locationRepository = locationRepository;
    }

    @Transactional
    public Map<String, Object> createReservation(ReservationCreateRequest request) {
        // 查找库存项目
        Inventory inventoryItem = inventoryRepository.findById(Long.parseLong(request.getInventoryItemId()))
            .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        // 查找位置
        Location location = locationRepository.findById(Long.parseLong(request.getLocationId()))
            .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        // 创建预定
        Reservation reservation = new Reservation();
        reservation.setLineItemId(request.getLineItemId());
        reservation.setInventoryItem(inventoryItem);
        reservation.setLocation(location);
        reservation.setQuantity(request.getQuantity());
        reservation.setExternalId(request.getExternalId());
        reservation.setDescription(request.getDescription());
        reservation.setMetadata(request.getMetadata());

        // 保存预定
        reservation = reservationRepository.save(reservation);

        // 返回响应
        return convertToMap(reservation);
    }

    public Map<String, Object> listReservations(Integer offset, Integer limit) {
        // 创建分页请求
        PageRequest pageRequest = PageRequest.of(
            offset != null ? offset : 0,
            limit != null ? limit : 10
        );

        // 获取预定列表
        Page<Reservation> reservations = reservationRepository.findByDeletedAtIsNull(pageRequest);

        // 使用 LinkedHashMap 保持字段顺序
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("limit", limit != null ? limit : 10);
        response.put("offset", offset != null ? offset : 0);
        response.put("count", reservations.getTotalElements());
        response.put("reservations", reservations.getContent().stream()
            .map(this::convertToMap)
            .toList());

        return response;
    }

    private Map<String, Object> convertToMap(Reservation reservation) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", reservation.getId().toString());
        result.put("line_item_id", reservation.getLineItemId());
        result.put("location_id", reservation.getLocation().getId().toString());
        result.put("quantity", reservation.getQuantity());
        result.put("external_id", reservation.getExternalId());
        result.put("description", reservation.getDescription());
        result.put("inventory_item_id", reservation.getInventoryItem().getId().toString());
        return result;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
            
        reservation.setDeletedAt(LocalDateTime.now());
        reservationRepository.save(reservation);
    }

    @Transactional
    public Map<String, Object> updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        if (request.getDescription() != null) {
            reservation.setDescription(request.getDescription());
        }

        if (request.getMetadata() != null) {
            reservation.setMetadata(request.getMetadata());
        }

        reservation = reservationRepository.save(reservation);
        return Collections.singletonMap("reservation", convertToMap(reservation));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));

        return Collections.singletonMap("reservation", convertToMap(reservation));
    }
} 