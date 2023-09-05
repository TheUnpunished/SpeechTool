package xyz.unpunished.speechtool.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Game {

    PS ("pa"),
    UC("cop"),
    WORLD("cop"),
    OTHER("cop");

    private String stValue;

}
