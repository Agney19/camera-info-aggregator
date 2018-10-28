package com.agney.agneyweb.dao;

import com.agney.agneyweb.model.Person;
import org.springframework.data.repository.Repository;
import org.springframework.lang.Nullable;

public interface PersonDao extends Repository<Person, Long> {
    Person save(Person person);
    void delete(Person person);
    @Nullable
    Person findById(long id);
}
