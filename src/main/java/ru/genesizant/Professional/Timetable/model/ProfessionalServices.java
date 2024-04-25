package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Comment;

import java.time.Duration;
import java.util.List;

@Comment("Услуги специалиста")
@Entity
@Table(name = "professionalservices")
public class ProfessionalServices {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Название не должно быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
    @Column(name = "servicename")
    private String serviceName;

    @Size(min = 2, max = 500, message = "Описание должно быть от 2 до 500 символов")
    @Column(name = "shortdescription")
    private String shortDescription;

    @Comment("Длительность услуги")
    @Min(value = 0)
    @Column(name = "durationservice")     //ToDo https://qaa-engineer.ru/kak-ispolzovat-tip-interval-v-postgresql/
    private Duration durationService;

    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person ownerService;

    @OneToMany(mappedBy = "professionalServices")
    private List<SpecialistAppointments> listServicesRendered;

    public ProfessionalServices() {
    }

    public List<SpecialistAppointments> getListServicesRendered() {
        return listServicesRendered;
    }

    public void setListServicesRendered(List<SpecialistAppointments> listServicesRendered) {
        this.listServicesRendered = listServicesRendered;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Duration getDurationService() {
        return durationService;
    }

    public void setDurationService(Duration durationService) {
        this.durationService = durationService;
    }

    public Person getOwnerService() {
        return ownerService;
    }

    public void setOwnerService(Person ownerService) {
        this.ownerService = ownerService;
    }
}
