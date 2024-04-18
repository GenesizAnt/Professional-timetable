package ru.genesizant.Professional.Timetable.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonDTO;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.config.security.PersonDetails;
import ru.genesizant.Professional.Timetable.services.PersonService;
import ru.genesizant.Professional.Timetable.services.RegistrationService;
import ru.genesizant.Professional.Timetable.util.PersonValidator;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final PersonValidator personValidator;
    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final PersonService personService;

    @Autowired
    public AuthController(PersonValidator personValidator, RegistrationService registrationService, JWTUtil jwtUtil, ModelMapper modelMapper, PersonService personService) {
        this.personValidator = personValidator;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.personService = personService;
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
        System.out.println(jwtToken); //ToDo временный код для проверки
        session.setAttribute("jwtToken", jwtToken); // ToDo добавить в форму регистрации зарегистрироваться как специалист

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
            personService.updateJwtToken(email, newJwtToken);

            session.setAttribute("jwtToken", newJwtToken);
            session.setAttribute("name", personDetails.getUsername());
            session.setAttribute("email", personDetails.getEmail());
            session.setAttribute("id", personDetails.getId());

        } catch (Exception e) {
            return "redirect:/auth/login?error";
        }

        switch (personDetails.getRole()) {
            case "ROLE_USER" -> {
                return "redirect:/visitors/my_specialist_menu";
            }
            case "ROLE_ADMIN" -> {
                return "redirect:/specialist/start_menu_specialist";
            }
            case "ROLE_SUPER" -> {
                return "redirect:/super"; //ToDo сделать страницу для суперпользователя
            }
            default -> {
                return "redirect:/auth/login";
            }
        }

//        return "redirect:/visitors/start_menu_visitor";
    }

    //ToDo lesson 92 - правильно сделать отдельный метод @ExceptionHandler для возвращения кода и ошибки
// return Map.of("message", "Incorrect credentials!"); //ToDo как создать свою ошибку 1:03:00 https://youtu.be/NIv9TFTSIlg?t=3933


    private Person concertPerson(PersonDTO personDTO) {
        Person person = this.modelMapper.map(personDTO, Person.class);
        person.setRole("ROLE_USER"); //ToDo это должно быть не здесь?? временная заглушка
        return person;
    }
}
