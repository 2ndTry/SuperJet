package com.alexeymirniy.superjet.common.messages;

import com.alexeymirniy.superjet.common.bean.Board;
import com.alexeymirniy.superjet.common.bean.Source;
import com.alexeymirniy.superjet.common.bean.Type;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardStateMessage extends Message{

    private Board board;


    public BoardStateMessage() {
        this.source = Source.BOARD;
        this.type = Type.STATE;
    }

    public BoardStateMessage(Board value) {
        this();
        this.board = value;
    }
}