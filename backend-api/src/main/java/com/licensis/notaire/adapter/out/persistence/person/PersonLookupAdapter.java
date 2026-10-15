package com.licensis.notaire.adapter.out.persistence.person;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.person.PersonLookupPort;
import com.licensis.notaire.repository.PersonRepository;

@Component
public class PersonLookupAdapter implements PersonLookupPort {

    private final PersonRepository people;

    public PersonLookupAdapter(PersonRepository people) {
        this.people = people;
    }

    @Override
    public boolean existsById(Integer id) {
        return id != null && people.existsById(id);
    }
}
