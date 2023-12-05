package ru.genesizant.Professional.Timetable.controllers.specialist.calendar;

public enum StatusAdmissionTime {
    AVAILABLE("Доступно"), RESERVED("Забронировано"), IN_QUESTION("Под вопросом");

    private final String status;

    StatusAdmissionTime(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
