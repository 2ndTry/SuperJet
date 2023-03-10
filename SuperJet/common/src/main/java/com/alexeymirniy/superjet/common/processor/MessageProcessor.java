package com.alexeymirniy.superjet.common.processor;

import com.alexeymirniy.superjet.common.messages.Message;

public interface MessageProcessor<T extends Message> {

    void process(String jsonMessage);
}
