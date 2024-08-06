package ru.genesizant.Professional.Timetable.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static ru.genesizant.Professional.Timetable.enums.DayOfWeekRus.getRusDayWeekShort;

@Getter
@Setter
public class AgreementAppointmentDTO {

    private Long idAppointment;
    private String fullName;
    private String dateAppointment;
    private LocalTime timeAppointment;


    public void setDateAppointment(LocalDate dateq) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        this.dateAppointment = dateq.format(formatter);
        this.dateAppointment = getRusDayWeekShort(dateq.getDayOfWeek().name()) + " " + this.dateAppointment;
    }

}
