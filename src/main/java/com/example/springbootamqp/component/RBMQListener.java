package com.example.springbootamqp.component;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RBMQListener implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {

        String msg = new String(message.getBody());

        log.info(msg);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

}
