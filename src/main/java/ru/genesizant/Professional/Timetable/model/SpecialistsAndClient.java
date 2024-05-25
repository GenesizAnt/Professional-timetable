package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.List;

@Comment("Связка Специлист-Клиент")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "specialistsandclient")
public class SpecialistsAndClient {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "specialistid", referencedColumnName = "id")
    private Person specialistList;

    @ManyToOne
    @JoinColumn(name = "visitorid", referencedColumnName = "id")
    private Person visitorList;
}
