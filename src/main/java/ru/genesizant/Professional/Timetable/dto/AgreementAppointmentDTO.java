package ru.genesizant.Professional.Timetable.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AgreementAppointmentDTO {

    private Long idAppointment;
    private String fullName;
    private LocalDate dateAppointment;
    private LocalTime timeAppointment;

}
