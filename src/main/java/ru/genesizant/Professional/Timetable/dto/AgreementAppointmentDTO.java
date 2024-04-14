package ru.genesizant.Professional.Timetable.dto;

import java.time.LocalDateTime;

public class AgreementAppointmentDTO {

    private Long idAppointment;
    private String fullName;
    private LocalDateTime dateAppointment;

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

    public LocalDateTime getDateAppointment() {
        return dateAppointment;
    }

    public void setDateAppointment(LocalDateTime dateAppointment) {
        this.dateAppointment = dateAppointment;
    }

//    @Override
//    public String toString() {
//        return "Неоплаченная консультация на " + dateAppointment.toLocalDate() + " в " + dateAppointment.toLocalTime() +
//                " от " + fullName;
//    }
}
