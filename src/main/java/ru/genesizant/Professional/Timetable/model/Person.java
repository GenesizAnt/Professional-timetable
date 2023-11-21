package ru.genesizant.Professional.Timetable.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "Person")
public class Person {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

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

    @Column(name = "numbervisits")
    private int numberVisits;

    @Column(name = "totalamount")
    private BigDecimal totalAmount;

    @Column(name = "role")
    private String role;

    @Column(name = "jwttoken")
    private String jwtToken;

    public Person() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getNumberVisits() {
        return numberVisits;
    }

    public void setNumberVisits(int numberVisits) {
        this.numberVisits = numberVisits;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
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
