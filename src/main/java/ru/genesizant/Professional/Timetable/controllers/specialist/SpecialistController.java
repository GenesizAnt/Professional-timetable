package ru.genesizant.Professional.Timetable.controllers.specialist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.services.PersonService;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/specialist")
public class SpecialistController {

    private final PersonService personService;

    @ModelAttribute
    public void getPayloadPage(Model model, HttpServletRequest request) {
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }

    @ModelAttribute(name = "specialist")
    public Person getSpecialist(HttpServletRequest request) {
        return personService.findById((Long) request.getSession().getAttribute("id")).orElseThrow();
    }

    //Отображение меню специалиста, что он может делать
    @GetMapping("/start_menu_specialist")
    public String getStartMenu(@ModelAttribute("specialist") Person specialist) {
        return "specialist/start_menu_specialist";
    }
}
