package ru.alexeymirniy.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.alexeymirniy.entity.AppDocument;

public interface FileService {

    AppDocument processDoc(Message externalMessage);
}
