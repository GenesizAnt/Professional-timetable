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
    private LocalDate dateVacant;

    @Column(name = "time_vacant")
    private LocalTime timeVacant;

    @ManyToOne
    @JoinColumn(name = "spec_id_reception", referencedColumnName = "id")
    private Person specIdReception;

    @ManyToOne
    @JoinColumn(name = "visitor_id_reception", referencedColumnName = "id")
    private Person visitorIdReception;

    @Column(name = "unregisteredperson")
    private String unregisteredPerson;

    @Comment("Предоплата отмеченная клиентом")
    @Column(name = "prepayment_visitor")
    private boolean prepaymentVisitor;

    @Comment("Предоплата отмеченная специалистом")
    @Column(name = "prepayment")
    private boolean prepayment;

    @Comment("Подтверждено специалистом")
    @Column(name = "confirmed_specialist")
    private boolean confirmedSpecialist;

    @Comment("Подтверждено клиентом")
    @Column(name = "confirmed_visitor")
    private boolean confirmedVisitor;

    @Comment("Было ли уведомление за 24 часа")
    @Column(name = "notify_24_hours")
    private boolean notify24hours;

    @Comment("Было ли уведомление за 3 часа")
    @Column(name = "notify_3_hours")
    private boolean notify3hours;

    @Transient
    private String formattedDate;

    public void setFormattedDate(String format) {
        this.formattedDate = format;
    }

    public String getDateAndTimeReception() {
        return String.format("%s в %s", this.dateVacant, this.timeVacant);
    }
}
