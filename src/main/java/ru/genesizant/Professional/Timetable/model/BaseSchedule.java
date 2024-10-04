package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import ru.genesizant.Professional.Timetable.enums.DayOfWeekRus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Comment("Таблица для шаблона расписания рабочего времени специалиста")
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "base_schedule")
public class BaseSchedule {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "idspecialist", referencedColumnName = "id")
    private Person specialistBaseSchedule;
    @Column(name = "starttime")
    private LocalTime startTime;
    @Column(name = "endtime")
    private LocalTime endTime;
    @Column(name = "mininterval")
    private LocalTime minInterval;
    @Column(name = "countdays")
    private int countDays;
    @Column(name = "monday")
    private boolean monday;
    @Column(name = "tuesday")
    private boolean tuesday;
    @Column(name = "wednesday")
    private boolean wednesday;
    @Column(name = "thursday")
    private boolean thursday;
    @Column(name = "friday")
    private boolean friday;
    @Column(name = "saturday")
    private boolean saturday;
    @Column(name = "sunday")
    private boolean sunday;

    public List<String> getWeekDays() {
        List<String> weekDays = new ArrayList<>();
        if (monday) weekDays.add(DayOfWeekRus.MONDAY.getTitleRus());
        if (tuesday) weekDays.add(DayOfWeekRus.TUESDAY.getTitleRus());
        if (wednesday) weekDays.add(DayOfWeekRus.WEDNESDAY.getTitleRus());
        if (thursday) weekDays.add(DayOfWeekRus.THURSDAY.getTitleRus());
        if (friday) weekDays.add(DayOfWeekRus.FRIDAY.getTitleRus());
        if (saturday) weekDays.add(DayOfWeekRus.SATURDAY.getTitleRus());
        if (sunday) weekDays.add(DayOfWeekRus.SUNDAY.getTitleRus());
        return weekDays;
    }
}
