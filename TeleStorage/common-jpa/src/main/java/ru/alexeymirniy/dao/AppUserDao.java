package ru.alexeymirniy.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexeymirniy.entity.AppUser;

public interface AppUserDao extends JpaRepository<AppUser, Long> {

    AppUser findAppUserByTelegramUserId(Long id);
}
