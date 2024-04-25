package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Comment("Записи приемов специалиста")
@Entity
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

    @OneToMany(mappedBy = "specialistAppointments")
    private List<StatisticalAnalysis> statisticalAnalysesList;

    @OneToMany(mappedBy = "appointmentsNotes")
    private List<SpecialistNotes> specialistNotesList;

    public SpecialistAppointments() {
    }

    public boolean isPrepayment() {
        return prepayment;
    }

    public void setPrepayment(boolean prepayment) {
        this.prepayment = prepayment;
    }

    public List<StatisticalAnalysis> getStatisticalAnalysesList() {
        return statisticalAnalysesList;
    }

    public void setStatisticalAnalysesList(List<StatisticalAnalysis> statisticalAnalysesList) {
        this.statisticalAnalysesList = statisticalAnalysesList;
    }

    public List<SpecialistNotes> getSpecialistNotesList() {
        return specialistNotesList;
    }

    public void setSpecialistNotesList(List<SpecialistNotes> specialistNotesList) {
        this.specialistNotesList = specialistNotesList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getSpecialistAppointments() {
        return specialistAppointments;
    }

    public void setSpecialistAppointments(Person specialistAppointments) {
        this.specialistAppointments = specialistAppointments;
    }

    public Person getVisitorAppointments() {
        return visitorAppointments;
    }

    public void setVisitorAppointments(Person visitorAppointments) {
        this.visitorAppointments = visitorAppointments;
    }

    public ProfessionalServices getProfessionalServices() {
        return professionalServices;
    }

    public void setProfessionalServices(ProfessionalServices professionalServices) {
        this.professionalServices = professionalServices;
    }

    public boolean isConsultationSuccessful() {
        return consultationSuccessful;
    }

    public void setConsultationSuccessful(boolean consultationSuccessful) {
        this.consultationSuccessful = consultationSuccessful;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public boolean isPrepaymentVisitor() {
        return prepaymentVisitor;
    }

    public void setPrepaymentVisitor(boolean prepaymentVisitor) {
        this.prepaymentVisitor = prepaymentVisitor;
    }
}
