package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserTelegramRepository extends CrudRepository<UserTelegram, Long> {
    Optional<UserTelegram> findByPersonMainService_Id(Long chatId);

    void deleteByPersonMainService_Id(Long id);
}
