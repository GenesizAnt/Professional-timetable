package ru.genesizant.Professional.Timetable.enums;

public enum DayOfWeekRus {

    SUNDAY ("Воскресенье", "Вск"),
    MONDAY ("Понедельник", "Пн"),
    TUESDAY ("Вторник", "Вт"),
    WEDNESDAY ("Среда", "Ср"),
    THURSDAY ("Четверг", "Чт"),
    FRIDAY ("Пятница", "Пт"),
    SATURDAY ("Суббота", "Сб");

    private final String titleRus;

    public String getTitleRusShort() {
        return titleRusShort;
    }

    private final String titleRusShort;

    DayOfWeekRus(String titleRus, String titleRusShort) {
        this.titleRus = titleRus;
        this.titleRusShort = titleRusShort;
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

    public static String getRusDayWeekShort(String engDayWeek) {
        return switch (engDayWeek.toUpperCase()) {
            case "SUNDAY" -> SUNDAY.getTitleRusShort();
            case "MONDAY" -> MONDAY.getTitleRusShort();
            case "TUESDAY" -> TUESDAY.getTitleRusShort();
            case "WEDNESDAY" -> WEDNESDAY.getTitleRusShort();
            case "THURSDAY" -> THURSDAY.getTitleRusShort();
            case "FRIDAY" -> FRIDAY.getTitleRusShort();
            case "SATURDAY" -> SATURDAY.getTitleRusShort();
            default -> "Неверное название дня недели";
        };
    }

}
