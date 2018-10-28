package com.agney.agneyweb.dto;

import com.agney.agneyweb.model.Person;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.util.Assert;

@Getter
@ToString
@AllArgsConstructor
public class PersonDto {
    private final String name;
    private final Integer age;

    public PersonDto(Person person) {
        Assert.notNull(person, "person is null");

        this.name = person.getName();
        this.age = person.getAge();
    }
}
