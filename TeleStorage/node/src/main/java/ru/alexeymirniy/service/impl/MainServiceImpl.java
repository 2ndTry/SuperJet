package ru.alexeymirniy.service.impl;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.alexeymirniy.dao.AppUserDao;
import ru.alexeymirniy.dao.RawDataDao;
import ru.alexeymirniy.entity.AppDocument;
import ru.alexeymirniy.entity.AppUser;
import ru.alexeymirniy.entity.RawData;
import ru.alexeymirniy.exception.UploadFileException;
import ru.alexeymirniy.service.FileService;
import ru.alexeymirniy.service.MainService;
import ru.alexeymirniy.service.ProducerService;
import ru.alexeymirniy.service.enums.ServiceCommand;

import static ru.alexeymirniy.entity.enam.UserState.BASIC_STATE;
import static ru.alexeymirniy.entity.enam.UserState.WAIT_FOR_EMAIL_STATE;
import static ru.alexeymirniy.service.enums.ServiceCommand.*;

@Service
@Log4j
public class MainServiceImpl implements MainService {

    private final RawDataDao rawDataDao;
    private final ProducerService producerService;
    private final AppUserDao appUserDao;
    private final FileService fileService;

    public MainServiceImpl(RawDataDao rawDataDao,
                           ProducerService producerService,
                           AppUserDao appUserDao,
                           FileService fileService) {
        this.rawDataDao = rawDataDao;
        this.producerService = producerService;
        this.appUserDao = appUserDao;
        this.fileService = fileService;
    }

    @Override
    public void processTextMessage(Update update) {

        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        var serviceCommand = ServiceCommand.fromValue(text);

        if (CANCEL.equals(serviceCommand)) {
            output = cancelProcess(appUser);
        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO  добавить обработку почты
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка! Введите \"/cancel\" и попробуйте снова";
        }

        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {

        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        try {

            //TODO добавить генерацию ссылки для скачивания
            AppDocument doc = fileService.processDoc(update.getMessage());
            var answer = "Документ успешно загружен! Ссылка на скачивание: http://test.ru/get-doc/777";
            sendAnswer(answer, chatId);
        } catch (UploadFileException e) {

            log.error(e);
            String error = "К сожалению, загрузка файла не удалась. Повторите попытку позже.";
            sendAnswer(error, chatId);
        }
    }

    @Override
    public void processPhotoMessage(Update update) {

        saveRawData(update);

        var appUser = findOrSaveAppUser(update);
        var chatId = update.getMessage().getChatId();

        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }

        //TODO добавить сохранения фото
        var answer = "Фото успешно загружено! Ссылка на скачивание: http://test.ru/get-photo/777";

        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {

        var userState = appUser.getState();

        if (!appUser.getIsActive()) {
            var error = "Зарегистрируйтесь или активируйте свою учетную запись для загрузки контента.";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            var error = "Отмените текущую команду с помощью \"/cancel\"";
            sendAnswer(error, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);

        producerService.produceAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {

        if (REGISTRATION.toString().equals(cmd)) {
            //TODO добавить регистрацию
            return "Временно недоступно";
        } else if (HELP.toString().equals(cmd)) {
            return help();
        } else if (START.toString().equals(cmd)) {
            return "Приветствую! Чтобы посмотреть список доступных команд введите команду \"/help\"";
        } else {
            return "Неизвестная команда! Чтобы посмотреть список доступных команд введите команду \"/help\"";
        }
    }

    private String help() {
        return "Список доступных команд:\n" +
                "/cancel - отмена выполнения текущей команды;\n" +
                "/registration - регистрация пользователя;\n";
    }

    private String cancelProcess(AppUser appUser) {

        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);

        return "Команда отменена!";
    }

    private AppUser findOrSaveAppUser(Update update) {

        User telegramUser = update.getMessage().getFrom();

        AppUser persistentAppUser = appUserDao.findAppUserByTelegramUserId(telegramUser.getId());

        if (persistentAppUser == null) {

            AppUser transientAppUser = AppUser.builder()
                    .telegramUserId(telegramUser.getId())
                    .userName(telegramUser.getUserName())
                    .firstName(telegramUser.getFirstName())
                    .lastName(telegramUser.getLastName())
                    //TODO Изменить значение по умолчанию после добавления регистрации
                    .isActive(true)
                    .state(BASIC_STATE)
                    .build();

            return appUserDao.save(transientAppUser);
        }

        return persistentAppUser;
    }

    private void saveRawData(Update update) {

        RawData rawData = RawData.builder()
                .event(update)
                .build();

        rawDataDao.save(rawData);
    }
}