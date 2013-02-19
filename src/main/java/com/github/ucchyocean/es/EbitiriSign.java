/*
 * Copyright ucchy 2013
 */
package com.github.ucchyocean.es;

import java.util.Random;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author ucchy
 *
 */
public class EbitiriSign extends JavaPlugin implements Listener {

    private static final String SIGN_FIRST = "[Ebitiri]";
    private static final String EBITIRI = "・。 ・";
    private static final String[][] EBITIRI_WORDS = {
        {ChatColor.GRAY + "・。 ・ ＜ piiiii"},
        {ChatColor.GRAY + "・。 ・ｂ"},
        {ChatColor.GRAY + "ｄ・。 ・ｂ"},
        {ChatColor.GRAY + "；。 ；"},
        {ChatColor.GOLD + "(*o ω n *) 三 (* o ω n*)", ChatColor.GRAY + "・。 ・ｐ funckin'..."},
    };

    private static final int SPECIAL_EVENT_CHANCE = 10;
    private static final int COLOR_CHANCE = 25;
    private static final int BIG_CHANCE = 20;
    private static final int MONEY_CHANCE = 15;
    private static final int WORDS_CHANCE = 30;
    private static final int THUNDER_CHANCE = 5;
    private static final int EXPLODE_CHANCE = 5;

    private static Economy econ;

    private int specialEventPercent;
    private int colorEvent;
    private int bigEvent;
    private int moneyEvent;
    private int wordsEvent;
    private int thunderEvent;
    private int explodeEvent;

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onLoad()
     */
    @Override
    public void onEnable() {
        getLogger().info("[・。・]様のご来店です！");
        getServer().getPluginManager().registerEvents(this, this);

        reloadConfigFile();

        Plugin vault = Bukkit.getServer().getPluginManager().getPlugin("Vault");
        if ( vault != null ) {
            econ = Bukkit.getServer().getServicesManager()
                    .getRegistration(Economy.class).getProvider();
        }
    }

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        getLogger().info("[・。・]様のお帰りです！");
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {

        if (event.getLine(0).equals(SIGN_FIRST)) {
            Player player = event.getPlayer();

            if ( !player.hasPermission("ebitirisign.set") ) {
                // パーミッションを持っていない。
                //player.getWorld().strikeLightning(player.getLocation());
                player.sendMessage(ChatColor.RED + EBITIRI + "＃ You don't have permission \"ebitirisign.set\"!!!");
                event.setLine(0, "");
                return;
            }

            player.sendMessage(ChatColor.GRAY + EBITIRI);
            event.setLine(2, EBITIRI);
        }
    }

