package com.mcst.easyfk.idea.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "com.mcst.easyfk.idea.settings.EasyfkSettingsState",
        storages = @Storage("easyfk-generator.xml")
)
@Service(Service.Level.APP)
public final class EasyfkSettingsState implements PersistentStateComponent<EasyfkSettings> {

    private EasyfkSettings state = new EasyfkSettings();

    public static EasyfkSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(EasyfkSettingsState.class);
    }

    @Override
    public @Nullable EasyfkSettings getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull EasyfkSettings state) {
        this.state = state;
    }
}
