package ru.alexeymirniy.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexeymirniy.entity.AppDocument;

public interface AppDocumentDao extends JpaRepository<AppDocument, Long> {
}
