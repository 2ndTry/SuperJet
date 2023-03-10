package com.alexeymirniy.superjet.common.messages;

import com.alexeymirniy.superjet.common.bean.Source;
import com.alexeymirniy.superjet.common.bean.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfficeStateMessage extends Message {

    public OfficeStateMessage() {
        this.source = Source.OFFICE;
        this.type = Type.STATE;
    }
}