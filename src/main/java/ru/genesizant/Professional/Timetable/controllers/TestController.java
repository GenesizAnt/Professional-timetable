package ru.genesizant.Professional.Timetable.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.model.UnregisteredPerson;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.security.PersonDetails;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class TestController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;

    @Autowired
    public TestController(JWTUtil jwtUtil, PersonService personService) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
    }

    @GetMapping("/hello")
    public String sayHello(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession(false); // Получение текущей сессии, если сессия не существует, вернет null
        if (session != null) {
            String jwtToken = (String) session.getAttribute("jwtToken");
            if (jwtToken != null) {
                try {
                    String email = jwtUtil.validateTokenAndRetrieveClaim(jwtToken);
                    model.addAttribute("email", email);
                } catch (Exception e) {
                    model.addAttribute("error", "Упс! Пора перелогиниться!");
                    return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
                }
            }
        }
        return "hello";
    }

    @GetMapping("/admin")
    public String adminPage() {
        //проверка доступа человека

//        HttpSession session = request.getSession(false);
//        if (session != null) {
//            String jwtToken = (String) session.getAttribute("jwtToken");
//            if (jwtToken != null) {
//                try {
//                    String email = jwtUtil.validateTokenAndRetrieveClaim(jwtToken);
//                    model.addAttribute("email", email);
//                } catch (Exception e) {
//                    model.addAttribute("error", "Упс! Пора перелогиниться!");
//                    return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
////                    model.addAttribute("errorMessage", "Упс! Пора перелогиниться!"); //ToDo добавить кнопку на страницу логин
////                    return "error";
//                }
//            }
//        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        System.out.println(personDetails.getPerson());
        return "admin";
    }

    @GetMapping("/super")
    public String showUserInfo(Model model, HttpServletRequest request) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            List<PersonFullName> users = personService.findAllPersonFullName();

            model.addAttribute("users", users);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }

        return "super";
    }

    @PostMapping("/superChoose")
    public String superChoose(Model model, HttpServletRequest request,
                               @RequestParam("clientId") String clientId,
                               @RequestParam("role") String selectedRole) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            List<PersonFullName> users = personService.findAllPersonFullName();

            model.addAttribute("users", users);

            personService.setNewRoleForUser(Long.valueOf(clientId), selectedRole);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error";
        }

        return "super";
    }

    @GetMapping("/all")
    public String allInfo() {
        return "all";
    }

    @GetMapping("/development")
    public String developmentInfo() {
        return "development_page";
    }


}
