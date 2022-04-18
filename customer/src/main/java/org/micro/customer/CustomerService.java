package org.micro.customer;

import lombok.AllArgsConstructor;
import org.micro.amqp.RabbitMQMessageProducer;
import org.micro.clients.fraud.FraudCheckResponse;
import org.micro.clients.fraud.FraudClient;
import org.micro.clients.notification.NotificationRequest;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final FraudClient fraudClient;
    private final RabbitMQMessageProducer rabbitMQMessageProducer;


    public void register(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();
        repository.saveAndFlush(customer);

        FraudCheckResponse checkResponse = fraudClient.isFraudster(customer.getId());

        if (checkResponse.isFraudster()) {
            throw new IllegalArgumentException("Customer is Fraudster!");
        }

        NotificationRequest notificationRequest = new NotificationRequest(
                customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to My_MicroServices...",
                        customer.getFirstName())
        );

        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );
    }
}
