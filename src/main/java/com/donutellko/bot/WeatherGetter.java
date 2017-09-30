package com.donutellko.bot;

import com.google.gson.Gson;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.donutellko.bot.DataGetter.getDataFromUrl;
import static com.donutellko.bot.Main.config;

public class WeatherGetter extends Thread {
	static ResponseObject currentInfo = null;
	static String currentInfoString = "";

	public WeatherGetter(String s) { }

	@Override
	public void run() {
		refresh();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				refresh();

			}
		}, 30 * 60 * 1000L);
	}

	private void refresh () {
		try {
			String response = getDataFromUrl(config.getWeatherUrl());
			ResponseObject weathers = new Gson().fromJson(response, ResponseObject.class);
			WeatherGetter.currentInfo = weathers;
			WeatherGetter.currentInfoString = WeatherGetter.getToday(weathers);
			System.out.println("Weather updated!\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getToday() {
		return getToday(currentInfo);
	}

	static String getToday(ResponseObject currentInfo) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			if (i >= currentInfo.list.length)
				break;
			ResponseObject.WeatherObject cur = currentInfo.list[i];
			int temp = (int) Math.round(cur.main.temp);
			int hum = (int) (cur.main.humidity + 0.5); // гениальный способ округления. Учитесь!
			result
					.append("\n ").append(cur.getTime()).append("    ")
					.append((temp < 10 ? "  " : "") + temp).append("°C   ")
					.append(hum).append("%  " + (hum == 100 ? "" : "  "))
					.append(cur.weather[0].description);
		}
		if (result.length() > 0)
			result.insert(0, "Расклад в СПб на ближайшие сутки такой:\n<b>Время  Темп.  Влажн.</b>");
		return result.toString();
	}

	class ResponseObject {
		WeatherObject[] list;

		class WeatherObject {
			String dt_txt;
			Main main;
			Weather[] weather;
			Clouds clouds;

			String getTime() {
				return dt_txt.substring(11, 16);
			}

			public Date getDate() {
				int year = Integer.parseInt(dt_txt.substring(0, 3));
				int month = Integer.parseInt(dt_txt.substring(5, 6));
				int day = Integer.parseInt(dt_txt.substring(8, 9));
				int hour = Integer.parseInt(dt_txt.substring(11, 12));
				return new Date(year, month, day, hour, 0);
			}

			class Main {
				double temp, pressure, humidity;
			}

			class Weather {
				String description;
			}

			class Clouds {
				String all;
			}
		}
	}
}