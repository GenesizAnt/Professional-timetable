package ru.genesizant.Professional.Timetable.services;

import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.PersonRepository;

import java.util.Optional;

@Service
public class PeopleService {

    private final PersonRepository personRepository;

    public PeopleService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Optional<Person> loadUserByEmail(String email)  {
        return personRepository.findByEmail(email);
    }
}
