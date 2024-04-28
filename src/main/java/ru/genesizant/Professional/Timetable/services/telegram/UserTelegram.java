package ru.genesizant.Professional.Timetable.services.telegram;

import jakarta.persistence.*;
import ru.genesizant.Professional.Timetable.model.Person;

@Entity(name = "usertelegram")
public class UserTelegram {

    @Id
    private Long chatId;

    @Column(name = "firstname")
    private String firstName;

    @Column(name = "lastname")
    private String lastName;

    @Column(name = "personusername")
    private String personusername;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role;

    @OneToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person personMainService;

    public UserTelegram() {
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }


    public Person getPersonMainService() {
        return personMainService;
    }

    public void setPersonMainService(Person personMainService) {
        this.personMainService = personMainService;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPersonusername() {
        return personusername;
    }

    public void setPersonusername(String personusername) {
        this.personusername = personusername;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
