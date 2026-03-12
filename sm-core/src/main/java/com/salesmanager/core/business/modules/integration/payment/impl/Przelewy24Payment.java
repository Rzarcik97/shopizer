package com.salesmanager.core.business.modules.integration.payment.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.payment.model.PaymentModule;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.salesmanager.core.business.modules.utils.Przelewy24Utils.sha384;

public class Przelewy24Payment implements PaymentModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(Przelewy24Payment.class);

    private static final String SANDBOX_URL = "https://sandbox.przelewy24.pl/api/v1";
    private static final String PRODUCTION_URL = "https://secure.przelewy24.pl/api/v1";

    @Override
    public void validateModuleConfiguration(
            IntegrationConfiguration integrationConfiguration,
            MerchantStore store) throws IntegrationException {

        List<String> errorFields = null;

        Map<String, String> keys = integrationConfiguration.getIntegrationKeys();

        if (keys == null || StringUtils.isBlank(keys.get("merchantId"))) {
            errorFields = new ArrayList<>();
            errorFields.add("merchantId");
        }
        if (keys == null || StringUtils.isBlank(keys.get("posId"))) {
            if (errorFields == null) errorFields = new ArrayList<>();
            errorFields.add("posId");
        }
        if (keys == null || StringUtils.isBlank(keys.get("crcKey"))) {
            if (errorFields == null) errorFields = new ArrayList<>();
            errorFields.add("crcKey");
        }
        if (keys == null || StringUtils.isBlank(keys.get("apiKey"))) {
            if (errorFields == null) errorFields = new ArrayList<>();
            errorFields.add("apiKey");
        }
        if (errorFields != null) {
            IntegrationException ex = new IntegrationException(IntegrationException.ERROR_VALIDATION_SAVE);
            ex.setErrorFields(errorFields);
            throw ex;
        }
    }

    @Override
    public Transaction initTransaction(
            MerchantStore store,
            Customer customer,
            BigDecimal amount,
            Payment payment,
            IntegrationConfiguration configuration,
            IntegrationModule module) throws IntegrationException {
        try {
            P24Config config = new P24Config(configuration);

            // Kwota w groszach (P24 wymaga najniższej jednostki waluty)
            int amountInGrosze = amount.multiply(new BigDecimal("100")).intValue();

            // Unikalny identyfikator sesji
            String sessionId = UUID.randomUUID().toString();

            // Wyliczenie podpisu SHA384
            String signInput = String.format(
                    "{\"sessionId\":\"%s\",\"merchantId\":%d,\"amount\":%d,\"currency\":\"%s\",\"crc\":\"%s\"}",
                    sessionId, config.merchantId, amountInGrosze, payment.getCurrency().getCode(), config.crcKey
            );
            String sign = sha384(signInput);

            // Budowanie body request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("merchantId", config.merchantId);
            requestBody.put("posId", config.posId);
            requestBody.put("sessionId", sessionId);
            requestBody.put("amount", amountInGrosze);
            requestBody.put("currency", payment.getCurrency().getCode());
            requestBody.put("description", "Zamówienie #" + sessionId);
            requestBody.put("email", customer.getEmailAddress());
            requestBody.put("country", "PL");
            requestBody.put("language", "pl");
            requestBody.put("urlReturn", store.getDomainName() + "/order/confirmation");
            requestBody.put("urlStatus", store.getDomainName() + "/api/v1/public/orders/payment/przelewy24/notification");
            requestBody.put("sign", sign);

            // Wywołanie API P24
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(requestBody);

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.baseUrl + "/transaction/register"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", basicAuth(config.posId, config.crcKey))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


                if (response.statusCode() != 200) {
                    LOGGER.error("P24 register error: {}", response.body());
                    throw new IntegrationException("Przelewy24 registration failed: " + response.body());
                }

                JsonNode responseJson = mapper.readTree(response.body());
                String token = responseJson.path("data").path("token").asText();

                // Budowanie URL przekierowania
                String env = configuration.getEnvironment();
                String redirectUrl = (Constants.PRODUCTION_ENVIRONMENT.equals(env)
                        ? "https://secure.przelewy24.pl"
                        : "https://sandbox.przelewy24.pl")
                        + "/trnRequest/" + token;

                Transaction transaction = new Transaction();
                transaction.setAmount(amount);
                transaction.setTransactionDate(new Date());
                transaction.setTransactionType(TransactionType.INIT);
                transaction.setPaymentType(PaymentType.PRZELEWY24);
                transaction.getTransactionDetails().put("TOKEN", token);
                transaction.getTransactionDetails().put("SESSION_ID", sessionId);
                transaction.getTransactionDetails().put("REDIRECT_URL", redirectUrl);


                return transaction;
            }

        } catch (Exception e) {
            throw new IntegrationException(e);
        }
    }

    @Override
    public Transaction authorize(MerchantStore store,
                                 Customer customer,
                                 List<ShoppingCartItem> items,
                                 BigDecimal amount, Payment payment,
                                 IntegrationConfiguration configuration,
                                 IntegrationModule module) throws IntegrationException {
        throw new IntegrationException("Authorize not supported for Przelewy24 - use initTransaction");
    }

    @Override
    public Transaction capture(MerchantStore store,
                               Customer customer,
                               Order order,
                               Transaction capturableTransaction,
                               IntegrationConfiguration configuration,
                               IntegrationModule module) throws IntegrationException {
        throw new IntegrationException("Capture not supported for Przelewy24 - use initTransaction");
    }

    @Override
    public Transaction authorizeAndCapture(MerchantStore store,
                                           Customer customer,
                                           List<ShoppingCartItem> items,
                                           BigDecimal amount,
                                           Payment payment,
                                           IntegrationConfiguration configuration,
                                           IntegrationModule module) throws IntegrationException {
        throw new IntegrationException("AuthorizeAndCapture not supported for Przelewy24 - use initTransaction");
    }

    @Override
    public Transaction refund(boolean partial,
                              MerchantStore store,
                              Transaction transaction,
                              Order order,
                              BigDecimal amount,
                              IntegrationConfiguration configuration,
                              IntegrationModule module) throws IntegrationException {
        try {

            P24Config config = new P24Config(configuration);

            // Kwota zwrotu w groszach
            int amountInGrosze = amount.multiply(new BigDecimal("100")).intValue();

            // sessionId z oryginalnej transakcji
            String sessionId = transaction.getTransactionDetails().get("SESSION_ID");
            String orderId = transaction.getTransactionDetails().get("ORDER_ID");

            // Podpis SHA384
            String signInput = String.format(
                    "{\"sessionId\":\"%s\",\"merchantId\":%d,\"requestId\":\"%s\",\"amount\":%d,\"currency\":\"%s\",\"crc\":\"%s\"}",
                    sessionId, config.merchantId, UUID.randomUUID(), amountInGrosze,
                    order.getCurrency().getCode(), config.crcKey
            );
            String sign = sha384(signInput);

            // Body request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("requestId", UUID.randomUUID().toString());
            requestBody.put("merchantId", config.merchantId);
            requestBody.put("posId", config.posId);
            requestBody.put("sessionId", sessionId);
            requestBody.put("orderId", Integer.parseInt(orderId));
            requestBody.put("amount", amountInGrosze);
            requestBody.put("currency", order.getCurrency().getCode());
            requestBody.put("sign", sign);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(requestBody);

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(config.baseUrl + "/transaction/refund"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", basicAuth(config.posId, config.apiKey))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    LOGGER.error("P24 refund error: {}", response.body());
                    throw new IntegrationException("Przelewy24 refund failed: " + response.body());
                }

                // Budowanie transakcji zwrotu
                Transaction refundTransaction = new Transaction();
                refundTransaction.setAmount(amount);
                refundTransaction.setTransactionDate(new Date());
                refundTransaction.setTransactionType(TransactionType.REFUND);
                refundTransaction.setPaymentType(PaymentType.PRZELEWY24);
                refundTransaction.getTransactionDetails().put("SESSION_ID", sessionId);
                refundTransaction.getTransactionDetails().put("ORDER_ID", orderId);

                return refundTransaction;
            }
        } catch (Exception e) {
            throw new IntegrationException(e);
        }
    }

    private static class P24Config {
        final int merchantId;
        final int posId;
        final String crcKey;
        final String apiKey;
        final String baseUrl;

        P24Config(IntegrationConfiguration configuration) {
            Map<String, String> keys = configuration.getIntegrationKeys();
            this.merchantId = Integer.parseInt(keys.get("merchantId"));
            this.posId = Integer.parseInt(keys.get("posId"));
            this.crcKey = keys.get("crcKey");
            this.apiKey = keys.get("apiKey");
            String env = configuration.getEnvironment();
            this.baseUrl = Constants.PRODUCTION_ENVIRONMENT.equals(env)
                    ? PRODUCTION_URL
                    : SANDBOX_URL;
        }
    }

    private String basicAuth(int posId, String apiKey) {
        String credentials = posId + ":" + apiKey;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }
}
