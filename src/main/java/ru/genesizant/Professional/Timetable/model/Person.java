package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Comment;

import java.util.List;

@Comment("Пользователи приложения")
@Entity
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

    @NotEmpty(message = "Номер телефона не должен быть пустым")
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

    public Person() {
    }

    public String getFullName() {
        return surname + " " + username + " " + patronymic;
    }

    public List<UnregisteredPerson> getSpecialistUnregisteredPersonList() {
        return specialistUnregisteredPersonList;
    }

    public void setSpecialistUnregisteredPersonList(List<UnregisteredPerson> specialistUnregisteredPersonList) {
        this.specialistUnregisteredPersonList = specialistUnregisteredPersonList;
    }

    public List<DatesAppointments> getDatesAppointmentsList() {
        return datesAppointmentsList;
    }

    public void setDatesAppointmentsList(List<DatesAppointments> datesAppointmentsList) {
        this.datesAppointmentsList = datesAppointmentsList;
    }

    public List<SpecialistsAndClient> getSpecialistSpecialistsAndClientList() {
        return specialistSpecialistsAndClientList;
    }

    public void setSpecialistSpecialistsAndClientList(List<SpecialistsAndClient> specialistSpecialistsAndClientList) {
        this.specialistSpecialistsAndClientList = specialistSpecialistsAndClientList;
    }

    public List<SpecialistsAndClient> getVisitorSpecialistsAndClientList() {
        return visitorSpecialistsAndClientList;
    }

    public void setVisitorSpecialistsAndClientList(List<SpecialistsAndClient> visitorSpecialistsAndClientList) {
        this.visitorSpecialistsAndClientList = visitorSpecialistsAndClientList;
    }

    public List<SpecialistNotes> getSpecialistNotesList() {
        return specialistNotesList;
    }

    public void setSpecialistNotesList(List<SpecialistNotes> specialistNotesList) {
        this.specialistNotesList = specialistNotesList;
    }

    public List<SpecialistNotes> getVisitorNotesList() {
        return visitorNotesList;
    }

    public void setVisitorNotesList(List<SpecialistNotes> visitorNotesList) {
        this.visitorNotesList = visitorNotesList;
    }

    public List<StatisticalAnalysis> getStatisticalAnalysesList() {
        return statisticalAnalysesList;
    }

    public void setStatisticalAnalysesList(List<StatisticalAnalysis> statisticalAnalysesList) {
        this.statisticalAnalysesList = statisticalAnalysesList;
    }

    public List<SpecialistAppointments> getSpecialistAppointmentsList() {
        return specialistAppointmentsList;
    }

    public void setSpecialistAppointmentsList(List<SpecialistAppointments> specialistAppointments) {
        this.specialistAppointmentsList = specialistAppointments;
    }

    public List<SpecialistAppointments> getVisitorAppointmentsList() {
        return visitorAppointmentsList;
    }

    public void setVisitorAppointmentsList(List<SpecialistAppointments> visitorAppointments) {
        this.visitorAppointmentsList = visitorAppointments;
    }

    public List<ProfessionalServices> getProfessionalServicesList() {
        return professionalServicesList;
    }

    public void setProfessionalServicesList(List<ProfessionalServices> professionalServicesList) {
        this.professionalServicesList = professionalServicesList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public String toString() {
        return "Person{" +
                "username='" + username + '\'' +
                ", surname='" + surname + '\'' +
                ", patronymic='" + patronymic + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
