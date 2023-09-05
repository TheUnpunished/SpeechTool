package xyz.unpunished.speechtool.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FileType {

    STH("sth"),
    HDR("hdr"),
    DAT("dat"),
    DEFAULT("sth");

    private String fileTypeSt;

}
