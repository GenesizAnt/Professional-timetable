package ru.genesizant.Professional.Timetable.services.telegram;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record BotProperties(String name, String token) {
}
