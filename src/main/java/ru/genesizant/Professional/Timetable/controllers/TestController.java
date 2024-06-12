package ru.genesizant.Professional.Timetable.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.genesizant.Professional.Timetable.dto.PersonFullName;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.config.security.PersonDetails;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;
import ru.genesizant.Professional.Timetable.services.PersonService;

import java.util.List;

@Controller
public class TestController {

    private final JWTUtil jwtUtil;
    private final PersonService personService;
    private final DatesAppointmentsService datesAppointmentsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TestController(JWTUtil jwtUtil, PersonService personService, DatesAppointmentsService datesAppointmentsService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.personService = personService;
        this.datesAppointmentsService = datesAppointmentsService;
        this.objectMapper = objectMapper;
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
        System.out.println(personDetails.person());
        return "admin";
    }

    @GetMapping("/super")
    public String showUserInfo(Model model, HttpServletRequest request) {

        if (jwtUtil.isValidJWTInRun(request)) {

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

        if (jwtUtil.isValidJWTInRun(request)) {

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

    @GetMapping("/calendar")
    public String calendar(Model model) {
//        String[][] te = new String[][]{     {"ГлавЗаголовок 2"},
//                                            {"Заголовок 1", "Заголовок 2", "Заголовок 3"},
//                                            {"Данные 1", "Данные 2", "Данные 3"},
//                                            {"Данные 4", "Данные 5", "Данные 6"}};
//
//        ObjectMapper mapper = new ObjectMapper();
//        String json = null;
//        try {
//            json = mapper.writeValueAsString(te);
//            System.out.println(json);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
////        return ResponseEntity.ok(json);
//
//        model.addAttribute("te", json);
        return "test_calendar";
    }

    @GetMapping("/development")
    public String developmentInfo() {
        return "development_page";
    }

//    @GetMapping("/img/{imageName}")
//    @ResponseBody
//    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) throws IOException {
//        ClassPathResource imgFile = new ClassPathResource("static/img/" + imageName);
//        byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.IMAGE_JPEG);
//        headers.setContentLength(imgFile.contentLength());
//        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
//    }
}
