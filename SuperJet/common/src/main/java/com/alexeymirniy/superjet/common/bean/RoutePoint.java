package com.alexeymirniy.superjet.common.bean;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RoutePoint {

    private String name;
    private double x;
    private double y;

    public RoutePoint(AirPort airPort) {
        this.name = airPort.getName();
        this.x = airPort.getX();
        this.y = airPort.getY();
    }
}