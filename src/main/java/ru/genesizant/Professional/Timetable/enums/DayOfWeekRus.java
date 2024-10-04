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

    private final String titleShortRus;

    DayOfWeekRus(String titleRus, String titleShortRus) {
        this.titleRus = titleRus;
        this.titleShortRus = titleShortRus;
    }

    public String getTitleShortRus() {
        return titleShortRus;
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
            case "SUNDAY" -> SUNDAY.getTitleShortRus();
            case "MONDAY" -> MONDAY.getTitleShortRus();
            case "TUESDAY" -> TUESDAY.getTitleShortRus();
            case "WEDNESDAY" -> WEDNESDAY.getTitleShortRus();
            case "THURSDAY" -> THURSDAY.getTitleShortRus();
            case "FRIDAY" -> FRIDAY.getTitleShortRus();
            case "SATURDAY" -> SATURDAY.getTitleShortRus();
            default -> "Неверное название дня недели";
        };
    }
}
