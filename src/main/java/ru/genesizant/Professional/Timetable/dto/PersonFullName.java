package ru.genesizant.Professional.Timetable.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonFullName {

    private Long id;

    private String username;

    private String surname;

    private String patronymic;

    @Override
    public String toString() {
        return surname + ' ' + username + ' ' + patronymic;
    }
}
