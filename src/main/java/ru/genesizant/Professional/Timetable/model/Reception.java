package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalTime;

@Comment("Прием у специалиста")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "reception")
public class Reception {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_week")
    private String dayOfWeek;

    @Column(name = "date_vacant")
    private LocalDate date_vacant;

    @Column(name = "time_vacant")
    private LocalTime time_vacant;

    @ManyToOne
    @JoinColumn(name = "specialist_id", referencedColumnName = "id")
    private Person specIdReception;

    @ManyToOne
    @JoinColumn(name = "visitor_id", referencedColumnName = "id")
    private Person visitorIdReception;

    @Comment("Предоплата отмеченная клиентом")
    @Column(name = "prepayment_visitor")
    private boolean prepaymentVisitor;

    @Comment("Предоплата отмеченная специалистом")
    @Column(name = "prepayment")
    private boolean prepayment;

    @Comment("Было ли уведомление за 24 часа")
    @Column(name = "notify24hours")
    private boolean notify24hours;

    @Comment("Было ли уведомление за 3 часа")
    @Column(name = "notify3hours")
    private boolean notify3hours;
}
