package ru.yandex.practicum.commerce.shoppingcart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interactionapi.client.WarehouseClient;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interactionapi.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.exception.ProductNotFoundInShoppingCartException;
import ru.yandex.practicum.commerce.shoppingcart.exception.ShoppingCartDeactivatedException;
import ru.yandex.practicum.commerce.shoppingcart.model.ShoppingCart;
import ru.yandex.practicum.commerce.shoppingcart.repository.ShoppingCartRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final WarehouseClient warehouseClient;

    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository,
                                   WarehouseClient warehouseClient) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.warehouseClient = warehouseClient;
    }

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(username);

        return toDto(shoppingCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(username);
        checkCartIsActive(shoppingCart);
        validateProducts(products);

        Map<UUID, Long> updatedProducts = new HashMap<>(shoppingCart.getProducts());

        products.forEach((productId, quantity) ->
                updatedProducts.merge(productId, quantity, Long::sum)
        );

        ShoppingCartDto cartForWarehouseCheck = new ShoppingCartDto(username, updatedProducts);
        warehouseClient.checkProductQuantityEnoughForShoppingCart(cartForWarehouseCheck);

        shoppingCart.setProducts(updatedProducts);
        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);

        return toDto(savedCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(username);
        checkCartIsActive(shoppingCart);
        validateChangeProductQuantityRequest(request);

        UUID productId = request.getProductId();

        if (!shoppingCart.getProducts().containsKey(productId)) {
            throw new ProductNotFoundInShoppingCartException(productId);
        }

        Map<UUID, Long> updatedProducts = new HashMap<>(shoppingCart.getProducts());
        updatedProducts.put(productId, request.getNewQuantity());

        ShoppingCartDto cartForWarehouseCheck = new ShoppingCartDto(username, updatedProducts);
        warehouseClient.checkProductQuantityEnoughForShoppingCart(cartForWarehouseCheck);

        shoppingCart.setProducts(updatedProducts);
        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);

        return toDto(savedCart);
    }

    @Override
    @Transactional
    public ShoppingCartDto removeProductFromShoppingCart(String username, Map<UUID, Long> products) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(username);
        checkCartIsActive(shoppingCart);
        validateProducts(products);

        Map<UUID, Long> updatedProducts = new HashMap<>(shoppingCart.getProducts());

        products.keySet().forEach(productId -> {
            if (!updatedProducts.containsKey(productId)) {
                throw new ProductNotFoundInShoppingCartException(productId);
            }

            updatedProducts.remove(productId);
        });

        shoppingCart.setProducts(updatedProducts);
        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);

        return toDto(savedCart);
    }

    @Override
    @Transactional
    public void deactivateShoppingCart(String username) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(username);
        shoppingCart.setActive(false);
        shoppingCartRepository.save(shoppingCart);
    }

    private ShoppingCart getOrCreateShoppingCart(String username) {
        validateUsername(username);

        return shoppingCartRepository.findById(username)
                .orElseGet(() -> shoppingCartRepository.save(new ShoppingCart(username)));
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
    }

    private void checkCartIsActive(ShoppingCart shoppingCart) {
        if (Boolean.FALSE.equals(shoppingCart.getActive())) {
            throw new ShoppingCartDeactivatedException(shoppingCart.getUsername());
        }
    }

    private ShoppingCartDto toDto(ShoppingCart shoppingCart) {
        return new ShoppingCartDto(
                shoppingCart.getUsername(),
                new HashMap<>(shoppingCart.getProducts())
        );
    }

    private void validateProducts(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Products must not be empty");
        }

        products.forEach((productId, quantity) -> {
            if (productId == null) {
                throw new IllegalArgumentException("Product id must not be null");
            }

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Product quantity must be positive");
            }
        });
    }

    private void validateChangeProductQuantityRequest(ChangeProductQuantityRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Change product quantity request must not be null");
        }

        if (request.getProductId() == null) {
            throw new IllegalArgumentException("Product id must not be null");
        }

        if (request.getNewQuantity() == null || request.getNewQuantity() <= 0) {
            throw new IllegalArgumentException("New product quantity must be positive");
        }
    }
}