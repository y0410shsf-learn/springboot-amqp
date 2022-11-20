package com.example.springbootamqp.config;

import lombok.Getter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RBMQConfig {

    String defaultExchange = "defaultExchange";
    String defaultQueue = "defaultQueue";
    String defaultRoutingKey = "defaultRoutingKey";

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 設定轉換器(json)
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        // 等同 spring.rabbitmq.template.mandatory，不設這個 returnCallback 不會生效
        rabbitTemplate.setMandatory(true);

        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
