package ru.yandex.practicum.commerce.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.client.OrderClient;
import ru.yandex.practicum.commerce.interactionapi.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interactionapi.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interactionapi.dto.payment.PaymentState;
import ru.yandex.practicum.commerce.interactionapi.dto.store.ProductDto;
import ru.yandex.practicum.commerce.payment.exception.PaymentNotFoundException;
import ru.yandex.practicum.commerce.payment.mapper.PaymentMapper;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final double VAT_RATE = 0.10;

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              PaymentMapper paymentMapper,
                              ShoppingStoreClient shoppingStoreClient,
                              OrderClient orderClient) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
    }

    @Override
    public Double productCost(OrderDto orderDto) {
        validateOrder(orderDto);

        double result = 0.0;

        for (Map.Entry<UUID, Long> entry : orderDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Product quantity must be positive");
            }

            ProductDto product = shoppingStoreClient.getProduct(productId);

            if (product.getPrice() == null) {
                throw new IllegalArgumentException("Product price must not be null");
            }

            result += product.getPrice() * quantity;
        }

        return result;
    }

    @Override
    public Double getTotalCost(OrderDto orderDto) {
        validateOrder(orderDto);

        double productPrice = orderDto.getProductPrice() != null
                ? orderDto.getProductPrice()
                : productCost(orderDto);

        double deliveryPrice = orderDto.getDeliveryPrice() != null
                ? orderDto.getDeliveryPrice()
                : 0.0;

        double vat = productPrice * VAT_RATE;

        return productPrice + vat + deliveryPrice;
    }

    @Override
    @Transactional
    public PaymentDto payment(OrderDto orderDto) {
        validateOrder(orderDto);

        double productPrice = orderDto.getProductPrice() != null
                ? orderDto.getProductPrice()
                : productCost(orderDto);

        double deliveryPrice = orderDto.getDeliveryPrice() != null
                ? orderDto.getDeliveryPrice()
                : 0.0;

        double totalPrice = productPrice + productPrice * VAT_RATE + deliveryPrice;

        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(orderDto.getOrderId());
        payment.setProductPrice(productPrice);
        payment.setDeliveryPrice(deliveryPrice);
        payment.setTotalPrice(totalPrice);
        payment.setPaymentState(PaymentState.PENDING);

        return paymentMapper.toDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentDto paymentSuccess(UUID paymentId) {
        PaymentEntity payment = getPaymentOrThrow(paymentId);

        payment.setPaymentState(PaymentState.SUCCESS);
        PaymentEntity savedPayment = paymentRepository.save(payment);

        orderClient.paymentSuccess(savedPayment.getOrderId());

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional
    public PaymentDto paymentFailed(UUID paymentId) {
        PaymentEntity payment = getPaymentOrThrow(paymentId);

        payment.setPaymentState(PaymentState.FAILED);
        PaymentEntity savedPayment = paymentRepository.save(payment);

        orderClient.paymentFailed(savedPayment.getOrderId());

        return paymentMapper.toDto(savedPayment);
    }

    private PaymentEntity getPaymentOrThrow(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment id must not be null");
        }

        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private void validateOrder(OrderDto orderDto) {
        if (orderDto == null) {
            throw new IllegalArgumentException("Order must not be null");
        }

        if (orderDto.getOrderId() == null) {
            throw new IllegalArgumentException("Order id must not be null");
        }

        if (orderDto.getProducts() == null || orderDto.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Products must not be empty");
        }
    }
}