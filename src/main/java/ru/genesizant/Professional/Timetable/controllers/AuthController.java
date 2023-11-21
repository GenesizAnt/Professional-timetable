package ru.genesizant.Professional.Timetable.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonDTO;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.RegistrationService;
import ru.genesizant.Professional.Timetable.util.PersonValidator;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final PersonValidator personValidator;
    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(PersonValidator personValidator, RegistrationService registrationService, JWTUtil jwtUtil, ModelMapper modelMapper, AuthenticationManager authenticationManager) {
        this.personValidator = personValidator;
        this.registrationService = registrationService;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.authenticationManager = authenticationManager;
    }

    //    работает без JWT
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/process_login")
    @ResponseBody
    public ResponseEntity<String> processLogin(@RequestParam("username") String username, @RequestParam("password") String password) {

        System.out.println(username);

//        // Проверяем логин и пароль пользователя
//        if (userService.authenticate(personDTO.getUsername(), personDTO.getPassword())) {
//            // Если аутентификация прошла успешно, генерируем новый токен
//            String newToken = jwtUtil.generateToken(personDTO.getUsername());
//            // Возвращаем новый токен в качестве ответа на запрос
//            return ResponseEntity.ok(newToken);
//        } else {
//            // Если аутентификация не удалась, возвращаем ошибку
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
        return ResponseEntity.ok("newToken");
    }

//    @PostMapping("/process_login")
//    @ResponseBody
//    public String processLogin(@RequestParam("username") String username,
//                               @RequestParam("password") String password,
//                               HttpServletResponse response, HttpSession session) {
//        // Проверка имени пользователя и пароля
//                UsernamePasswordAuthenticationToken authInputToken =
//                new UsernamePasswordAuthenticationToken(username, password);
//
//        try {
//            authenticationManager.authenticate(authInputToken);
//        } catch (BadCredentialsException e) {
//            return "redirect:/auth/login?error";
//        }
//
//        PersonDTO personDTO = (PersonDTO) authInputToken.getPrincipal();
//
////        String token = "11";
//        String jwtToken = jwtUtil.generateToken(personDTO.getUsername(), personDTO.getEmail(), personDTO.getPhoneNumber());
//        session.setAttribute("jwtToken", jwtToken);
//
////        response.addHeader("Authorization", "Bearer " + token); // добавить новый токен в заголовок ответа
//        return "redirect:/hello"; // перенаправить пользователя на защищенную страницу
//    }

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("person") Person person) {
        return "auth/registration";
    }

    //    работает без JWT
    @PostMapping("/registration")
    public String performRegistration(@ModelAttribute("person") @Valid PersonDTO personDTO, BindingResult bindingResult,
                                      HttpServletResponse response, HttpSession session) { //@RequestParam("jwtToken") String jwtToken
        Person person = concertPerson(personDTO);
        personValidator.validate(person, bindingResult);

        if (bindingResult.hasErrors()) {
            return "/auth/registration"; //ToDo сделать прозрачный текст подсказку как вводить номер телефона
        }

        String jwtToken = jwtUtil.generateToken(person.getEmail());
//        response.addHeader("Authorization", "Bearer " + jwtToken); // добавить новый токен в заголовок ответа
        System.out.println(jwtToken); // //ToDo временный код для проверки
        session.setAttribute("jwtToken", jwtToken);

        registrationService.register(person, jwtToken);
        return "redirect:/auth/login";
    }

    //    работает c JWT
//    @PostMapping("/registration")
//    @ResponseBody
//    public Map<String, String> performRegistration(@RequestBody @Valid PersonDTO personDTO, BindingResult bindingResult) {
//        Person person = concertPerson(personDTO);
//        personValidator.validate(person, bindingResult);
//        if (bindingResult.hasErrors()) {
//            return Map.of("message", "Error!");//ToDo lesson 92 - правильно сделать отдельный метод @ExceptionHandler для возвращения кода и ошибки
//        }
//        registrationService.register(person);
//
//        String token = jwtUtil.generateToken(person.getUsername(), person.getEmail(), person.getPhoneNumber());
//        return Map.of("jwt-token", token);
//    }
//
//    @PostMapping("/login")
//    @ResponseBody
//    public Map<String, String> performLogin(@RequestBody PersonDTO personDTO) { //@AuthenticationPrincipal UserDetails userDetails
//        UsernamePasswordAuthenticationToken authInputToken =
//                new UsernamePasswordAuthenticationToken(personDTO.getEmail(), personDTO.getPassword());
//
//        try {
//            authenticationManager.authenticate(authInputToken);
//        } catch (BadCredentialsException e) {
//            return Map.of("message", "Incorrect credentials!"); //ToDo как создать свою ошибку 1:03:00 https://youtu.be/NIv9TFTSIlg?t=3933
//        }
//
//        String token = jwtUtil.generateToken(personDTO.getUsername(), personDTO.getEmail(), personDTO.getPhoneNumber());
//        return Map.of("jwt-token", token);
//    }

    private Person concertPerson(PersonDTO personDTO) {
        Person person = this.modelMapper.map(personDTO, Person.class);
        person.setNumberVisits(0);
        person.setTotalAmount(BigDecimal.ZERO);
        person.setRole("ROLE_USER"); //ToDo это должно быть не здесь?? временная заглушка
        return person;
    }


}
