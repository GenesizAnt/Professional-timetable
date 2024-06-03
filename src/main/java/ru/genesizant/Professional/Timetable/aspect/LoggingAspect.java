package ru.genesizant.Professional.Timetable.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.genesizant.Professional.Timetable.services.DatesAppointmentsService;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.ModelAttribute)")
    public void anyControllerLogging() {

    }

    @Before("anyControllerLogging()")
    public void beforeAnyController() {
        log.info("======BEFORE======");
    }


//    Установить для конкретного класса
//    @Pointcut("within(ru.genesizant.Professional.Timetable.controllers.TestController)")

    //    для всех определнных бинов
//    @Pointcut("bean(*Service)")

    //    Установить для всех классов в пакете
//    @Pointcut("within(ru.genesizant.Professional.Timetable.controllers.*)")

    //к методам, которые возвращают Стринг, называются ХХХ, и принимают аргумент Стринг
//    @Pointcut("execution(String getGreed(String)")

    //все методы которые принимают определенную переменную, напрм класс DatesAppointmentsService
//    @Pointcut("args(ru.genesizant.Professional.Timetable.services.DatesAppointmentsService)")

    // также можно получить доступ к аргументу
//    @Pointcut("args(req)")
//    public void any(DatesAppointmentsService req) {
//
//    }
//    @Before("any(req)")
//    public void beforeAnyController(DatesAppointmentsService req) {
//        log.info("======BEFORE====== {}", req);
//    }
}
