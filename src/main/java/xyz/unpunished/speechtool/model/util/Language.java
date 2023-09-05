package xyz.unpunished.speechtool.model.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Language {

    ENGLISH("en"),
    FRENCH("fr"),
    GERMAN("ge"),
    ITALIAN("it"),
    JAPANESE("jp"),
    RUSSIAN("ru"),
    SPANISH("sp"),
    DEFAULT("en");

    private String languageSt;

}
