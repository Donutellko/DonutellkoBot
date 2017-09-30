package com.donutellko.bot;

import com.google.gson.Gson;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import static com.donutellko.bot.DonutellkoBot.donutellkoBot;

public class Main {
	static Config config = new Config();

	public static void main (String[] args) {

		String configJson = FileWorker.readFromFile(Config.getConfigPath());
		if (configJson != null) {
			config = new Gson().fromJson(configJson, Config.class);
			FileWorker.writeToFile(Config.getConfigPath(), new Gson().toJson(config));
		}

		String dataJson = FileWorker.readFromFile(config.getDataPath());
		if (configJson != null) {
			donutellkoBot.createBotsFromJson(dataJson);
		}

		ApiContextInitializer.init();

		// Instantiate Telegram Bots API
		TelegramBotsApi botsApi = new TelegramBotsApi();
		
		// Register our bot
		try {
			botsApi.registerBot(new DonutellkoBot());
		} catch (TelegramApiException e) {
			errorLog(e.getStackTrace());
		}

		ShutdownHook shutdownHook = new ShutdownHook();

		Runtime.getRuntime().addShutdownHook(shutdownHook);
	}

	static void errorLog(String s) {
		FileWorker.addToFile(config.getErrorLogPath(),"\n" + s + "\n");
	}

	private static void errorLog(StackTraceElement[] stackTrace) {
		errorLog(stackTraceToString(stackTrace));
	}

	static String stackTraceToString(StackTraceElement[] stackTrace) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement st : stackTrace)
			sb.append(st).append("\n");
		return  sb.toString();
	}
}
