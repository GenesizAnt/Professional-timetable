package ru.genesizant.Professional.Timetable.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.genesizant.Professional.Timetable.model.Person;
import ru.genesizant.Professional.Timetable.model.SpecialistPay;
import ru.genesizant.Professional.Timetable.repositories.SpecialistPayRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialistPayService {

    private final SpecialistPayRepository specialistPayRepository;
    private final PersonService personService;

    public Optional<SpecialistPay> findBySpecialistPay(Long id) {
        return specialistPayRepository.findBySpecialistPay_Id(id);
    }

    public void saveNewLinkPay(String linkpay, Long id) {
        Optional<SpecialistPay> specialistPayId = specialistPayRepository.findBySpecialistPay_Id(id);
        Optional<Person> person = personService.findById(id);
        if (specialistPayId.isPresent()) {
            specialistPayId.get().setLinkPay(linkpay);
            specialistPayRepository.save(specialistPayId.get());
        } else {
            if (person.isPresent()) {
                SpecialistPay specialistPay = new SpecialistPay();
                specialistPay.setSpecialistPay(person.get());
                specialistPay.setLinkPay(linkpay);
                specialistPayRepository.save(specialistPay);
            }
        }
    }
}
