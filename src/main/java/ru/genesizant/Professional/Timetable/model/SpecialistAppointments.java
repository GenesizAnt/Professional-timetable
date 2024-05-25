package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Comment("Записи приемов специалиста")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "specialistappointments")
public class SpecialistAppointments {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialist_id", referencedColumnName = "id")
    private Person specialistAppointments;

    @ManyToOne
    @JoinColumn(name = "visitor_id", referencedColumnName = "id")
    private Person visitorAppointments;

    @ManyToOne
    @JoinColumn(name = "services_id", referencedColumnName = "id")
    private ProfessionalServices professionalServices;

    @Column(name = "consultation_successful")
    private boolean consultationSuccessful;

    @Comment("Первичное или вторичное посещение")
    @Column(name = "visit_type")
    private String visitType;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "appointmenttime")
    private LocalTime appointmentTime;

    @Comment("Стоимость консультации")
    @Column(name = "consultation_fee")
    private BigDecimal consultationFee;

    @Comment("Предоплата отмеченная специалистом")
    @Column(name = "prepayment")
    private boolean prepayment;

    @Comment("Предоплата отмеченная клиентом")
    @Column(name = "prepaymentvisitor")
    private boolean prepaymentVisitor;

    @Comment("Было ли уведомление за 24 часа")
    @Column(name = "notify24hours")
    private boolean notify24hours;

    @Comment("Было ли уведомление за 3 часа")
    @Column(name = "notify3hours")
    private boolean notify3hours;

    @OneToMany(mappedBy = "specialistAppointments")
    private List<StatisticalAnalysis> statisticalAnalysesList;

    @OneToMany(mappedBy = "appointmentsNotes")
    private List<SpecialistNotes> specialistNotesList;

}