    private void reloadConfigFile() {

        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        FileConfiguration config = getConfig();

        specialEventPercent = config.getInt("specialEventPercent", SPECIAL_EVENT_CHANCE);
        colorEvent = config.getInt("specialEvents.colorEvent", COLOR_CHANCE);
        bigEvent = config.getInt("specialEvents.bigEvent", BIG_CHANCE);
        moneyEvent = config.getInt("specialEvents.moneyEvent", MONEY_CHANCE);
        wordsEvent = config.getInt("specialEvents.wordsEvent", WORDS_CHANCE);
        thunderEvent = config.getInt("specialEvents.thunderEvent", THUNDER_CHANCE);
        explodeEvent = config.getInt("specialEvents.explodeEvent", EXPLODE_CHANCE);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getAction() == Action.LEFT_CLICK_BLOCK &&
                event.getClickedBlock().getState() instanceof Sign) {

            Sign sign = (Sign)event.getClickedBlock().getState();

            if (!sign.getLine(0).equalsIgnoreCase(SIGN_FIRST)) {
                return;
            }

            Random rand = new Random();
            int specialChance = rand.nextInt(100);

            if ( specialChance < specialEventPercent ) {
                // スペシャルイベント実行

                int kindChance = rand.nextInt(getTotalOfSpecialEvent());

                if ( kindChance < colorEvent ) {
                    // 色つきエビチリ
                    setEbiToRandomPos(sign, true);
                    return;
                }
                kindChance -= colorEvent;

                if ( kindChance < bigEvent ) {
                    // 大きなエビチリ
                    setBigEbiToRandomPos(sign);
                    return;
                }
                kindChance -= bigEvent;

                if ( kindChance < moneyEvent ) {
                    // お金をくれるエビチリ
                    setEbiToRandomPos(sign, false);
                    if ( econ != null ) {
                        Player player = event.getPlayer();
                        econ.depositPlayer(player.getName(), 100.0).transactionSuccess();
                        String unit = econ.currencyNamePlural();
                        player.sendMessage(ChatColor.RED + EBITIRI + " ＜ 100" + unit + "あげよう。");
                    }
                    return;
                }
                kindChance -= moneyEvent;

                if ( kindChance < wordsEvent ) {
                    // お言葉をくれるエビチリ
                    setEbiToRandomPos(sign, false);
                    int wordIndex = rand.nextInt(EBITIRI_WORDS.length);
                    event.getPlayer().sendMessage(EBITIRI_WORDS[wordIndex]);
                    return;
                }
                kindChance -= wordsEvent;

                if ( kindChance < thunderEvent ) {
                    // 雷を落とすエビチリ
                    setEbiToRandomPos(sign, false);
                    Player player = event.getPlayer();
                    player.getWorld().strikeLightning(player.getLocation());
                    player.sendMessage(ChatColor.RED + EBITIRI + "＃");
                    return;
                }
                kindChance -= thunderEvent;

                if ( kindChance < explodeEvent ) {
                    // 爆発するエビチリ
                    setEbiToRandomPos(sign, false);
                    Player player = event.getPlayer();
                    player.getWorld().createExplosion(player.getLocation(), 4F);
                    player.sendMessage(ChatColor.RED + EBITIRI + "＃");
                    return;
                }
                kindChance -= explodeEvent;

                // TODO: 他のスペシャルイベントをここに追加

                setEbiToRandomPos(sign, false);

                return;

            } else {
                // 通常イベント

                setEbiToRandomPos(sign, false);
                return;
            }

        }
    }

    private void clearSign(Sign sign) {
        sign.setLine(0, SIGN_FIRST);
        sign.setLine(1, "");
        sign.setLine(2, "");
        sign.setLine(3, "");
    }

    private String getRandomColor() {
        Random rand = new Random();
        int value = rand.nextInt(16);
        if ( value < 10 ) {
            return "\u00A7" + value;
        } else {
            char c = (char)('a' + value - 10);
            return "\u00A7" + c;
        }
    }

    private void setEbiToRandomPos(Sign sign, boolean randomColor) {

        clearSign(sign);

        Random rand = new Random();
        int line = rand.nextInt(3) + 1;
        int pos = rand.nextInt(11);
        StringBuilder buf = new StringBuilder();
        for ( int i=0; i<11; i++ ) {
            if ( i == pos ) {
                buf.append(EBITIRI);
            } else {
                buf.append(" ");
            }
        }

        if ( randomColor )
            sign.setLine(line, getRandomColor() + buf.toString());
        else
            sign.setLine(line, buf.toString());

        sign.update();
    }

    private void setBigEbiToRandomPos(Sign sign) {

        clearSign(sign);

        Random rand = new Random();
        int line = rand.nextInt(2) + 1;
        int pos = rand.nextInt(11);
        StringBuilder buf1 = new StringBuilder();
        StringBuilder buf2 = new StringBuilder();
        for ( int i=0; i<11; i++ ) {
            if ( i == pos ) {
                buf1.append("●　 ●");
                buf2.append("　○ 　");
            } else {
                buf1.append(" ");
                buf2.append(" ");
            }
        }

        sign.setLine(line,   buf1.toString());
        sign.setLine(line+1, buf2.toString());

        sign.update();
    }

    private int getTotalOfSpecialEvent() {

        int total = 0;
        total += colorEvent;
        total += bigEvent;
        total += moneyEvent;
        total += wordsEvent;
        total += thunderEvent;
        total += explodeEvent;
        return total;
    }
}
