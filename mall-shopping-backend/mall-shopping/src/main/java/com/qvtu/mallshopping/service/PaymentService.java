package com.qvtu.mallshopping.service;

import com.qvtu.mallshopping.model.Payment;
import com.qvtu.mallshopping.repository.PaymentRepository;
import com.qvtu.mallshopping.dto.PaymentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import com.qvtu.mallshopping.model.PaymentProvider;
import com.qvtu.mallshopping.repository.PaymentProviderRepository;
import com.qvtu.mallshopping.dto.PaymentProviderDTO;
import com.qvtu.mallshopping.model.PaymentCapture;
import com.qvtu.mallshopping.repository.PaymentCaptureRepository;
import java.time.LocalDateTime;
import com.qvtu.mallshopping.model.PaymentRefund;
import com.qvtu.mallshopping.repository.PaymentRefundRepository;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final PaymentCaptureRepository paymentCaptureRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    
    public PaymentService(
        PaymentRepository paymentRepository,
        PaymentProviderRepository paymentProviderRepository,
        PaymentCaptureRepository paymentCaptureRepository,
        PaymentRefundRepository paymentRefundRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentProviderRepository = paymentProviderRepository;
        this.paymentCaptureRepository = paymentCaptureRepository;
        this.paymentRefundRepository = paymentRefundRepository;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> listPayments(Integer offset, Integer limit) {
        PageRequest pageRequest = PageRequest.of(
            offset != null ? offset : 0,
            limit != null ? limit : 10
        );
        
        Page<Payment> paymentsPage = paymentRepository.findAll(pageRequest);
        
        List<PaymentDTO> paymentDTOs = paymentsPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
            
        Map<String, Object> response = new HashMap<>();
        response.put("payments", paymentDTOs);
        response.put("count", paymentsPage.getTotalElements());
        response.put("offset", offset != null ? offset : 0);
        response.put("limit", limit != null ? limit : 10);
        
        return response;
    }
    
    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId().toString());
        dto.setAmount(payment.getAmount());
        dto.setCurrencyCode(payment.getCurrencyCode());
        dto.setProviderId(payment.getProviderId());
        return dto;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> listPaymentProviders(Integer offset, Integer limit) {
        PageRequest pageRequest = PageRequest.of(
            offset != null ? offset : 0,
            limit != null ? limit : 10
        );
        
        Page<PaymentProvider> providersPage = paymentProviderRepository.findAll(pageRequest);
        
        List<PaymentProviderDTO> providerDTOs = providersPage.getContent().stream()
            .map(this::convertProviderToDTO)
            .collect(Collectors.toList());
            
        Map<String, Object> response = new HashMap<>();
        response.put("payment_providers", providerDTOs);
        response.put("count", providersPage.getTotalElements());
        response.put("offset", offset != null ? offset : 0);
        response.put("limit", limit != null ? limit : 10);
        
        return response;
    }

    private PaymentProviderDTO convertProviderToDTO(PaymentProvider provider) {
        PaymentProviderDTO dto = new PaymentProviderDTO();
        dto.setId(provider.getProviderId());
        dto.setIsEnabled(provider.getIsEnabled());
        return dto;
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        return convertToDTO(payment);
    }

    @Transactional
    public PaymentDTO capturePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        // 检查支付状态，防止重复捕获
        if (payment.getCapturedAt() != null) {
            throw new RuntimeException("Payment has already been captured");
        }
        
        // 创建支付捕获记录
        PaymentCapture capture = new PaymentCapture();
        capture.setPayment(payment);
        capture.setAmount(payment.getAmount());
        capture.setCreatedBy("system");
        
        // 更新支付状态
        payment.setCapturedAt(LocalDateTime.now());
        payment.setCapturedAmount(payment.getAmount());
        payment.setUpdatedAt(LocalDateTime.now());
        
        // 保存捕获记录
        paymentCaptureRepository.save(capture);
        
        // 保存更新后的支付记录
        payment = paymentRepository.save(payment);
        
        return convertToDTO(payment);
    }

    @Transactional
    public PaymentDTO refundPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        // 检查支付状态
        if (payment.getCapturedAt() == null) {
            throw new RuntimeException("Payment has not been captured yet");
        }
        
        if (payment.getRefundedAmount() != null && 
            payment.getRefundedAmount().compareTo(payment.getAmount()) >= 0) {
            throw new RuntimeException("Payment has already been fully refunded");
        }
        
        // 创建退款记录
        PaymentRefund refund = new PaymentRefund();
        refund.setPayment(payment);
        refund.setAmount(payment.getAmount());
        refund.setCreatedBy("system");
        refund.setNote("Customer requested refund");
        
        // 更新支付状态
        payment.setRefundedAmount(payment.getAmount());
        payment.setUpdatedAt(LocalDateTime.now());
        
        // 保存退款记录
        paymentRefundRepository.save(refund);
        
        // 保存更新后的支付记录
        payment = paymentRepository.save(payment);
        
        return convertToDTO(payment);
    }
} 