package com.alexeymirniy.superjet.common.messages;

import com.alexeymirniy.superjet.common.bean.Route;
import com.alexeymirniy.superjet.common.bean.Source;
import com.alexeymirniy.superjet.common.bean.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfficeRouteMessage extends Message {

    private Route route;

    public OfficeRouteMessage() {
        this.source = Source.OFFICE;
        this.type = Type.ROUTE;
    }

    public OfficeRouteMessage(Route value) {
        this();
        this.route = value;
    }
}