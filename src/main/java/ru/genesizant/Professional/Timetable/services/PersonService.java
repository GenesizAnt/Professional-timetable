package ru.genesizant.Professional.Timetable.services;

import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.PersonRepository;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Optional<Person> loadUserByEmail(String email)  {
        return personRepository.findByEmail(email);
    }

    public void updateJwtToken(String email, String newJwtToken) {
        Optional<Person> person = personRepository.findByEmail(email);
        person.get().setJwtToken(newJwtToken);
        personRepository.save(person.get());
    }

    public List<Person> getPersonByRoleList(String role) {
        return personRepository.findByRole(role);
    }
}
