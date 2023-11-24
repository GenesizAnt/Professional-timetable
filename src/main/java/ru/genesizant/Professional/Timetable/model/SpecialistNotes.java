package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SpecialistNotes")
public class SpecialistNotes {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialist_id", referencedColumnName = "id")
    private Person specialist_notes;

    @ManyToOne
    @JoinColumn(name = "visitor_id", referencedColumnName = "id")
    private Person visitor_notes;

    @ManyToOne
    @JoinColumn(name = "appointments_id", referencedColumnName = "id")
    private SpecialistAppointments appointmentsNotes;

    @Size(min = 2, max = 2500, message = "Описание должно быть от 2 до 2500 символов")
    @Column(name = "note_description")
    private String noteDescription;

    public SpecialistNotes() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getSpecialist_notes() {
        return specialist_notes;
    }

    public void setSpecialist_notes(Person specialist_notes) {
        this.specialist_notes = specialist_notes;
    }

    public Person getVisitor_notes() {
        return visitor_notes;
    }

    public void setVisitor_notes(Person visitor_notes) {
        this.visitor_notes = visitor_notes;
    }

    public SpecialistAppointments getAppointmentsNotes() {
        return appointmentsNotes;
    }

    public void setAppointmentsNotes(SpecialistAppointments appointmentsNotes) {
        this.appointmentsNotes = appointmentsNotes;
    }

    public String getNoteDescription() {
        return noteDescription;
    }

    public void setNoteDescription(String noteDescription) {
        this.noteDescription = noteDescription;
    }
}
