package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.PersonRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public Optional<Person> findByEmail(String email) {
        return personRepository.findByEmail(email);
    }

    public void updateJwtToken(String email, String newJwtToken) {
        Optional<Person> person = personRepository.findByEmail(email);
        if (person.isPresent()) {
            person.get().setJwtToken(newJwtToken);
            personRepository.save(person.get());
        } else {
            throw new UsernameNotFoundException("User not found!");
        }
    }

    public List<Person> getPersonByRoleList(String role) {
        return personRepository.findByRole(role);
    }

    public Optional<Person> findById(Long id) {
        return personRepository.findById(id);
    }

    // Получить ФИО всех пользователей
    public List<PersonFullName> findAllPersonFullName() {
        List<PersonFullName> allUsers = new ArrayList<>();
        List<Person> allPerson = personRepository.findAll();
        for (Person person : allPerson) {
            allUsers.add(
                    modelMapper.map(personRepository.findById(person.getId()), PersonFullName.class)
            );
        }
        return allUsers;
    }

    //Установить роль пользователю
    public void setNewRoleForUser(Long clientId, String selectedRole) {
        Optional<Person> person = personRepository.findById(clientId);
        if (person.isPresent()) {
            Person newRolePerson = person.get();
            newRolePerson.setRole(selectedRole);
            personRepository.save(newRolePerson);
        }
    }

    // найти запись по ФИО
    public Optional<Person> findByFullName(String username, String surname, String patronymic) {
        return personRepository.findByUsernameAndSurnameAndPatronymic(username, surname, patronymic);
    }

    public void setNewPassword(String text, Person person) {
        String newPass = text.substring(text.indexOf("ь") + 1).trim();
        person.setPassword(passwordEncoder.encode(newPass));
        personRepository.save(person);
    }

    public Optional<Person> findSpecialistByPhoneNumber(String specialistPhone) {
        return personRepository.findByPhoneNumberAndRole(specialistPhone, "ROLE_ADMIN");
    }
}
