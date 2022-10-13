package com.example.springbootamqp.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    String name;
    int age;
}
