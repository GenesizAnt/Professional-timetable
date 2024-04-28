package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTelegramService {

    private final UserTelegramRepository userTelegramRepository;

    @Autowired
    public UserTelegramService(UserTelegramRepository userTelegramRepository) {
        this.userTelegramRepository = userTelegramRepository;
    }

    public boolean isUserNotFoundById(Long chatId) {
        return userTelegramRepository.findById(chatId).isEmpty();
    }

    public boolean isUserEmailEmpty(Long chatId) {
        return userTelegramRepository.findById(chatId).get().getEmail().isEmpty();
    }
}
