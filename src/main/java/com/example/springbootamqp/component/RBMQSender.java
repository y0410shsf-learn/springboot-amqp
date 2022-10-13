package com.example.springbootamqp.component;

import com.example.springbootamqp.model.Person;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RBMQSender {

    private RabbitTemplate rabbitTemplate;

    AmqpAdmin amqpAdmin;

    public RBMQSender(RabbitTemplate rabbitTemplate,
                      AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    public void sendMsg(String exchangeName, String queueName, String routingKey, String msg) {

        // 避免 exchangeName, queueName, routingKey 不存在s
        createExchangeQueueAndBind(exchangeName, queueName, routingKey);

        rabbitTemplate.convertAndSend(exchangeName, routingKey, msg);

    }

    public void sendMsg(String exchangeName, String queueName, String routingKey, Person p) {

        createExchangeQueueAndBind(exchangeName, queueName, routingKey);

        rabbitTemplate.convertAndSend(exchangeName, routingKey, p);

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

}
