package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalTime;

@Comment("Сводное дата-время для приема")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "vacant_seat")
public class VacantSeat {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_week")
    private String dayOfWeek;

    @Column(name = "date_vacant")
    private LocalDate dateVacant;

    @Column(name = "time_vacant")
    private LocalTime timeVacant;

    @Column(name = "fullname_visitor")
    private String fullname;

    @Column(name = "status_reg_visitor")
    private String statusRegistration;

    @Column(name = "id_visitor")
    private Long idVisitor;

    @ManyToOne
    @JoinColumn(name = "spec_id", referencedColumnName = "id")
    private Person specId;
}
