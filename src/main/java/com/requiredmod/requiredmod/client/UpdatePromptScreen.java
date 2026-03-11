package com.requiredmod.requiredmod.client;

import com.requiredmod.requiredmod.update.UpdateInfo;
import com.requiredmod.requiredmod.update.UpdateService;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.Util;

import java.nio.file.Path;

public class UpdatePromptScreen extends Screen {
    private final Screen previous;
    private final UpdateInfo update;
    private final UpdateService updateService;
    private Component status = Component.literal("Найдена новая версия: " + updateVersionText()).withStyle(ChatFormatting.YELLOW);
    private boolean downloading;

    protected UpdatePromptScreen(Screen previous, UpdateInfo update, UpdateService updateService) {
        super(Component.literal("RequiredMod обновление"));
        this.previous = previous;
        this.update = update;
        this.updateService = updateService;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Скачать и установить"), button -> startDownload())
                .bounds(centerX - 102, centerY + 20, 204, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Открыть страницу релиза"), button -> Util.getPlatform().openUri(update.releasePage()))
                .bounds(centerX - 102, centerY + 44, 204, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Позже"), button -> this.minecraft.setScreen(previous))
                .bounds(centerX - 102, centerY + 68, 204, 20)
                .build());
    }

    private void startDownload() {
        if (downloading) {
            return;
        }
        downloading = true;
        status = Component.literal("Скачивание...").withStyle(ChatFormatting.GRAY);

        updateService.downloadUpdate(update).whenComplete((Path path, Throwable error) -> this.minecraft.execute(() -> {
            if (error != null) {
                String message = error.getCause() != null ? error.getCause().getMessage() : error.getMessage();
                status = Component.literal("Ошибка загрузки: " + message).withStyle(ChatFormatting.RED);
                downloading = false;
                return;
            }
                this.minecraft.setScreen(new ConfirmScreen(
                        restart -> this.minecraft.setScreen(previous),
                        Component.literal("Обновление загружено"),
                        Component.literal("Файл сохранен в mods: " + path.getFileName() + "\nПерезапустите игру, чтобы применить обновление.")
                ));
            });
        }));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 52, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Версия: " + update.version()), this.width / 2, this.height / 2 - 32, 0xAAAAAA);
        graphics.drawCenteredString(this.font, Component.literal(update.changelog()), this.width / 2, this.height / 2 - 18, 0xAAAAAA);
        graphics.drawCenteredString(this.font, status, this.width / 2, this.height / 2, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private String updateVersionText() {
        return update.version() + " (MC " + update.minecraft() + ", " + update.loader() + ")";
    }
}
