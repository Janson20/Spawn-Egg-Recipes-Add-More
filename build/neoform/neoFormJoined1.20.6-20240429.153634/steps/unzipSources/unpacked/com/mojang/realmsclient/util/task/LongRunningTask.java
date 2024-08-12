package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LongRunningTask implements Runnable {
    protected static final int NUMBER_OF_RETRIES = 25;
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean aborted = false;

    protected static void pause(long p_167656_) {
        try {
            Thread.sleep(p_167656_ * 1000L);
        } catch (InterruptedException interruptedexception) {
            Thread.currentThread().interrupt();
            LOGGER.error("", (Throwable)interruptedexception);
        }
    }

    public static void setScreen(Screen p_90406_) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(p_90406_));
    }

    protected void error(Component p_90408_) {
        this.abortTask();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(new RealmsGenericErrorScreen(p_90408_, new RealmsMainScreen(new TitleScreen()))));
    }

    protected void error(Exception p_300964_) {
        if (p_300964_ instanceof RealmsServiceException realmsserviceexception) {
            this.error(realmsserviceexception.realmsError.errorMessage());
        } else {
            this.error(Component.literal(p_300964_.getMessage()));
        }
    }

    protected void error(RealmsServiceException p_301270_) {
        this.error(p_301270_.realmsError.errorMessage());
    }

    public abstract Component getTitle();

    public boolean aborted() {
        return this.aborted;
    }

    public void tick() {
    }

    public void init() {
    }

    public void abortTask() {
        this.aborted = true;
    }
}
