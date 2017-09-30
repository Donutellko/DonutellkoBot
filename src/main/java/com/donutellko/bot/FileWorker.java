package com.donutellko.bot;

import java.io.*;
import java.nio.file.Path;

class FileWorker {

	static String readFromFile(String path) {
		File file = new File(path);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			StringBuilder result = new StringBuilder();

			try {
				String line;
				do {
					line = reader.readLine();
					result.append(line);
				} while (line != null);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Successfully read from " + file.getAbsolutePath());
			return result.toString();
		} catch (FileNotFoundException e) {
			Main.errorLog("No such file: " + file.getAbsolutePath());
//			e.printStackTrace();
			return null;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	static void writeToFile(String path, String text) {
		File file = new File(path);

		createFileIfAbsent(file);

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(file));
			writer.write(text);
			writer.println(text);
			Main.errorLog("Saved to " + file.getAbsolutePath());
		} catch (IOException e) {
			Main.errorLog("Unable to save to " + file.getAbsolutePath() + "\n" + Main.stackTraceToString(e.getStackTrace()));
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	static void addToFile(String path, String text) {
		File file = new File(path);

		createFileIfAbsent(file);
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(file, true)); // boolean: append mode.
			writer.append(text);
			writer.println(text);
			System.out.println("Added to " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Unable to add to " + file.getAbsolutePath() + "\n" + Main.stackTraceToString(e.getStackTrace()));
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	private static boolean dirExists(File file) {
		return new File(file.getPath()).exists();
	}

	private static void createFileIfAbsent(File file) {
		if (!dirExists(file)) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
//				Main.errorLog(e.getStackTrace()); /// Вызовет StackOverflow!!!
				System.out.println(e.getMessage() + " " + file.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}
}
