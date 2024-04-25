package ru.genesizant.Professional.Timetable.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.services.PersonService;

@Component
public class PersonValidator implements Validator {

    private final PersonService personService;
//    private final PersonDetailsService personDetailsService;

//    @Autowired
//    public PersonValidator(PeopleService peopleService, PersonDetailsService personDetailsService) {
//        this.peopleService = peopleService;
//        this.personDetailsService = personDetailsService;
//    }

    @Autowired
    public PersonValidator(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target;
        if (!person.getPhoneNumber().matches("^\\+\\d{1,3}\\d{10}$")) {
            errors.rejectValue("phoneNumber", "", "Некорректный номер телефона");
        }
        if (person.getPassword() == null) {
            errors.rejectValue("password", "", "Обязательно должен быть пароль!"); //ToDo установить правило пароля, не менее 4 символов например
        }
        if (personService.loadUserByEmail(person.getEmail()).isPresent()) {
            errors.rejectValue("email", "", "Пользователь с таким email уже существует");
        }
    }
}


//        Optional<Person> person1 = peopleService.loadUserByUsername(person.getUsername());
//        String username = person1.get().getUsername();
//        System.out.println(username);
