package ru.ptvi.elscriptserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "script-server")
public class ScriptServerProperties {

    private String pathToScripts;

    private int finishedScriptStoreHours;
}
