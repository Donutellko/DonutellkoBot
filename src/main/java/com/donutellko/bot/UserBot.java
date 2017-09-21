package com.donutellko.bot;

import com.google.gson.Gson;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Update;

import static com.donutellko.bot.DonutellkoBot.Request;
import static com.donutellko.bot.DonutellkoBot.RequestType;
import static com.donutellko.bot.DonutellkoBot.RequestType.*;
import static com.donutellko.bot.DonutellkoBot.donutellkoBot;

/**
 * Это объект, отвечающий за хранение данных о диалоге с ботом в каждом чате.
 */

public class UserBot {
	Long chatId;
	String name;
	boolean isPrivate;
	int groupId = 0;
	RequestType currentRequest;
	StringBuilder dialog = new StringBuilder();
	public String username;

	UserBot(Update upd) {
		Chat chat = upd.getMessage().getChat();
		this.chatId = chat.getId();
		this.isPrivate = chat.isUserChat();
		this.name = isPrivate ?
				chat.getFirstName() : chat.getTitle();
		this.username = chat.getUserName();

		dialog.append("Started chat ").append(isPrivate ? "with " : "in ").append(name);
	}

	private void sendMsg(String text) {
		donutellkoBot.sendMsg(chatId, text);
	}

	private void sendMsg(String text, boolean reply) {
		donutellkoBot.sendMsg(chatId, text, reply);
	}

	void process(Update upd) {
		System.out.println("Processing update: " + upd.getMessage().getFrom().getUserName() + ": " + upd.getMessage().getText());
		Request request = new Request(upd.getMessage());

		String forLog = upd.getMessage().getFrom().getFirstName() + ": " + upd.getMessage().getText();
		answer(request);
	}

	private void answer(Request request) {
		if (request.isEmpty())
			return;

		if (request.isCommand())
			switch (request.type) {
				case START:          sendMsg("Привет, " + name + "! Отправь /help, чтобы увидеть список команд."); break;
				case HELP:           sendMsg(getHelp(request));  break;
				case ECHO:           sendMsg("Я тут. Привет, " + name + "."); break;
				case TIMETABLE_NEXT: sendToday(); break;
				case TIMETABLE_WEEK: sendWeek(); break;
				case WEATHER:        sendWeather(); break;
				case GROUPINFO:      sendGroupinfo(request); break;
				case SUBSCRIBE:      subscribe(request); break;
				case UNSUBSCRIBE:    unsubscribe(request); break;
				case RESET:          reset(); break;
				case STOP:           currentRequest = null; sendMsg("Ok."); break;
				case UNKNOWN:
				default:             sendMsg("Шо это ты мне послал?\nПрочитай /help и не делай так больше!");
			}
		else
			questionAnswer(request.text);
	}

	private void sendGroupinfo(Request request) {
		if (request.text.trim().matches("[0-9]+/+[0-9]+")) {
			String info = "";
			info += request.text.charAt(0) + " курс" + (request.text.charAt(3) == '4' ? " магистратуры." : " бакалавриата." ) + "\n";
			switch ((request.text.charAt(1) - '0') * 10 + (request.text.charAt(2) - '0')) {
				case 35: info += "ИКНТ\n";
				switch (request.text.charAt(4) - '0') {
					case 1: info += "КСПТ"; break;
					case 6: info += "Кафедра КИТ"; break;
					case 8: info += "ибкс изи"; break;
					default: info += "хз какая кафедра"; break;
				} break;
				default: info += "Какой-то дурацкий факультет"; break;
			}

			sendMsg(info);
		} else
			sendMsg("Ошибка в номере группы");
	}

	private void reset() {
		DonutellkoBot.ids.remove(this);
		DonutellkoBot.userBots.remove(this);
		DonutellkoBot.userBots.remove(chatId);
		sendMsg("Ты удалён из базы.");
		System.out.println("Removed " + name + "\n");
	}


	private void subscribe(Request request) {
		if (request.hasText()) {
			// TODO:
		} else {
			sendMsg(getHelp(SUBSCRIBE));
		}
	}
	private void unsubscribe(Request request) {
		// TODO:
	}


