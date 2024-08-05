package ru.genesizant.Professional.Timetable.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class AgreementAppointmentDTO {

    private Long idAppointment;
    private String fullName;
    private String dateAppointment;
    private LocalTime timeAppointment;


    public void setDateAppointment(LocalDate dateq) {
        if (dateq != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            this.dateAppointment = String.valueOf(LocalDate.parse(dateq.toString(), formatter));
        }
    }

}
