package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Comment("Статистика - пока показывает только время")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "statisticalanalysis")
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

}
