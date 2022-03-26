package org.micro.customer;

import lombok.AllArgsConstructor;
import org.micro.clients.fraud.FraudCheckResponse;
import org.micro.clients.fraud.FraudClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final RestTemplate restTemplate;
    private final FraudClient client;

    public void register(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
        repository.saveAndFlush(customer);

        FraudCheckResponse checkResponse = client.isFraudster(customer.getId());

        if (checkResponse.isFraudster()) {
            throw new IllegalArgumentException("Customer is Fraudster!");
        }
    }
}
