package com.donutellko.bot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.donutellko.bot.DataGetter.getDataFromUrl;

class TimetableSender implements Runnable { // При успешном получении расписания передаёт userBot'у и посылает пользователю
	long chat_id;
	int group_id;
	TimetableType type;

	enum TimetableType { FULL_WEEK, NEXT_DAY }

	@Deprecated
	public TimetableSender (long chat_id, int group_id) {
		this.chat_id = chat_id;
		this.group_id = group_id;
		this.type = TimetableType.FULL_WEEK;
	}

	public TimetableSender (long chat_id, int group_id, TimetableType type) {
		this.chat_id = chat_id;
		this.group_id = group_id;
		this.type = type;
	}

	public void run() {
		try {
			String result  = getDataFromUrl("http://ruz.spbstu.ru/faculty/95/groups/" + group_id + "/ical");
			try {
				result = icalToText(result);
			} catch (Exception e) {
				System.out.println("ОШИБКА РАСПИСАНИЯ!" + result);
				e.printStackTrace();
				result = "Ошибка при формировании результата. Перешли это сообщение мне (@Donutellko).\n"
						+ e.toString() + "\n" + e.getStackTrace()[0].toString() + "\nGroupId = " + group_id;
			}

			DonutellkoBot.donutellkoBot.sendMsg(chat_id, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static String icalToText(String ical) {
		int tmp;
		char newline = ical.charAt(15);

		String[] vevents = ical.split("BEGIN:VEVENT"); // разделение на блоки (нулевой -- заголовок)
		Subject[] subjects = new Subject[vevents.length - 1]; // кроме нулевого

		tmp = vevents[0].indexOf("Расписание");
		String groupN = vevents[0].substring(tmp, vevents[0].indexOf(newline, tmp)); // Получение номера группы

		for (int i = 0; i < vevents.length - 1; i++) {
			subjects[i] = new Subject(vevents[i + 1]);
		}

		String result = groupN + " на эту неделю.";
		Week week = new Week(subjects);
		result += week.toString();
		return result.toString();
	}

	static class Week {
		List<Day> days = new ArrayList<>();

		public Week (Subject[] subjects) {
			Day cur_day = null;
			for (Subject subj : subjects) {
				if (cur_day == null) { // если нет составляемого дня
					cur_day = new Day(subj);
					days.add(cur_day);
				} else if (cur_day.isSameDay(subj)) { // если всё
					cur_day.add(subj);
				} else {
					days.add(cur_day);
					cur_day = new Day(subj);
				}
			}
		}

		@Override
		public String toString() {
			String s = "";
			for (Day day : days)
				s += "\n" + day.toString();
			return s;
		}

		class Day {
			String date;
			List<Subject> subjects = new ArrayList<>();
			public Day (Subject subj) {
				subjects.add(subj);
				date = subj.DTSTART.substring(0, 8);
			}

			public boolean isSameDay(Subject subj2) {
				return subj2.DTSTART.substring(0, 8).equals(date);
			}

			public void add(Subject subj) {
				subjects.add(subj);
			}

			@Override
			public String toString() {
				Subject.Time t;
				String s = "\n" +(new Subject.Time(date + "00000000")).toString();
				for (Subject subject : subjects)
					s += "\n" + subject.toString();
				return s;
			}
		}
	}

	static class Subject {
		String DTSTART, DTEND, SUMMARY, LOCATION;
		Time start, end;

		public Subject(String vevent) {
			int tmp;
			tmp = vevent.indexOf("SUMMARY:") + ("SUMMARY:").length();
			SUMMARY = vevent.substring(tmp, vevent.indexOf('\n', tmp));
			tmp = vevent.indexOf("LOCATION:") + ("LOCATION:").length();
			LOCATION = vevent.substring(tmp, vevent.indexOf('\n', tmp));
			tmp = vevent.indexOf("DTSTART:") + ("DTSTART:").length();
			DTSTART = vevent.substring(tmp, vevent.indexOf('\n', tmp));
			tmp = vevent.indexOf("DTEND:") + ("DTEND:").length();
			DTEND = vevent.substring(tmp, vevent.indexOf('\n', tmp));

			start = new Time(DTSTART);
			end = new Time(DTEND);
		}

		public String getTimeRange() {
			return start.getHourMinute() + "-" + end.getHourMinute();
		}

		@Override
		public String toString() {
			return "<b>" + getTimeRange() + "</b> " + SUMMARY + " (" + LOCATION + ") ";
		}

		static class Time {
			String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};
			// String[] weekdays = {"???", "Понедельник", "Вторник", "Среда", "Четверг", " Пятница", "Суббота", "Воскресенье", };

			int year, month, day, hour, min;
			String weekday;

			public Time(String s) {

				year  = Integer.parseInt(s.substring(0, 4));
				month = Integer.parseInt(s.substring(4, 6)) - 1;
				day   = Integer.parseInt(s.substring(6, 8));
				hour  = Integer.parseInt(s.substring(9, 11)) + 2;
				min   = Integer.parseInt(s.substring(11, 13));

				Calendar c = Calendar.getInstance();
				c.set(year, month, day); // TODO: ПРОБЛЕМА!!!!!!!
				int d = c.get(Calendar.DAY_OF_WEEK);
				weekday = new SimpleDateFormat("EEEE", new Locale("ru")).format(c.getTime());
			}

			@Override
			public String toString() {
				return day + " " + months[month] + ", " + weekday;
			}

			public String getHourMinute() {
				return (hour < 10 ? "0" : "") + hour + ":" + (min < 10 ? "0" : "") + min;
			}
		}
	}
}