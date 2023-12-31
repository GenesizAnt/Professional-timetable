package ru.genesizant.Professional.Timetable.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.UnregisteredPerson;
import ru.genesizant.Professional.Timetable.repositories.UnregisteredPersonRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UnregisteredPersonService {

    private final UnregisteredPersonRepository unregisteredPersonRepository;

    @Autowired
    public UnregisteredPersonService(UnregisteredPersonRepository unregisteredPersonRepository) {
        this.unregisteredPersonRepository = unregisteredPersonRepository;
    }

    public List<UnregisteredPerson> getUnregisteredPersonBySpecialistList(Long id) {
        return unregisteredPersonRepository.findAllBySpecialistUnregisteredPersonIdOrderById(id);
    }

    public Optional<UnregisteredPerson> findById(Long id) {
        return unregisteredPersonRepository.findById(id);
    }

    public void addNewUnregisteredPerson(String username, String surname, String patronymic, Person specialist) {
        UnregisteredPerson newUnregisteredPerson = new UnregisteredPerson();
        newUnregisteredPerson.setUsername(username);
        newUnregisteredPerson.setSurname(surname);
        newUnregisteredPerson.setPatronymic(patronymic);
        newUnregisteredPerson.setSpecialistUnregisteredPerson(specialist);
        unregisteredPersonRepository.save(newUnregisteredPerson);
    }
}
