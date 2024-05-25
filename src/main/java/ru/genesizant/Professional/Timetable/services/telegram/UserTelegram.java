package ru.genesizant.Professional.Timetable.services.telegram;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.genesizant.Professional.Timetable.model.Person;

@Entity(name = "usertelegram")
@NoArgsConstructor
@Getter
@Setter
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

    @Column(name = "agree")
    private boolean agree;

    @OneToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person personMainService;

}
