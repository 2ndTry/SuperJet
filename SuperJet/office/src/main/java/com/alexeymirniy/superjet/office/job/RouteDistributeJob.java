package com.alexeymirniy.superjet.office.job;

import com.alexeymirniy.superjet.common.bean.AirPort;
import com.alexeymirniy.superjet.common.bean.Board;
import com.alexeymirniy.superjet.common.bean.Route;
import com.alexeymirniy.superjet.common.bean.RoutePath;
import com.alexeymirniy.superjet.common.messages.AirPortStateMessage;
import com.alexeymirniy.superjet.common.messages.OfficeRouteMessage;
import com.alexeymirniy.superjet.common.processor.MessageConverter;
import com.alexeymirniy.superjet.office.provider.AirPortsProvider;
import com.alexeymirniy.superjet.office.provider.BoardsProvider;
import com.alexeymirniy.superjet.office.service.PathService;
import com.alexeymirniy.superjet.office.service.WaitingRoutesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RouteDistributeJob {

    private final PathService pathService;
    private final BoardsProvider boardsProvider;
    private final WaitingRoutesService waitingRoutesService;
    private final AirPortsProvider airPortsProvider;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageConverter messageConverter;

    @Scheduled(initialDelay = 500, fixedDelay = 2500)
    private void distribute() {

        waitingRoutesService.list().stream()
                .filter(Route::notAssigned)
                .forEach(route -> {

                    String startLocation = route.getPath().get(0).getFrom().getName();

                    boardsProvider.getBoards().stream()
                            .filter(board -> startLocation.equals(board.getLocation()) && board.noBusy())
                            .findFirst()
                            .ifPresent(board -> sendBoardToRoute(route, board));

                    if(route.notAssigned()) {
                        boardsProvider.getBoards().stream()
                                .filter(Board::noBusy)
                                .findFirst()
                                .ifPresent(board -> {
                                    String currentLocation = board.getLocation();
                                    if(!currentLocation.equals(startLocation)) {
                                        RoutePath routePath = pathService.makePath(currentLocation, startLocation);
                                        route.getPath().add(0, routePath);
                                    }
                                    sendBoardToRoute(route, board);
                                });
                    }
                });
    }

    private void sendBoardToRoute(Route route, Board board) {

        route.setBoardName(board.getName());
        AirPort airPort = airPortsProvider.findAirPortAndRemoveBoard(board.getName());
        board.setLocation(null);

        kafkaTemplate.sendDefault(messageConverter.toJson(new OfficeRouteMessage(route)));
        kafkaTemplate.sendDefault(messageConverter.toJson(new AirPortStateMessage(airPort)));
    }
}