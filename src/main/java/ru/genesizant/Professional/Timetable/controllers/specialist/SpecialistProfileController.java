package ru.genesizant.Professional.Timetable.controllers.specialist;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegram;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegramService;

@Controller
@RequestMapping("/spec_profile")
public class SpecialistProfileController {

    private final JWTUtil jwtUtil;
    private final UserTelegramService userTelegramService;
    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final String PROFILE_SPEC_VIEW_REDIRECT = "redirect:/spec_profile/my_profile";

    public SpecialistProfileController(JWTUtil jwtUtil, UserTelegramService userTelegramService) {
        this.jwtUtil = jwtUtil;
        this.userTelegramService = userTelegramService;
    }

    // Отображение страницы профиля специалиста
    @GetMapping("/my_profile")
    public String listDebtors(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {
            displayPage(model, request);
        } else {
            return ERROR_LOGIN;
        }
        return "specialist/specialistprofile";
    }

    // Подтвердить аккаунт в ТГ боте
    @GetMapping("/agreeTG")
    public String agreeTG(Model model, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        UserTelegram userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        userTelegram.setAgree(Boolean.TRUE);
        userTelegramService.save(userTelegram);
        return PROFILE_SPEC_VIEW_REDIRECT;
    }

    // Отметить подтверждение зарегистрированного аккаунта в ТГ боте - т.е. удалить его
    @GetMapping("/cancelTG")
    public String cancelTG(Model model, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        userTelegramService.deleteByPersonId((Long) request.getSession().getAttribute("id"));
        return PROFILE_SPEC_VIEW_REDIRECT;
    }


    private void displayPage(Model model, HttpServletRequest request) {
        UserTelegram userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        if (userTelegram == null) {
            model.addAttribute("notacc", "");
        } else {
            if (userTelegram.isAgree()) {
                model.addAttribute("agree", "подтвержден");
            }
            if (!userTelegram.isAgree()) {
                model.addAttribute("notagree", "не подтвержден");
                model.addAttribute("username", userTelegram.getPersonusername());
            }
        }
        model.addAttribute("name", request.getSession().getAttribute("name"));
    }
}
