package com.tomwang.roasthub.service.impl;

import com.tomwang.roasthub.dao.pojo.RegistrationRequest;
import com.tomwang.roasthub.service.EmailService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "RoastHubQueue", ackMode = "MANUAL")
public class RegistrationConsumer {
    @Autowired
    private EmailService emailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RegistrationConsumer.class);

    @RabbitHandler
    public void receiveMessage(RegistrationRequest request, Message message, Channel channel) throws IOException {
        logger.info("Received message: " + request);
        try {
            emailService.sendEmail(request.getEmail(), "Success Register for RoastHub", "You have been a member of RoastHub, lets share roasts with others");
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            logger.error("Error processing message", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);

            // Optionally, handle the error, e.g., by rejecting the message
        }
    }
}
