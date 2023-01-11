package com.example.springbootamqp.controller;

import com.example.springbootamqp.component.RBMQListener;
import com.example.springbootamqp.component.RBMQListenerTools;
import com.example.springbootamqp.component.RBMQSender;
import com.example.springbootamqp.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
public class HelloWorldController {

    RBMQSender rbmqSender;

    public HelloWorldController(RBMQSender rbmqSender){
        this.rbmqSender = rbmqSender;
    }

    @GetMapping("hello")
    public String hello() {

        Person p = Person.builder()
                        .name("alice")
                        .age(8)
                        .build();

        rbmqSender.sendMsg("defaultExchange", "defaultQueue", "defaultRoutingKey", p);

        return "ok";
    }

    @Autowired
    RBMQListenerTools rbmqListenerTools;

    @GetMapping("hello2")
    public String hello2() {

        RBMQListener rbmqListener = new RBMQListener();
        rbmqListenerTools.addRBMQListener("addListener_exchange", "addListener_queue", "addListener_routing", rbmqListener);

        return "ok";
    }

}
