package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public UserTelegram findById(Long chatId) {
        Optional<UserTelegram> userTelegram = userTelegramRepository.findById(chatId);
        return userTelegram.orElse(null);
    }


    public void save(UserTelegram userTelegram) {
        userTelegramRepository.save(userTelegram);
    }
}
