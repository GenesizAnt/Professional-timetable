package ru.genesizant.Professional.Timetable.controllers.visitors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.repositories.PeopleRepository;
import ru.genesizant.Professional.Timetable.security.JWTUtil;

import java.util.Optional;

@Controller
@RequestMapping("/visitors")
public class VisitorsController {

    private final JWTUtil jwtUtil;
    private final PeopleRepository peopleRepository;

    @Autowired
    public VisitorsController(JWTUtil jwtUtil, PeopleRepository peopleRepository) {
        this.jwtUtil = jwtUtil;
        this.peopleRepository = peopleRepository;
    }

    @GetMapping("/start_menu")
    public String getStartMenu(Model model, HttpServletRequest request) {

        HttpSession session = request.getSession(false); // Получение текущей сессии, если сессия не существует, вернет null

        if (session != null) {
            String jwtToken = (String) session.getAttribute("jwtToken");
            if (jwtToken != null) {
                try {
                    String email = jwtUtil.validateTokenAndRetrieveClaim(jwtToken);
                    Optional<Person> visitor = peopleRepository.findByEmail(email);
                    model.addAttribute("name", visitor.get().getUsername());
                    
                } catch (Exception e) {
                    model.addAttribute("error", "Упс! Пора перелогиниться!");
                    return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
                }
            }
        }
        return "visitors/start_menu";
    }
}
