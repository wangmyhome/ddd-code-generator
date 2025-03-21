package com.zto.ddd.code.generator.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@State(
    name = "DDDCodeGeneratorSettings",
    storages = @Storage("ddd-code-generator.xml")
)
public class GeneratorSettings implements PersistentStateComponent<GeneratorSettings> {
    private String basePackage = "com.zto";
    private String ormPackage = "orm";
    private String domainPackage = "domain";
    private String infrastructurePackage = "infrastructure";
    private String applicationPackage = "app";
    private String clientPackage = "client";

    public static GeneratorSettings getInstance() {
        return ServiceManager.getService(GeneratorSettings.class);
    }

    @Override
    public @Nullable GeneratorSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull GeneratorSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }
} 