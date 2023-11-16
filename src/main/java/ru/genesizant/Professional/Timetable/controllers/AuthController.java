package ru.genesizant.Professional.Timetable.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonDTO;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.RegistrationService;
import ru.genesizant.Professional.Timetable.util.PersonValidator;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final PersonValidator personValidator;
    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthController(PersonValidator personValidator, RegistrationService registrationService, JWTUtil jwtUtil, ModelMapper modelMapper) {
        this.personValidator = personValidator;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("person") Person person) {
        return "auth/registration";
    }

    //работает без JWT
//    @PostMapping("/registration")
//    public String performRegistration(@ModelAttribute("person") @Valid PersonDTO personDTO, BindingResult bindingResult) {
//        Person person = concertPerson(personDTO);
//        personValidator.validate(person, bindingResult);
//        if (bindingResult.hasErrors()) {
//            return "/auth/registration"; //ToDo сделать прозрачный текст подсказку как вводить номер телефона
//        }
//        registrationService.register(person);
//        return "redirect:/auth/login";
//    }

    @PostMapping("/registration")
    public Map<String, String> performRegistration(@RequestBody @Valid PersonDTO personDTO, BindingResult bindingResult) {
        Person person = concertPerson(personDTO);
        personValidator.validate(person, bindingResult);
        if (bindingResult.hasErrors()) {
            return Map.of("message", "Error!");//ToDo lesson 92 - правильно сделать отдельный метод @ExceptionHandler для возвращения кода и ошибки
        }
        registrationService.register(person);

        String token = jwtUtil.generateToken(person.getUsername(), person.getEmail(), person.getPhoneNumber());
        return Map.of("jwt-token", token);

//        claims.put("username", jwt.getClaim("username").asString());
//        claims.put("email", jwt.getClaim("email").asString());
//        claims.put("phoneNumber", jwt.getClaim("phoneNumber").asString());
    }

    private Person concertPerson(PersonDTO personDTO) {
        Person person = this.modelMapper.map(personDTO, Person.class);
        person.setNumberVisits(0);
        person.setTotalAmount(BigDecimal.ZERO);
        person.setRole("ROLE_USER"); //ToDo это должно быть не здесь?? временная заглушка
        return person;
    }


}
