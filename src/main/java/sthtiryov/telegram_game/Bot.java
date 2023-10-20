package sthtiryov.telegram_game;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot {
    private final TelegramBot bot = new TelegramBot(System.getenv("BOT_TOKEN"));
    private final String PROCESSING_LABEL = "...";
    private final static List<String> opponentWins = new ArrayList<String>() {{
        add("01");
        add("12");
        add("20");
    }};
    private final static Map<String, String> items = new HashMap<String, String>() {{
        put("0", "🖐");
        put("1", "✌");
        put("2", "👊");
    }};

    public void serve() {


        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);

            return UpdatesListener.CONFIRMED_UPDATES_ALL;


        });
    }


    private void process(Update update) {
        Message message = update.message();
        CallbackQuery callbackQuery = update.callbackQuery();
        InlineQuery inlineQuery = update.inlineQuery();
        BaseRequest request = null;
        if (message != null && message.viaBot() != null && message.viaBot().username().equals("gameShtiryovBot")) {
            InlineKeyboardMarkup replyMarkup = message.replyMarkup();
            if (replyMarkup == null) {
                return;
            }
            InlineKeyboardButton[][] buttons = replyMarkup.inlineKeyboard();
            if (buttons == null) {
                return;
            }
            InlineKeyboardButton button = buttons[0][0];
            String buttonLabel = button.text();
            if (!buttonLabel.equals(PROCESSING_LABEL)) {
                return;
            }
            Long chatId = message.chat().id();
            String senderName = message.from().firstName();
            String senderChose = button.callbackData();
            Integer messageId = message.messageId();
            request = new EditMessageText(chatId, messageId, message.text())
                    .replyMarkup(new InlineKeyboardMarkup(
                            new InlineKeyboardButton("🖐").callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "0", messageId)),
                            new InlineKeyboardButton("✌").callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "1", messageId)),
                            new InlineKeyboardButton("👊").callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "2", messageId))));
        } else if (inlineQuery != null) {
            InlineQueryResultArticle paper = buildInlineButton("paper", "\uD83D\uDD90 Бумага", "0");

            InlineQueryResultArticle scissors = buildInlineButton("scissors", "✌ Ножницы", "1");

            InlineQueryResultArticle stone = buildInlineButton("stone", " \uD83D\uDC4A Камень", "2");

            request = new AnswerInlineQuery(inlineQuery.id(), paper, scissors, stone).cacheTime(1);
        } else if (callbackQuery != null) {
            String[] data = callbackQuery.data().split(" ");
            if (data.length < 4) {
                return;
            }
            Long chatId = Long.parseLong(data[0]);
            String senderName = data[1];
            String senderChose = data[2];
            String opponentChose = data[3];
            int messageId = Integer.parseInt(data[4]);
            String opponentName = callbackQuery.from().firstName();
            if (senderChose.equals(opponentChose)) {
                request = new EditMessageText(chatId, messageId, String.format("%s и %s выбрали %s. Победила Дружба =)", senderName, opponentName, items.get(senderChose)));
            } else if (opponentWins.contains(senderChose + opponentChose)) {
                request = new EditMessageText(chatId, messageId, String.format("%s выбрал %s и получил люлей  от %s,выбравшего %s", senderName, items.get(senderChose), opponentName, items.get(opponentChose)));
            } else {
                request = new EditMessageText(chatId, messageId, String.format("%s выбрал %s и получил люлей  от %s,выбравшего %s", opponentName, items.get(opponentChose), senderName, items.get(senderChose)));
            }
        }

        if (request != null) {
            bot.execute(request);

        }

    }

    private InlineQueryResultArticle buildInlineButton(String id, String title, String callbackData) {
        return new InlineQueryResultArticle(id, title, "Я готов!")
                .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton(PROCESSING_LABEL).callbackData(callbackData)));
    }
}


