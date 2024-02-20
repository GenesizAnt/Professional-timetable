package ru.genesizant.Professional.Timetable.enums;

public enum DayOfWeekRus {

    SUNDAY ("Воскресенье"),
    MONDAY ("Понедельник"),
    TUESDAY ("Вторник"),
    WEDNESDAY ("Среда"),
    THURSDAY ("Четверг"),
    FRIDAY ("Пятница"),
    SATURDAY ("Суббота");

    private final String titleRus;

    DayOfWeekRus(String titleRus) {
        this.titleRus = titleRus;
    }

    public String getTitleRus() {
        return titleRus;
    }

    public static String getRusDayWeek(String engDayWeek) {
        return switch (engDayWeek.toUpperCase()) {
            case "SUNDAY" -> "Воскресенье";
            case "MONDAY" -> "Понедельник";
            case "TUESDAY" -> "Вторник";
            case "WEDNESDAY" -> "Среда";
            case "THURSDAY" -> "Четверг";
            case "FRIDAY" -> "Пятница";
            case "SATURDAY" -> "Суббота";
            default -> "Неверное название дня недели";
        };
    }

}
