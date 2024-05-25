package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Comment("Заметки специалиста по проведенным приемам")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "specialistnotes")
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

}
