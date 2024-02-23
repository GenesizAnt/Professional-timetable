package ru.genesizant.Professional.Timetable.enums;

public enum StatusAdmissionTime {
    AVAILABLE("Доступно для записи"),
    RESERVED("Забронировано"),
    IN_QUESTION("Под вопросом"),
    NEED_CONFIRMATION("Требует подтверждения"),
    CONFIRMED("Запись подтверждена");

    private final String status;

    StatusAdmissionTime(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
