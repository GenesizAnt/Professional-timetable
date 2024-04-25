package ru.genesizant.Professional.Timetable.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.genesizant.Professional.Timetable.model.Person;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByEmail(String email);

    List<Person> findByRole(String role);

    // Найти пользователя по ФИО
    Optional<Person> findByUsernameAndSurnameAndPatronymic(String username, String surname, String patronymic);
}