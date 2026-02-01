package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final NotificationService notificationService;

    @Transactional
    public void notifyDelay(int leadTime, Product product) {
        log.info("Notifying delay for product: {}, leadTime: {} days", product.getName(), leadTime);
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
        log.debug("Delay notification sent successfully for product: {}", product.getName());
    }

    public void handleProducts(Set<Product> products) {
        log.info("Starting to handle {} products", products.size());
        for (Product product : products) {
            switch (product.getType()) {
                case NORMAL -> handleNormalProduct(product);
                case SEASONAL -> handleSeasonalProduct(product);
                case EXPIRABLE -> handleExpiredProduct(product);
            }
        }
        log.info("Finished handling all products");
    }

    @Transactional
    public void handleNormalProduct(Product product) {
        log.debug("Handling normal product: {}", product.getName());
        if (isAvailable(product)) {
            log.debug("Decreasing available stock for product: {} from {} to {}",
                    product.getName(), product.getAvailable(), product.getAvailable() - 1);
            decrementProductAvailability(product);
            productRepository.save(product);
        } else {
            log.warn("Product {} is out of stock, notifying delay", product.getName());
            int leadTime = product.getLeadTime();
            notifyDelay(leadTime, product);
        }
    }

    @Transactional
    public void handleSeasonalProduct(Product product) {
        log.debug("Handling seasonal product: {} (season: {} to {})",
                product.getName(), product.getSeasonStartDate(), product.getSeasonEndDate());
        if (isInSeason(product)) {
            log.debug("Product {} is currently in season", product.getName());
            if (isAvailable(product)) {
                log.debug("Decreasing available stock for seasonal product: {}", product.getName());
                decrementProductAvailability(product);
                productRepository.save(product);
            } else if (isOutOfSeason(product)) {
                log.info("Seasonal product {} out of stock, but can be restocked before end of season", product.getName());
                notifyDelay(product.getLeadTime(), product);
            } else {
                log.warn("Seasonal product {} out of stock and cannot be restocked before end of season", product.getName());
                notificationService.sendOutOfStockNotification(product.getName());
                product.setAvailable(0);
                productRepository.save(product);
            }
        } else {
            log.warn("Seasonal product {} is out of season", product.getName());
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        }
    }

    @Transactional
    public void handleExpiredProduct(Product product) {
        log.debug("Handling expirable product: {} (expiry date: {})", product.getName(), product.getExpiryDate());
        if (isAvailable(product) && product.getExpiryDate().isAfter(LocalDate.now())) {
            log.debug("Decreasing available stock for non-expired product: {}", product.getName());
            decrementProductAvailability(product);
            productRepository.save(product);
        } else {
            log.warn("Product {} is expired or unavailable (expiry: {}, available: {})",
                    product.getName(), product.getExpiryDate(), product.getAvailable());
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepository.save(product);
        }
    }

    private void decrementProductAvailability(Product product) {
        product.setAvailable(product.getAvailable() - 1);
    }

    private boolean isAvailable(Product product) {
        return product.getAvailable() > 0;
    }

    private boolean isOutOfSeason(Product product) {
        return LocalDate.now().plusDays(product.getLeadTime()).isBefore(product.getSeasonEndDate());
    }

    private boolean isInSeason(Product product) {
        return LocalDate.now().isAfter(product.getSeasonStartDate()) && LocalDate.now().isBefore(product.getSeasonEndDate());
    }
}
