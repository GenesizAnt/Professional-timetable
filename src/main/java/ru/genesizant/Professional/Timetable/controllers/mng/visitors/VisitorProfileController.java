package ru.genesizant.Professional.Timetable.controllers.mng.visitors;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.genesizant.Professional.Timetable.config.security.JWTUtil;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegram;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegramService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/profile")
public class VisitorProfileController {

    @Value("${error_login}")
    private String ERROR_LOGIN;
    private final JWTUtil jwtUtil;
    private final String PROFILE_VIEW_REDIRECT = "redirect:/profile/my_profile";

    private final UserTelegramService userTelegramService;

    // Отображение страницы профиля клиента
    @GetMapping("/my_profile")
    public String listDebtors(Model model, HttpServletRequest request) {
        if (jwtUtil.isValidJWTAndSession(request)) {
            displayPage(model, request);
            log.info("Клиент: " + request.getSession().getAttribute("id") + ". Перешел на страницу профиля клиента");
        } else {
            return ERROR_LOGIN;
        }
        return "visitors/visitorprofile";
    }

    // Подтвердить аккаунт в ТГ боте
    @GetMapping("/agreeTG")
    public String agreeTG(Model model, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        Optional<UserTelegram> userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        if (userTelegram.isPresent()) {
            userTelegram.get().setAgree(Boolean.TRUE);
            userTelegramService.save(userTelegram.get());
            log.info("Клиент: " + request.getSession().getAttribute("id") + ". Нажал кнопку подтверждение акк в ТГ");
        } else {
            return encodeError("Не найден аккаунт в ТГ-боте, перейдите в бот и зарегистрируйтесь https://t.me/TimeProfessionalBot");
        }
        return PROFILE_VIEW_REDIRECT;
    }

    // Отметить подтверждение зарегистрированного аккаунта в ТГ боте - т.е. удалить его
    @GetMapping("/cancelTG")
    public String cancelTG(Model model, HttpServletRequest request) {
        if (!jwtUtil.isValidJWTAndSession(request)) {
            return ERROR_LOGIN;
        }
        userTelegramService.deleteByPersonId((Long) request.getSession().getAttribute("id"));
        log.info("Клиент: " + request.getSession().getAttribute("id") + ". Нажал кнопку отметить подтверждение зарегистрированного аккаунта в ТГ боте ");
        return PROFILE_VIEW_REDIRECT;
    }


    private void displayPage(Model model, HttpServletRequest request) {
        Optional<UserTelegram> userTelegram = userTelegramService.findByPersonId((Long) request.getSession().getAttribute("id"));
        if (userTelegram.isEmpty()) {
            model.addAttribute("notacc", "");
        } else {
            if (userTelegram.get().isAgree()) {
                model.addAttribute("agree", "подтвержден");
            }
            if (!userTelegram.get().isAgree()) {
                model.addAttribute("notagree", "не подтвержден");
                model.addAttribute("username", userTelegram.get().getPersonusername());
            }
        }

        model.addAttribute("name", request.getSession().getAttribute("name"));
    }

    private String encodeError(String error) {
        String ERROR_VALIDATE_FORM = "redirect:/profile/my_profile?error=";
        return ERROR_VALIDATE_FORM + URLEncoder.encode(error, StandardCharsets.UTF_8);
    }
}
