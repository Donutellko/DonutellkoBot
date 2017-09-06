package com.donutellko.bot;

import com.google.gson.Gson;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.*;

/**
 * Created by donat on 8/28/17.
 */

public class DonutellkoBot extends TelegramLongPollingBot {
	public static DonutellkoBot donutellkoBot;
	static String weatherUrl = "http://api.openweathermap.org/data/2.5/forecast?id=519690&appid=484811a1b7ad9193b884eb1396f726d1&units=metric&lang=ru";
	static String myName = "DonutellkoBot";

	static Map<Long, UserBot> userBots = new HashMap<>();
	static List<Long> ids = new LinkedList<>();

	enum RequestType {START, ECHO, WEATHER, TIMETABLE_NEXT, TIMETABLE_WEEK, SUBSCRIBE, UNSUBSCRIBE, GROUPINFO, STOP, HELP, RESET, UNKNOWN, ADMINISTRATING}

	public DonutellkoBot() {
		super();
		donutellkoBot = this;

		new WeatherGetter("Weather getter thread.").start();
	}

	public void onUpdateReceived(Update upd) {
		if (!upd.hasMessage()) return;

		Message msg = upd.getMessage();
		Long id = msg.getChatId();

		UserBot userBot;
		if (! userBots.containsKey(id)) {
			userBot = new UserBot(upd);
			userBots.put(id, userBot);
			ids.add(id);
		} else
			userBot = userBots.get(id);

		userBot.process(upd);
	}

	public String getBotUsername() {
		return "DonutellkoBot";
	}

	@Override
	public String getBotToken() {
		return "424429240:AAHSc651dDwaUM_hbhiFjtG5jga9Ya7PFKg";
	}

	void sendMsg(Long chat_id, String text) {
		sendMsg(chat_id, text, false);
	}

	void sendMsg(Long chat_id, String text, boolean reply) {
		SendMessage message = new SendMessage() // Create a message object object
				.setChatId(chat_id)
				.setText(text)
				.enableHtml(true)
				;
		if (reply)
			message.setReplyMarkup(new ForceReplyKeyboard());
		try {
			sendMessage(message); // Sending our message object to user
		} catch (TelegramApiException e) {
			System.out.println(text);
			e.printStackTrace();
		}
	}

	String getBotsJson () {
		UserBot[] botsArr = (UserBot[]) userBots.values().toArray();
		return new Gson().toJson(botsArr);
	}

	static class Request {
		Long chatId;
		Message message;
		RequestType type;
		String text;
		UserBot user;

		Request(Message message) {
			this.message = message;
			chatId = message.getChat().getId();
			user = userBots.get(chatId);

			String msg = message.getText().trim();
			if (msg.length() > 0 && msg.charAt(0) == '/') { // если первое слово -- команда
				String command = msg.replace("@" + DonutellkoBot.myName, "");
				if (command.indexOf(' ') > 0) {
					command = command.substring(0, command.indexOf(' '));
					text = msg.substring(msg.indexOf(' ')).trim();
				}
				type = Request.fromString(command);
			} else // если нет команды
				text = msg;
		}

		public boolean isCommand() {
			return type != null;
		}

		boolean hasText() {
			return text != null;
		}

		boolean isEmpty() {
			return text == null && type == null;
		}

		static RequestType fromString(String command) {
			switch (command) {
				case "/help":
					return RequestType.HELP; // Справка
				case "/start":
					return RequestType.START; // В начале общения с ботом
				case "/echo":
					return RequestType.ECHO; // Пингование. Проверить, что бот онлайн
				case "/next":
					return RequestType.TIMETABLE_NEXT; // Расписание на сегодня
				case "/week":
					return RequestType.TIMETABLE_WEEK; // Расписание на эту неделю
				case "/weather":
					return RequestType.WEATHER; // Сводка погоды на сегодня
				case "/group":
					return RequestType.GROUPINFO; // Узнать информацию о группе по номеру
				case "/sub":
					return RequestType.SUBSCRIBE; // Подписать на уведомления (ежедневная сводка погоды/расписание на день/посты на dl'ке)
				case "/unsub":
					return RequestType.UNSUBSCRIBE; // Отписаться от уведомления
				case "/reset":
					return RequestType.RESET; // Забыть всё, что бот о тебе знает
				case "/stop":
					return RequestType.STOP; // Отменить текущий реквест
				case "/admin":
					return RequestType.ADMINISTRATING; // Команды для администрирования
				default:
					return RequestType.UNKNOWN; // Неизвестная команда

			}
		}
	}

	static class Administration {
		// команды, используемые при админстве ботом.
		public static void sendJson(long logchat_id) {
			donutellkoBot.sendMsg(logchat_id, donutellkoBot.getBotsJson());
		}

		public static void removeUser(long logchat_id, String username) {
			for (UserBot ub : userBots.values())
				if (ub.username.equals(username)) {
					userBots.remove(ub);
					donutellkoBot.sendMsg(logchat_id, username + " удалён из базы.");
				}
			donutellkoBot.sendMsg(logchat_id, "Пользователь " + username + " не найден в базе.");
		}

		public static void shutdown(long logchat_id) {
			donutellkoBot.sendMsg(logchat_id, "Shutting down.");
			System.exit(0);
		}
	}
}