package ru.genesizant.Professional.Timetable.services.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserTelegramService {

    private final UserTelegramRepository userTelegramRepository;

    public boolean isUserNotFoundById(Long chatId) {
        return userTelegramRepository.findById(chatId).isEmpty();
    }

    public boolean isUserEmailEmpty(Long chatId) {
        Optional<UserTelegram> userOptional = userTelegramRepository.findById(chatId);
        return userOptional.isEmpty() || userOptional.get().getEmail() == null || userOptional.get().getEmail().isEmpty();
    }

    public Optional<UserTelegram> findByPersonId(Long id) {
        return userTelegramRepository.findByPersonMainService_Id(id);
    }

    public void save(UserTelegram userTelegram) {
        userTelegramRepository.save(userTelegram);
    }

    public Iterable<UserTelegram> findAll() {
        return userTelegramRepository.findAll();
    }

    public UserTelegram findById(Long chatId) {
        Optional<UserTelegram> userTelegram = userTelegramRepository.findById(chatId);
        return userTelegram.orElse(null);
    }

    //Удалить аккаунт
    @Transactional
    public void deleteByPersonId(Long id) {
        userTelegramRepository.deleteByPersonMainService_Id(id);
    }
}
