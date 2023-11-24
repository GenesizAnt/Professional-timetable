package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "DatesAppointments")
public class DatesAppointments {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialist_id", referencedColumnName = "id")
    private Person specialist_date_appointments;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    public DatesAppointments() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getSpecialist_date_appointments() {
        return specialist_date_appointments;
    }

    public void setSpecialist_date_appointments(Person specialist_date_appointments) {
        this.specialist_date_appointments = specialist_date_appointments;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }
}