package com.example.springbootamqp.config;

import lombok.Getter;

@Getter
public class RBMQConfig {

    String defaultExchange = "defaultExchange";
    String defaultQueue = "defaultQueue";
    String defaultRoutingKey = "defaultRoutingKey";

}
