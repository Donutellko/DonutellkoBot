package com.donutellko.bot;

import static com.donutellko.bot.DonutellkoBot.donutellkoBot;

public class ShutdownHook extends Thread {
	@Override
	public void run() {
		if (donutellkoBot != null) {
			donutellkoBot.sendLog("Shutting down.");
			donutellkoBot.saveUserBots();
		}
	}
}
