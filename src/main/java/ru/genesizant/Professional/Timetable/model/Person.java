package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import ru.genesizant.Professional.Timetable.services.telegram.UserTelegram;

import java.util.List;

@Comment("Пользователи приложения")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "person")
public class Person {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
    @Column(name = "username")
    private String username;

    @NotEmpty(message = "Фамилия не должна быть пустой")
    @Size(min = 2, max = 100, message = "Фамилия должна быть от 2 до 100 символов")
    @Column(name = "surname")
    private String surname;

    @NotEmpty(message = "Отчество не должно быть пустым")
    @Size(min = 2, max = 100, message = "Отчество должно быть от 2 до 100 символов")
    @Column(name = "patronymic")
    private String patronymic;

    @NotEmpty(message = "email не должен быть пустым")
    @Size(min = 2, max = 100, message = "email должен быть от 2 до 100 символов")
    @Email
    @Column(name = "email")
    private String email;

    @NotEmpty(message = "Пароль не должен быть пустым")
    @Column(name = "password")
    private String password;

    @Column(name = "phonenumber")
    private String phoneNumber;

    @Column(name = "role")
    private String role;

    @Column(name = "jwttoken")
    private String jwtToken;

    @OneToMany(mappedBy = "ownerService")
    private List<ProfessionalServices> professionalServicesList; //ToDo переименовать??

    @OneToMany(mappedBy = "specialistAppointments")
    private List<SpecialistAppointments> specialistAppointmentsList;

    @OneToMany(mappedBy = "visitorAppointments")
    private List<SpecialistAppointments> visitorAppointmentsList;

    @OneToMany(mappedBy = "specialist_statistical")
    private List<StatisticalAnalysis> statisticalAnalysesList;

    @OneToMany(mappedBy = "specialist_notes")
    private List<SpecialistNotes> specialistNotesList;

    @OneToMany(mappedBy = "visitor_notes")
    private List<SpecialistNotes> visitorNotesList;

    @OneToMany(mappedBy = "specialistDateAppointments")
    private List<DatesAppointments> datesAppointmentsList;

    @OneToMany(mappedBy = "specialistList")
    private List<SpecialistsAndClient> specialistSpecialistsAndClientList;

    @OneToMany(mappedBy = "visitorList")
    private List<SpecialistsAndClient> visitorSpecialistsAndClientList;

    @OneToMany(mappedBy = "specialistUnregisteredPerson")
    private List<UnregisteredPerson> specialistUnregisteredPersonList;

    @OneToOne(mappedBy = "personMainService")
    private UserTelegram userTelegram;

    @OneToMany(mappedBy = "specialistPay")
    private List<SpecialistPay> specialistPays;

    @OneToMany(mappedBy = "specId")
    private List<VacantSeat> specIdList;

    @OneToMany(mappedBy = "specIdReception")
    private List<Reception> specIdReceptionList;

    @OneToMany(mappedBy = "visitorIdReception")
    private List<Reception> visitorIdReceptionList;
    public String getFullName() {
        return surname + " " + username + " " + patronymic;
    }

}
