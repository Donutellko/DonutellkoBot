import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Created by donat on 8/28/17.
 */

public class DonutellkoBot extends TelegramLongPollingBot {
    public static TelegramLongPollingBot donutellkoBot;

    public DonutellkoBot() {
        super();
        donutellkoBot = this;
    }

    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            SendMessage message = new SendMessage() // Create a message object object
                    .setChatId(chat_id)
                    .setText(message_text);
            try {
                sendMessage(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public String getBotUsername() {
        return "DonutellkoBot";
    }

    @Override
    public String getBotToken() {
        return "424429240:AAHSc651dDwaUM_hbhiFjtG5jga9Ya7PFKg";
    }
}