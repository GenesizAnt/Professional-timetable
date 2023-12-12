package ru.genesizant.Professional.Timetable.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.security.PersonDetails;

@Controller
public class TestController {

    private final JWTUtil jwtUtil;

    @Autowired
    public TestController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
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
    public String showUserInfo() {
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
