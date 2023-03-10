package com.alexeymirniy.superjet.common.messages;

import com.alexeymirniy.superjet.common.bean.Source;
import com.alexeymirniy.superjet.common.bean.Type;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Message {

    protected Type type;
    protected Source source;

    public String getCode() {
        return source.name() + "_" + type.name();
    }
}
