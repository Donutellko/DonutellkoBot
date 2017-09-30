package com.donutellko.bot;

import static com.donutellko.bot.Main.config;

public class Config {
	private static String configPath = "data/config.json";
	private String dataPath = "data/data.json";
	private String errorLogPath = "data/error.log";

	private String botName = "DonutellkoBot";
	private String weatherUrl = "http://api.openweathermap.org/data/2.5/forecast?id=519690&appid=484811a1b7ad9193b884eb1396f726d1&units=metric&lang=ru";
	private String botToken = "424429240:AAHSc651dDwaUM_hbhiFjtG5jga9Ya7PFKg";

	private long logChatId = -210985019;
	long adminId = 315210300;
	String adminPassword = "nyaka";


	Config () { }

	String getBotToken() {
		return botToken;
	}

	public void setLogChat(long logChatId) {
		config.logChatId = logChatId;
	}

	long getLogChatId() {
		return logChatId;
	}

	static String getConfigPath() {
		return configPath;
	}

	String getBotName() {
		return botName;
	}

	String getWeatherUrl() {
		return weatherUrl;
	}

	String getDataPath() {
		return dataPath;
	}

	String getErrorLogPath() {
		return errorLogPath;
	}
}
