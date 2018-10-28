package com.agney.agneyweb.controller;

import com.agney.agneyweb.dto.PersonDto;
import com.agney.agneyweb.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class PersonController {
    @Autowired
    PersonService personService;

    @PostMapping(value = "create")
    public void createPerson(@RequestBody PersonDto dto) {
        personService.createPerson(dto);
    }

    @GetMapping(value = "person/{personId}")
    public PersonDto getPerson(@PathVariable("personId") long id) {
        return personService.getPerson(id);
    }

    @DeleteMapping(value = "person/{personId}")
    public void deletePerson(@PathVariable("personId") long id) {
        personService.deletePerson(id);
    }

    @GetMapping(value = "test")
    public String test() {
        return "hi there everyone!";
    }
}
