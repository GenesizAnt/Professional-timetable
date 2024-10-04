package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Comment("Хранение данных по платежным ссылкам специалиста")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "specialist_pay")
public class SpecialistPay {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialist_id", referencedColumnName = "id")
    private Person specialistPay;

    @Column(name = "link_pay")
    private String linkPay;
}
