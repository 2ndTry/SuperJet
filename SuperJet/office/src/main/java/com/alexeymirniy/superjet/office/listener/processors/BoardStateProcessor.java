package com.alexeymirniy.superjet.office.listener.processors;

import com.alexeymirniy.superjet.common.bean.AirPort;
import com.alexeymirniy.superjet.common.bean.Board;
import com.alexeymirniy.superjet.common.bean.Route;
import com.alexeymirniy.superjet.common.messages.AirPortStateMessage;
import com.alexeymirniy.superjet.common.messages.BoardStateMessage;
import com.alexeymirniy.superjet.common.processor.MessageConverter;
import com.alexeymirniy.superjet.common.processor.MessageProcessor;
import com.alexeymirniy.superjet.office.provider.AirPortsProvider;
import com.alexeymirniy.superjet.office.provider.BoardsProvider;
import com.alexeymirniy.superjet.office.service.WaitingRoutesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("BOARD_STATE")
@RequiredArgsConstructor
public class BoardStateProcessor implements MessageProcessor<BoardStateMessage> {

    private final MessageConverter messageConverter;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final WaitingRoutesService waitingRoutesService;
    private final BoardsProvider boardsProvider;
    private final AirPortsProvider airPortsProvider;

    @Override
    public void process(String jsonMessage) {

        BoardStateMessage message = messageConverter.extractMessage(jsonMessage, BoardStateMessage.class);
        Board board = message.getBoard();
        Optional<Board> previousOpt = boardsProvider.getBoard(board.getName());
        AirPort airPort = airPortsProvider.getAirPort(board.getLocation());

        boardsProvider.addBoard(board);

        if(previousOpt.isPresent() && board.hasRoute() && !previousOpt.get().hasRoute()) {
            Route route = board.getRoute();
            waitingRoutesService.remove(route);
        }

        if(previousOpt.isEmpty() || !board.isBusy() && previousOpt.get().isBusy()) {
            airPort.addBoard(board.getName());
            kafkaTemplate.sendDefault(messageConverter.toJson(new AirPortStateMessage(airPort)));
        }
    }
}