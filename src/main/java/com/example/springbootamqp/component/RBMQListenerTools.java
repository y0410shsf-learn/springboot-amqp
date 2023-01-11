package com.example.springbootamqp.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RBMQListenerTools {

    RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    ConnectionFactory connectionFactory;

    AmqpAdmin amqpAdmin;

    public RBMQListenerTools(RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry,
                             ConnectionFactory connectionFactory,
                             AmqpAdmin amqpAdmin) {

        this.rabbitListenerEndpointRegistry = rabbitListenerEndpointRegistry;
        this.connectionFactory = connectionFactory;
        this.amqpAdmin = amqpAdmin;

    }

    /**
     * 建立 exchange/queue/binding 並加入監聽器
     */
    public void addRBMQListener(String exchangeName, String queueName, String routingKey, MessageListener messageListener) {

        checkAndCreateQueue(exchangeName, queueName, routingKey);

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        SimpleRabbitListenerEndpoint simpleRabbitListenerEndpoint = new SimpleRabbitListenerEndpoint();
        simpleRabbitListenerEndpoint.setId(UUID.randomUUID().toString());
        simpleRabbitListenerEndpoint.setMessageListener(messageListener);
        simpleRabbitListenerEndpoint.setQueueNames(queueName);
        factory.setConnectionFactory(connectionFactory);

        rabbitListenerEndpointRegistry.registerListenerContainer(simpleRabbitListenerEndpoint,factory,true);
    }

    /**
     * 啟動監聽
     */
    public void startAllRBMQListener() {

        Collection<MessageListenerContainer> containers = rabbitListenerEndpointRegistry.getListenerContainers();
        if(containers != null && containers.size() > 0) {
            for(MessageListenerContainer container : containers) {
                if (!container.isRunning()) {
                    container.start();
                    log.info("rabbit container start");
                }
            }
        }
    }

    /**
     * 關閉所有的 rbmq 監聽
     */
    public void closeAllRBMQListener() {
        rabbitListenerEndpointRegistry.stop();
    }

    /**
     * 關閉指定的 queue 的監聽
     */
    public void stopSpecificListener(String queueName) {
        Collection<MessageListenerContainer> containers = rabbitListenerEndpointRegistry.getListenerContainers();
        if(containers != null && containers.size() > 0) {
            for(MessageListenerContainer container : containers) {
                if (isQueueListener(queueName, container)) {
                    container.stop();
                }
            }
        }
    }

    /**
     * 判斷某個 queue 是否有監聽
     */
    private boolean isQueueListener(String queueName, MessageListenerContainer listenerContainer) {
        if (listenerContainer instanceof AbstractMessageListenerContainer) {
            AbstractMessageListenerContainer abstractMessageListenerContainer = (AbstractMessageListenerContainer) listenerContainer;
            List<String> queueNames = Arrays.stream(abstractMessageListenerContainer.getQueueNames()).collect(Collectors.toList());
            return queueNames.contains(queueName);
        }
        return false;
    }

    private void checkAndCreateQueue(String exchangeName, String queueName, String routingKey) {

        try {

            Map<String, Object> args = new HashMap<>();
            CustomExchange customExchange = new CustomExchange(exchangeName, "topic", true, false, args);

            Queue queue = new Queue(queueName);

            Binding binding = new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName, routingKey, null);

            amqpAdmin.declareExchange(customExchange);
            amqpAdmin.declareQueue(queue);
            amqpAdmin.declareBinding(binding);

        } catch (Exception e) {

            log.info("建立監聽器時, 建立 rbmq queue 綁 binding 時失敗。 exchange:{}, routingKey:{}, queue:{}", exchangeName, queueName, queueName);

            throw e;
        }

    }
}
