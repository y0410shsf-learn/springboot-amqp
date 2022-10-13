package com.example.springbootamqp.component;

import com.example.springbootamqp.config.RBMQConfig;
import com.example.springbootamqp.model.Person;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RBMQConsumer {

    AmqpAdmin amqpAdmin;
    RBMQConfig rbmqConfig;

    // 為了確保在 @RabbitListener 啟動監聽時，已經有 Queue 的存在，把一些預設要建立的 Queue 放在這
    public RBMQConsumer(AmqpAdmin amqpAdmin,
                        RBMQConfig rbmqConfig) {
        this.amqpAdmin = amqpAdmin;
        this.rbmqConfig = rbmqConfig;

        createExchangeQueueAndBind(rbmqConfig.getDefaultExchange(), rbmqConfig.getDefaultQueue(), rbmqConfig.getDefaultRoutingKey());
    }

    private void createExchangeQueueAndBind(String exchangeName, String queueName, String routingKey) {

        Map<String, Object> args = new HashMap<>();
        CustomExchange customExchange = new CustomExchange(exchangeName, "topic", true, false, args);

        Queue queue = new Queue(queueName);

        Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null);

        amqpAdmin.declareExchange(customExchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);

    }

    @RabbitListener(queues = "defaultQueue")
    public void defaultQueueListener(Person msg, Message message, Channel channel) throws IOException {
        System.out.println(msg.getName());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
