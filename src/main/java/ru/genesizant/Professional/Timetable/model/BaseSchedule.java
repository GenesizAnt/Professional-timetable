package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.time.LocalTime;

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
}
