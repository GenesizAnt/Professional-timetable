package ru.genesizant.Professional.Timetable.controllers.visitors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/visitors")
public class VisitorsController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;

    @Autowired
    public VisitorsController(JWTUtil jwtUtil, PersonService personService) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
    }

    @GetMapping("/start_menu_visitor") //ToDo добавить в конфиг - доступ только для авторизированных пользователей
    public String getStartMenu(Model model, HttpServletRequest request) {

        //ToDo добавить навигационный бар https://getbootstrap.com/docs/5.0/components/navbar/

        if (jwtUtil.isValidJWTAndSession(request)) {

//            нужно добавить в html страницу отображение карточки, при этом количество карточек может меняться <div class="card" style="width: 18rem;">
//  <img src="..." class="card-img-top" alt="...">
//  <div class="card-body">
//    <h5 class="card-title">Card title</h5>
//    <p class="card-text">Some quick example text to build on the card title and make up the bulk of the card's content.</p>
//                    <a href="#" class="btn btn-primary">Go somewhere</a>
//  </div>
//</div> как сделать такую html страницу с разным количеством отображения карточек




//            String jwtToken = (String) request.getSession().getAttribute("jwtToken");
//            String email = jwtUtil.validateTokenAndRetrieveClaim(jwtToken);
//            Optional<Person> visitor = personRepository.findByEmail(email);
//            model.addAttribute("name", visitor.get().getUsername());

            List<Person> specialists = personService.getPersonByRoleList("ROLE_ADMIN");
            model.addAttribute("name", request.getSession().getAttribute("name"));
            model.addAttribute("specialists", specialists);

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
        }

        return "visitors/start_menu_visitor";

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
//        return "visitors/start_menu_visitor";
    }

    @GetMapping("/specialist_choose/{id}") //ToDo добавить в конфиг - доступ только для авторизированных пользователей
    public String getSpecialistMenu(Model model, HttpServletRequest request, @PathVariable String id) {

        if (jwtUtil.isValidJWTAndSession(request)) {

            Optional<Person> specialist = personService.findById(Long.valueOf(id));


//            List<Person> specialists = personService.getPersonByRoleList("ROLE_ADMIN");
//            model.addAttribute("name", request.getSession().getAttribute("name"));
            model.addAttribute("specialist", specialist.get().getUsername());

        } else {
            model.addAttribute("error", "Упс! Пора перелогиниться!");
            return "redirect:/auth/login?error"; //ToDo добавить считывание ошибки и правильного отображения сейчас отображается "Неправильные имя или пароль"
        }

        return "visitors/specialist_choose";
    }
}
