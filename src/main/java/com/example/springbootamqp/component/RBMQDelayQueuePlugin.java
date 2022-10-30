package com.example.springbootamqp.component;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RBMQDelayQueuePlugin {

    AmqpAdmin amqpAdmin;

    public RBMQDelayQueuePlugin(AmqpAdmin amqpAdmin,
                                RabbitTemplate rabbitTemplate) {

        this.amqpAdmin = amqpAdmin;

        // 建立 exchange,queue,routing key
//        Map<String, Object> args = new HashMap<>();

        DirectExchange exchange = new DirectExchange("delayExchange");
        // 指定為延遲 exchange 類型
        exchange.setDelayed(true);

        Queue queue = new Queue("delayQueue");

        Binding binding = BindingBuilder.bind(queue).to(exchange).with("routingKey");

        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);

        // message 屬性處理類，指定延遲 5000 毫秒
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                message.getMessageProperties().setDelay(5000);  // 毫秒
                return message;
            }
        };

        String msg = (new Date()).toString();
        rabbitTemplate.convertAndSend("delayExchange", "routingKey", msg, messagePostProcessor);
    }

    @RabbitListener(queues = "delayQueue")
    public void defaultQueueListener(String msg, Message message, Channel channel) throws IOException {
        log.info(msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
