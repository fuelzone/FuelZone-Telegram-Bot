import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MyBot extends TelegramLongPollingBot {
    String myChatId;
    String msg;

    @Override
    public String getBotUsername() {
        return TelegramBotUtils.USERNAME;
    }

    @Override
    public String getBotToken() {
        return TelegramBotUtils.TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            System.out.println(update.getMessage().getText());
            try {
                msg = getWord(update.getMessage().getText(), update.getMessage().getChatId().toString());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            myChatId = "" + update.getMessage().getChatId().toString();
            SendMessage sendMessage = new SendMessage(myChatId + "", msg);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public String getWord(String str, String uid) throws JsonProcessingException {
        str = str.replaceAll(" ", "%20");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.brainshop.ai/get?bid=162049&key=sEN6FttC8GopL3fG&uid=" + uid + "&msg=" + str))
                .header("x-rapidapi-host", "acobot-brainshop-ai-v1.p.rapidapi.com")
                .header("x-rapidapi-key", "sEN6FttC8GopL3fG")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Root root = new Gson().fromJson(response.body(), Root.class);
        System.out.println("reponse: " + response.body());
        return String.valueOf(root.cnt);
    }
}
