package com.alexeymirniy.superjet.office.controller;

import com.alexeymirniy.superjet.common.bean.Route;
import com.alexeymirniy.superjet.office.service.PathService;
import com.alexeymirniy.superjet.office.service.WaitingRoutesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/routes")
public class RoutRestController {

    private final PathService pathService;
    private final WaitingRoutesService waitingRoutesService;

    @PostMapping(path = "route")
    public void addRoute(@RequestBody List<String> routeLocations) {

        Route route = pathService.convertLocationsToRoute(routeLocations);
        waitingRoutesService.add(route);
    }
}