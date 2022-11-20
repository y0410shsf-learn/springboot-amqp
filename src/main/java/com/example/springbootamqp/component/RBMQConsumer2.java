package com.example.springbootamqp.component;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
public class RBMQConsumer2 implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    AmqpAdmin amqpAdmin;

    // 為了確保在 @RabbitListener 啟動監聽時，已經有 Queue 的存在，把一些預設要建立的 Queue 放在這
    public RBMQConsumer2(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) throws InterruptedException {
        this.amqpAdmin = amqpAdmin;

        createExchangeQueueAndBind("exchangeName2", "queueName2", "routingKeyName2");

        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);

        // 指定訊息的 id
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend("exchangeName2", "routingKeyName", "testQueueName2 msg", correlationData);
    }

    private void createExchangeQueueAndBind(String exchangeName, String queueName, String routingKey) {

        Map<String, Object> args = new HashMap<>();
        CustomExchange customExchange = new CustomExchange(exchangeName, "direct", true, false, args);

        Queue queue = new Queue(queueName);

        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null);

        amqpAdmin.declareExchange(customExchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);

    }

    // 成功或失敗都會調用
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        System.out.println("setConfirmCallback 訊息: " + correlationData);
        if (Objects.nonNull(correlationData)) {
            System.out.println("setConfirmCallback 訊息: " + correlationData.getReturnedMessage());
        }
        System.out.println("setConfirmCallback 訊息ack: " + b);
        System.out.println("setConfirmCallback 原因: " + s);
        System.out.println("----------------------------------");
        String id = correlationData != null ? correlationData.getId() : "";
    }

    // 失敗時才會調用
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        System.out.println("setReturnCallBack message: " + returnedMessage.getMessage());
        System.out.println("setReturnCallBack replyCode: " + returnedMessage.getReplyCode());
        System.out.println("setReturnCallBack replyText: " + returnedMessage.getReplyText());
        System.out.println("setReturnCallBack exchange: " + returnedMessage.getExchange());
        System.out.println("setReturnCallBack routingKey: " + returnedMessage.getRoutingKey());
    }
}
