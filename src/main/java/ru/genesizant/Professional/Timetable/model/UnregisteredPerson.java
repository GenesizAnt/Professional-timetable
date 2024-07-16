package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "unregisteredperson")
public class UnregisteredPerson {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Имя не должно быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
    @Column(name = "username")
    private String usernameUnregistered;

    @NotEmpty(message = "Фамилия не должна быть пустой")
    @Size(min = 2, max = 100, message = "Фамилия должна быть от 2 до 100 символов")
    @Column(name = "surname")
    private String surnameUnregistered;

    @Size(min = 2, max = 100, message = "Отчество должно быть от 2 до 100 символов")
    @Column(name = "patronymic")
    private String patronymicUnregistered;

    @ManyToOne
    @JoinColumn(name = "specialistid", referencedColumnName = "id")
    private Person specialistUnregisteredPerson;

    public UnregisteredPerson() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsernameUnregistered() {
        return usernameUnregistered;
    }

    public void setUsernameUnregistered(String usernameUnregistered) {
        this.usernameUnregistered = usernameUnregistered;
    }

    public String getSurnameUnregistered() {
        return surnameUnregistered;
    }

    public void setSurnameUnregistered(String surnameUnregistered) {
        this.surnameUnregistered = surnameUnregistered;
    }

    public String getPatronymicUnregistered() {
        return patronymicUnregistered;
    }

    public void setPatronymicUnregistered(String patronymicUnregistered) {
        this.patronymicUnregistered = patronymicUnregistered;
    }

    //    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getSurname() {
//        return surname;
//    }
//
//    public void setSurname(String surname) {
//        this.surname = surname;
//    }
//
//    public String getPatronymic() {
//        return patronymic;
//    }
//
//    public void setPatronymic(String patronymic) {
//        this.patronymic = patronymic;
//    }

    public Person getSpecialistUnregisteredPerson() {
        return specialistUnregisteredPerson;
    }

    public void setSpecialistUnregisteredPerson(Person specialistUnregisteredPerson) {
        this.specialistUnregisteredPerson = specialistUnregisteredPerson;
    }
}
