package com.donutellko.bot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Created by donat on 9/5/17.
 */
public class Main {
    public static void main (String[] args) {

        ApiContextInitializer.init();

        // Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // Register our bot
        try {
            botsApi.registerBot(new DonutellkoBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
