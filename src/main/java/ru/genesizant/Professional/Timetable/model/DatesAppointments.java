package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Comment("Таблица открытых дат для записей у специалиста")
@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "datesappointments")
public class DatesAppointments {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialist_id", referencedColumnName = "id")
    private Person specialistDateAppointments;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "schedule_time")
    private String scheduleTime;


    public DatesAppointments(LocalDate startDateObject, Person personSpecialist, String availableRecordingTime) {
        visitDate = startDateObject;
        specialistDateAppointments = personSpecialist;
        scheduleTime = availableRecordingTime;
    }
}
