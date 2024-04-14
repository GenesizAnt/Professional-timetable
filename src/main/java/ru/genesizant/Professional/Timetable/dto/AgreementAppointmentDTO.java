package ru.genesizant.Professional.Timetable.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AgreementAppointmentDTO {

    private Long idAppointment;
    private String fullName;
    private LocalDate dateAppointment;
    private LocalTime timeAppointment;

    public Long getIdAppointment() {
        return idAppointment;
    }

    public void setIdAppointment(Long idAppointment) {
        this.idAppointment = idAppointment;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateAppointment() {
        return dateAppointment;
    }

    public void setDateAppointment(LocalDate dateAppointment) {
        this.dateAppointment = dateAppointment;
    }

    public LocalTime getTimeAppointment() {
        return timeAppointment;
    }

    public void setTimeAppointment(LocalTime timeAppointment) {
        this.timeAppointment = timeAppointment;
    }
}
