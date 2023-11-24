package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "StatisticalAnalysis")
public class StatisticalAnalysis {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialist_id", referencedColumnName = "id")
    private Person specialist_statistical;

    @ManyToOne
    @JoinColumn(name = "appointments_id", referencedColumnName = "id")
    private SpecialistAppointments specialistAppointments;

    @Min(value = 0)
    @Column(name = "duration_appointment")     //ToDo https://qaa-engineer.ru/kak-ispolzovat-tip-interval-v-postgresql/
    private Duration durationAppointment;

    public StatisticalAnalysis() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getSpecialist_statistical() {
        return specialist_statistical;
    }

    public void setSpecialist_statistical(Person specialist_statistical) {
        this.specialist_statistical = specialist_statistical;
    }

    public SpecialistAppointments getSpecialistAppointments() {
        return specialistAppointments;
    }

    public void setSpecialistAppointments(SpecialistAppointments specialistAppointments) {
        this.specialistAppointments = specialistAppointments;
    }

    public Duration getDurationAppointment() {
        return durationAppointment;
    }

    public void setDurationAppointment(Duration durationAppointment) {
        this.durationAppointment = durationAppointment;
    }
}
