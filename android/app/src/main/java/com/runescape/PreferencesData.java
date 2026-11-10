package com.runescape;

import com.runescape.loginscreen.LoginBackground;

import net.runelite.rs.api.RSClientPreferences;

/**
 * The player's settings, persisted as {@code settings.json} next to the cache.
 *
 * <p>Java port of the desktop Kotlin data class. The field names match the
 * JSON keys (Klaxon maps them by name) and the accessors keep the desktop
 * names, so the shared client code is unchanged.
 */
public class PreferencesData implements RSClientPreferences {

    public double brightnessState = 0.8;
    public boolean enableMusic = true;
    public boolean escapeCloseInterface = false;
    public boolean mergeExpDrops = true;
    public boolean hpAboveHeads = false;
    public boolean enableTooltipHovers = false;
    public boolean enableSkillOrbs = false;
    public boolean enableGroundItemNames = true;
    public boolean enableBuffOverlay = true;
    public boolean combatOverlayBox = true;
    public boolean enableSpecOrb = true;
    public boolean enableOrbs = true;
    public boolean enableRoofs = false;
    public boolean stackSideStones = false;
    public boolean changeChatArea = false;
    public boolean transparentTabArea = false;
    public int npcAttackOptionPriority = 2;
    public int playerAttackOptionPriority = 0;
    public boolean enableShiftClickDrop = true;
    public boolean eulaAccepted = false;
    public boolean rememberUsername = false;
    public boolean hiddenUsername = false;
    public String savedUsername = "";
    public LoginBackground loginBackground = LoginBackground.NORMAL;

    public double getBrightnessState() {
        return brightnessState;
    }

    public void setBrightnessState(double brightnessState) {
        this.brightnessState = brightnessState;
    }

    public boolean getEnableMusic() {
        return enableMusic;
    }

    public void setEnableMusic(boolean enableMusic) {
        this.enableMusic = enableMusic;
    }

    public boolean getEscapeCloseInterface() {
        return escapeCloseInterface;
    }

    public void setEscapeCloseInterface(boolean escapeCloseInterface) {
        this.escapeCloseInterface = escapeCloseInterface;
    }

    public boolean getMergeExpDrops() {
        return mergeExpDrops;
    }

    public void setMergeExpDrops(boolean mergeExpDrops) {
        this.mergeExpDrops = mergeExpDrops;
    }

    public boolean getHpAboveHeads() {
        return hpAboveHeads;
    }

    public void setHpAboveHeads(boolean hpAboveHeads) {
        this.hpAboveHeads = hpAboveHeads;
    }

    public boolean getEnableTooltipHovers() {
        return enableTooltipHovers;
    }

    public void setEnableTooltipHovers(boolean enableTooltipHovers) {
        this.enableTooltipHovers = enableTooltipHovers;
    }

    public boolean getEnableSkillOrbs() {
        return enableSkillOrbs;
    }

    public void setEnableSkillOrbs(boolean enableSkillOrbs) {
        this.enableSkillOrbs = enableSkillOrbs;
    }

    public boolean getEnableGroundItemNames() {
        return enableGroundItemNames;
    }

    public void setEnableGroundItemNames(boolean enableGroundItemNames) {
        this.enableGroundItemNames = enableGroundItemNames;
    }

    public boolean getEnableBuffOverlay() {
        return enableBuffOverlay;
    }

    public void setEnableBuffOverlay(boolean enableBuffOverlay) {
        this.enableBuffOverlay = enableBuffOverlay;
    }

    public boolean getCombatOverlayBox() {
        return combatOverlayBox;
    }

    public void setCombatOverlayBox(boolean combatOverlayBox) {
        this.combatOverlayBox = combatOverlayBox;
    }

    public boolean getEnableSpecOrb() {
        return enableSpecOrb;
    }

    public void setEnableSpecOrb(boolean enableSpecOrb) {
        this.enableSpecOrb = enableSpecOrb;
    }

    public boolean getEnableOrbs() {
        return enableOrbs;
    }

    public void setEnableOrbs(boolean enableOrbs) {
        this.enableOrbs = enableOrbs;
    }

    public boolean getEnableRoofs() {
        return enableRoofs;
    }

    public void setEnableRoofs(boolean enableRoofs) {
        this.enableRoofs = enableRoofs;
    }

    public boolean getStackSideStones() {
        return stackSideStones;
    }

    public void setStackSideStones(boolean stackSideStones) {
        this.stackSideStones = stackSideStones;
    }

    public boolean getChangeChatArea() {
        return changeChatArea;
    }

    public void setChangeChatArea(boolean changeChatArea) {
        this.changeChatArea = changeChatArea;
    }

    public boolean getTransparentTabArea() {
        return transparentTabArea;
    }

    public void setTransparentTabArea(boolean transparentTabArea) {
        this.transparentTabArea = transparentTabArea;
    }

    public int getNpcAttackOptionPriority() {
        return npcAttackOptionPriority;
    }

    public void setNpcAttackOptionPriority(int npcAttackOptionPriority) {
        this.npcAttackOptionPriority = npcAttackOptionPriority;
    }

    public int getPlayerAttackOptionPriority() {
        return playerAttackOptionPriority;
    }

    public void setPlayerAttackOptionPriority(int playerAttackOptionPriority) {
        this.playerAttackOptionPriority = playerAttackOptionPriority;
    }

    public boolean getEnableShiftClickDrop() {
        return enableShiftClickDrop;
    }

    public void setEnableShiftClickDrop(boolean enableShiftClickDrop) {
        this.enableShiftClickDrop = enableShiftClickDrop;
    }

    public boolean getEulaAccepted() {
        return eulaAccepted;
    }

    public void setEulaAccepted(boolean eulaAccepted) {
        this.eulaAccepted = eulaAccepted;
    }

    public boolean getRememberUsername() {
        return rememberUsername;
    }

    public void setRememberUsername(boolean rememberUsername) {
        this.rememberUsername = rememberUsername;
    }

    public boolean getHiddenUsername() {
        return hiddenUsername;
    }

    public void setHiddenUsername(boolean hiddenUsername) {
        this.hiddenUsername = hiddenUsername;
    }

    public String getSavedUsername() {
        return savedUsername;
    }

    public void setSavedUsername(String savedUsername) {
        this.savedUsername = savedUsername;
    }

    public LoginBackground getLoginBackground() {
        return loginBackground;
    }

    public void setLoginBackground(LoginBackground loginBackground) {
        this.loginBackground = loginBackground;
    }

    // ---- RSClientPreferences ------------------------------------------------

    @Override
    public String getRememberedUsername() {
        return savedUsername;
    }

    @Override
    public void setRememberedUsername(String username) {
        this.savedUsername = username;
    }

    @Override
    public int getSoundEffectVolume() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setSoundEffectVolume(int volume) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getAreaSoundEffectVolume() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setAreaSoundEffectVolume(int volume) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public int getMusicVolume() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void setMusicVolume(int volume) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean getHideUsername() {
        return hiddenUsername;
    }
}
