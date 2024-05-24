package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserTelegramRepository extends CrudRepository<UserTelegram, Long> {
    Optional<UserTelegram> findByPersonMainService_Id(Long chatId);

    @Transactional
    void deleteByPersonMainService_Id(Long id);
}
