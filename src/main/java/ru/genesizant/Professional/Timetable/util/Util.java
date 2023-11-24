package ru.genesizant.Professional.Timetable.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.security.JWTUtil;

import java.util.Optional;

@Component
public class Util {

//    private static JWTUtil jwtUtil;
//
//    public Util(JWTUtil jwtUtil) {
//        this.jwtUtil = jwtUtil;
//    }

//    public static boolean isValidJWTAndSession(Model model, HttpServletRequest request) {
//        HttpSession session = request.getSession(false); // Получение текущей сессии, если сессия не существует, вернет null
//
//        if (session != null) {
//            String jwtToken = (String) session.getAttribute("jwtToken");
//            if (jwtToken != null) {
//                try {
//                    String email = jwtUtil.validateTokenAndRetrieveClaim(jwtToken);
//                    Optional<Person> visitor = personRepository.findByEmail(email);
//                    model.addAttribute("name", visitor.get().getUsername());
//
//                } catch (Exception e) {
//                    model.addAttribute("error", "Упс! Пора перелогиниться!");
//                    return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
//                }
//            }
//        }
//    }
}
