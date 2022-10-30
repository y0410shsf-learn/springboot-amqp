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
public class RBMQDelayQueueDeadLetter {

    public RBMQDelayQueueDeadLetter(AmqpAdmin amqpAdmin,
                                    RabbitTemplate rabbitTemplate) {

        // 建立用來放延遲訊息的 exchange 和 queue 並綁定
        // 設定 queue 時要死定，在這個 queeu 裡面過期的訊息(dead letter)要轉到哪個地方
        DirectExchange delayExchange2 = new DirectExchange("delayExchange2");

        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange","deadLetterExchange");
        args.put("x-dead-letter-routing-key","deadLetter");
        // 指定隊列中的訊息幾毫秒後過期
        args.put("x-message-ttl", 10000);

        Queue delayQueue2 = QueueBuilder.durable("delayQueue2").withArguments(args).build();
        Binding delayBinding = BindingBuilder.bind(delayQueue2).to(delayExchange2).with("delayRoutingKey");

        amqpAdmin.declareExchange(delayExchange2);
        amqpAdmin.declareQueue(delayQueue2);
        amqpAdmin.declareBinding(delayBinding);

        // 建立用來收 dead letter 的 exchange 和 queue
        DirectExchange deadLetterExchange = new DirectExchange("deadLetterExchange");
        Queue deadLetterQueue = new Queue("deadLetterQueue");
        Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("deadLetter");

        amqpAdmin.declareExchange(deadLetterExchange);
        amqpAdmin.declareQueue(deadLetterQueue);
        amqpAdmin.declareBinding(deadLetterBinding);

        // message 屬性處理類，指定 5000 毫秒後訊息過期，轉入死信佇列並被監聽器接收
        MessagePostProcessor messagePostProcessorDelay5000ms = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("5000");
                return message;
            }
        };

        // 訊息送入 delayQueue 後，5000 ms 後過期，轉入 deadLetter 佇列
        rabbitTemplate.convertAndSend("delayExchange2", "delayRoutingKey", "delay 5 sec" + (new Date()).toString(), messagePostProcessorDelay5000ms);

        // message 屬性處理類，指定 5000 毫秒後訊息過期
        MessagePostProcessor messagePostProcessorDelay10000ms = message -> {
            message.getMessageProperties().setExpiration("10000");
            return message;
        };

        // 訊息送入 delayQueue 後，10000 ms 後過期，轉入 deadLetter 佇列
        rabbitTemplate.convertAndSend("delayExchange2", "delayRoutingKey", "delay 10 sec" + (new Date()).toString(), messagePostProcessorDelay10000ms);

        // 如果兩條訊息送入 rabbitmq 的順序調換，要等到先進 queue 的訊息過期處理掉之後，才會輪到處理第二個送進 queue 的訊息
        // rabbitTemplate.convertAndSend("delayExchange2", "delayRoutingKey", "delay 10 sec" + (new Date()).toString(), messagePostProcessorDelay10000ms);
        // rabbitTemplate.convertAndSend("delayExchange2", "delayRoutingKey", "delay 5 sec" + (new Date()).toString(), messagePostProcessorDelay5000ms);
    }

    @RabbitListener(queues = "deadLetterQueue")
    public void defaultQueueListener(String msg, Message message, Channel channel) throws IOException {
        log.info(msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
