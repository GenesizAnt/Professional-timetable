package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Comment;

import java.util.List;

@Comment("Связка Специлист-Клиент")
@Entity
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

    public SpecialistsAndClient() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getSpecialistList() {
        return specialistList;
    }

    public void setSpecialistList(Person specialistList) {
        this.specialistList = specialistList;
    }

    public Person getVisitorList() {
        return visitorList;
    }

    public void setVisitorList(Person visitorList) {
        this.visitorList = visitorList;
    }
}
