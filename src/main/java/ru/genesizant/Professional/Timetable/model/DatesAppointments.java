package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;

@Comment("Таблица открытых дат для записей у специалиста")
@Entity
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

    public DatesAppointments() {

    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getSpecialistDateAppointments() {
        return specialistDateAppointments;
    }

    public void setSpecialistDateAppointments(Person specialist_date_appointments) {
        this.specialistDateAppointments = specialist_date_appointments;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }
}
