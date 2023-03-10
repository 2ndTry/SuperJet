package com.alexeymirniy.superjet.ship.job;

import com.alexeymirniy.superjet.common.bean.Board;
import com.alexeymirniy.superjet.common.bean.RoutePath;
import com.alexeymirniy.superjet.common.messages.BoardStateMessage;
import com.alexeymirniy.superjet.common.processor.MessageConverter;
import com.alexeymirniy.superjet.ship.provider.BoardProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
@EnableAsync
@RequiredArgsConstructor
public class BoardJob {

    private final BoardProvider boardProvider;
    private final MessageConverter messageConverter;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(initialDelay = 0, fixedDelay = 250)
    public void fly() {
        boardProvider.getBoards().stream()
                .filter(Board::hasRoute)
                .forEach(board -> {
                    board.getRoute()
                            .getPath()
                            .stream()
                            .filter(RoutePath::inProgress)
                            .findFirst()
                            .ifPresent(routePath -> {
                                board.calculatePosition(routePath);
                                routePath.addProgress(board.getSpeed());

                                if(routePath.done()) {
                                    board.setLocation(routePath.getTo().getName());
                                }
                            });
                });
    }

    @Async
    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    public void notifyState() {
        boardProvider.getBoards().stream()
                .filter(Board::isBusy)
                .forEach(board -> {
                    Optional<RoutePath> route = board
                            .getRoute()
                            .getPath()
                            .stream()
                            .filter(RoutePath::inProgress)
                            .findAny();

                    if(route.isEmpty()) {
                        List<RoutePath> path = board.getRoute().getPath();
                        board.setLocation(path.get(path.size() - 1).getTo().getName());
                        board.setBusy(false);
                    }

                    BoardStateMessage boardStateMessage = new BoardStateMessage(board);

                    kafkaTemplate.sendDefault(messageConverter.toJson(boardStateMessage));
                });
    }
}