package com.agney.agneyweb.service;

import com.agney.agneyweb.dao.PersonDao;
import com.agney.agneyweb.dto.PersonDto;
import com.agney.agneyweb.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class PersonService {
    @Autowired
    PersonDao personDao;

    public void createPerson(PersonDto dto) {
        Assert.notNull(dto, "dto is null");
        String name = dto.getName();
        Assert.notNull(name, "name is null");
        Integer age = dto.getAge();
        Assert.notNull(name, "age is null");

        Person person = new Person();
        person.setAge(age);
        person.setName(name);
        personDao.save(person);
    }

    public PersonDto getPerson (Long id) {
        Assert.notNull(id, "id is null");

        Person person = personDao.findById(id);
        return new PersonDto(person);
    }

    public void deletePerson (Long id) {
        Assert.notNull(id, "id is null");

        Person person = personDao.findById(id);
        Assert.notNull(person, "person is null");

        personDao.delete(person);
    }
}
