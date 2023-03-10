package ru.alexeymirniy.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.alexeymirniy.entity.RawData;

@Repository
public interface RawDataDao extends JpaRepository<RawData, Long> {
}
