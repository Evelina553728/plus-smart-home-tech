package ru.yandex.practicum.commerce.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;

@Component
public class PaymentMapper {

    public PaymentDto toDto(PaymentEntity payment) {
        PaymentDto dto = new PaymentDto();

        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrderId());
        dto.setProductPrice(payment.getProductPrice());
        dto.setDeliveryPrice(payment.getDeliveryPrice());
        dto.setTotalPrice(payment.getTotalPrice());
        dto.setPaymentState(payment.getPaymentState());

        return dto;
    }
}