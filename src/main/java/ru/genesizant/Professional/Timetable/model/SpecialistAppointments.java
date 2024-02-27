package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "specialistappointments")
public class SpecialistAppointments {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialist_id", referencedColumnName = "id")
    private Person specialist_appointments;

    @ManyToOne
    @JoinColumn(name = "visitor_id", referencedColumnName = "id")
    private Person visitor_appointments;

    @ManyToOne
    @JoinColumn(name = "services_id", referencedColumnName = "id")
    private ProfessionalServices professionalServices;

    @Column(name = "consultation_successful")
    private boolean consultationSuccessful;

    @Column(name = "visit_type")
    private String visitType; //первичное или вторичное

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "appointmentTime")
    private LocalDateTime appointmentTime;

    @Column(name = "consultation_fee")
    private BigDecimal consultationFee;

    @Column(name = "prepayment")
    private boolean prepayment;

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

    public Person getSpecialist_appointments() {
        return specialist_appointments;
    }

    public void setSpecialist_appointments(Person specialist_appointments) {
        this.specialist_appointments = specialist_appointments;
    }

    public Person getVisitor_appointments() {
        return visitor_appointments;
    }

    public void setVisitor_appointments(Person visitor_appointments) {
        this.visitor_appointments = visitor_appointments;
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

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }
}
