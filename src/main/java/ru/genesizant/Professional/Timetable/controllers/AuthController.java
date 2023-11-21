package ru.genesizant.Professional.Timetable.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonDTO;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.PeopleRepository;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.security.PersonDetails;
import ru.genesizant.Professional.Timetable.services.PeopleService;
import ru.genesizant.Professional.Timetable.services.RegistrationService;
import ru.genesizant.Professional.Timetable.util.PersonValidator;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final PersonValidator personValidator;
    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final PeopleRepository peopleRepository;

    @Autowired
    public AuthController(PersonValidator personValidator, RegistrationService registrationService, JWTUtil jwtUtil, ModelMapper modelMapper, PeopleRepository peopleRepository) {
        this.personValidator = personValidator;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.peopleRepository = peopleRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("person") Person person) {
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String performRegistration(@ModelAttribute("person") @Valid PersonDTO personDTO,
                                      BindingResult bindingResult, HttpSession session) {

        Person person = concertPerson(personDTO);
        personValidator.validate(person, bindingResult);
        if (bindingResult.hasErrors()) {
            return "/auth/registration"; //ToDo сделать прозрачный текст подсказку как вводить номер телефона
        }

        String jwtToken = jwtUtil.generateToken(person.getEmail());
        System.out.println(jwtToken); // //ToDo временный код для проверки
        session.setAttribute("jwtToken", jwtToken);

        registrationService.register(person, jwtToken);
        return "redirect:/auth/login";
    }

    @GetMapping("/check_jwt")
    public String sayHello(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        String jwtToken = personDetails.getJwtToken();

        try {
            DecodedJWT decodedJWT = JWT.decode(jwtToken); // декодирование токена
            String email = decodedJWT.getClaim("email").asString();

            String newJwtToken = jwtUtil.generateToken(email);
            updateJwtToken(email, newJwtToken);

            session.setAttribute("jwtToken", newJwtToken);

        } catch (Exception e) {
            return "redirect:/auth/login?error";
        }
        return "redirect:/hello";
    }

    //ToDo lesson 92 - правильно сделать отдельный метод @ExceptionHandler для возвращения кода и ошибки
// return Map.of("message", "Incorrect credentials!"); //ToDo как создать свою ошибку 1:03:00 https://youtu.be/NIv9TFTSIlg?t=3933
    private void updateJwtToken(String email, String newJwtToken) {
        Optional<Person> person = peopleRepository.findByEmail(email);
        person.get().setJwtToken(newJwtToken);
        peopleRepository.save(person.get());
    }


    private Person concertPerson(PersonDTO personDTO) {
        Person person = this.modelMapper.map(personDTO, Person.class);
        person.setNumberVisits(0);
        person.setTotalAmount(BigDecimal.ZERO);
        person.setRole("ROLE_USER"); //ToDo это должно быть не здесь?? временная заглушка
        return person;
    }
}