	private void sendToday() {
		sendMsg("Нет инфы. Зайди потом.");
	}

	private void sendWeather() {
		if (WeatherGetter.currentInfoString.length() > 0)
			sendMsg(WeatherGetter.currentInfoString);
		else
			sendMsg("С вероятностью в 99.(9)% сегодня пойдёт дождь. А к сервису погоды чего-то не подключиться... Смыло штоле?");
	}

	private void sendWeek() {
		if (groupId > 0) {
			new TimetableSender(chatId, groupId).run();
		} else {
			currentRequest = TIMETABLE_WEEK;
			sendMsg("Назови id своей группы на сайте расписаний: ruz.spbstu.ru", true);
		}
	}

	private static String getHelp(Request request) {
		if (request.hasText())
			return getHelp(Request.fromString("/" + request.text));
		else
			return getHelp();
	}

	private static String getHelp() {
		return  "Справка."
				+ '\n' + "/echo   \tПроверить, что бот онлайн"
				+ '\n' + "/next   \tРасписание на ближайший рабочий день"
				+ '\n' + "/week   \tРасписание на эту неделю"
				+ '\n' + "/weather\tСводка погоды на сегодня"
				+ '\n' + "/group  \tУзнать информацию о группе по номеру"
				+ '\n' + "/stop   \tОтменить текущий реквест"
				+ '\n' + "/sub    \tПодписать на уведомления (см. /help sub)"
				+ '\n' + "/unsub  \tОтписаться от уведомления"
				+ '\n' + "/reset  \tЗабыть всё, что бот о тебе знает"
				+ '\n' + "/help   \tПоказать эту справку"
				+ '\n' + "Вызови '/help название_команды', чтобы получить более подробную справку по ней.";
	}

	private static String getHelp(RequestType requestType) {
		switch (requestType) {
			case WEATHER:   return "По команде /weather отправляется прогноз погоды в СПб на сутки с периодичностью в 3 часа. Информация берётся с openweathermap.org";
			case SUBSCRIBE: return "Напиши одну из команд ниже, чтобы периодически получать уведомления:" +
					"\n/sub weather ежедневный прогноз погоды в СПб на сутки" +
					"\n/sub week расписание на следующую неделю по воскресеньям" +
					"\n/sub next ежевечернее расписание на следующий день" +
					"\n/sub dl name:pass чтобы мгновенно получать новые посты со всех форумов на dl.spbstu.ru";
			case UNSUBSCRIBE: return "Напиши одну из команд ниже, чтобы перестать получать:" +
					"\n/unsub weather ежедневный прогноз погоды в СПб на сутки" +
					"\n/unsub week расписание на следующую неделю" +
					"\n/unsub next ежевечернее расписание на следующий день" +
					"\n/unsub dl новые посты со всех форумов на dl.spbstu.ru";
			case GROUPINFO: return "/group 12345/6 узнать доступную информацию об этой группе";
			case STOP: return "Отменяет текущий	запрос";
			case RESET: return "Отписывает тебя от всех уведомлений и удаляет информацию об этой переписке.";
			case TIMETABLE_NEXT: return "Расписание на ближайший рабочий день";
			case TIMETABLE_WEEK: return "Расписание на эту неделю";
			case ECHO: return "Проверить, что бот онлайн";
			case HELP: return "Не шали тут";
			default: return "Ошибка. Такой команды нет.";
		}
	}

	public void questionAnswer(String answer) {
		if (currentRequest == TIMETABLE_NEXT || currentRequest == TIMETABLE_WEEK) {
			if (answer.matches("[0-9]+")) {
				groupId = Integer.parseInt(answer);
				sendWeek();
			} else
				sendMsg("Неверный формат. Пришли мне одно число или \'/stop\' чтоб отменить.");
		} else
			sendMsg("Не понял тебя...");
	}

	public String getJson() {
		return new Gson().toJson(this);
	}

	public static UserBot getUserBot(String json) {
		return new Gson().fromJson(json, UserBot.class);
	}
}