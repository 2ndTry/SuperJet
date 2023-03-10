package com.alexeymirniy.superjet.ship.listener.processor;

import com.alexeymirniy.superjet.common.bean.Route;
import com.alexeymirniy.superjet.common.messages.OfficeRouteMessage;
import com.alexeymirniy.superjet.common.processor.MessageConverter;
import com.alexeymirniy.superjet.common.processor.MessageProcessor;
import com.alexeymirniy.superjet.ship.provider.BoardProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("OFFICE_ROUTE")
@RequiredArgsConstructor
public class OfficeRouteProcessor implements MessageProcessor<OfficeRouteMessage> {

    private final MessageConverter messageConverter;
    private final BoardProvider boardProvider;

    @Override
    public void process(String jsonMessage) {

        OfficeRouteMessage officeRouteMessage = messageConverter.extractMessage(jsonMessage, OfficeRouteMessage.class);
        Route route = officeRouteMessage.getRoute();

        boardProvider.getBoards().stream()
                .filter(board -> board.noBusy() && route.getBoardName().equals(board.getName()))
                .findFirst()
                .ifPresent(board -> {
                    board.setRoute(route);
                    board.setBusy(true);
                    board.setLocation(null);
                });
    }
}
