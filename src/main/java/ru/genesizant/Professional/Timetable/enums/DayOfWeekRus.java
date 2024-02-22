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
            case "SUNDAY" -> SUNDAY.getTitleRus();
            case "MONDAY" -> MONDAY.getTitleRus();
            case "TUESDAY" -> TUESDAY.getTitleRus();
            case "WEDNESDAY" -> WEDNESDAY.getTitleRus();
            case "THURSDAY" -> THURSDAY.getTitleRus();
            case "FRIDAY" -> FRIDAY.getTitleRus();
            case "SATURDAY" -> SATURDAY.getTitleRus();
            default -> "Неверное название дня недели";
        };
    }

}
