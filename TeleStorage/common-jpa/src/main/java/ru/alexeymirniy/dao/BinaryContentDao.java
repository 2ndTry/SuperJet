package ru.alexeymirniy.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexeymirniy.entity.BinaryContent;

public interface BinaryContentDao extends JpaRepository<BinaryContent, Long> {
}
