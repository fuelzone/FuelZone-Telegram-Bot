import com.google.gson.*;
import dtos.RequestDto;
import dtos.ResponseDto;
import dtos.UserDto;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyBot extends TelegramLongPollingBot {
    HashMap<String, Integer> condition = new HashMap<>();
    HashMap<String, String> fuelTypes = new HashMap<>();

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
        if (update.hasCallbackQuery())
            System.out.println("Callback: " + update.getCallbackQuery().toString());
        String myChatId = "" + update.getMessage().getChatId().toString();
        if(update.getMessage().hasText() && update.getMessage().getText().equals("/start")){
            System.out.println("starting...");
            UserDto userDto = new UserDto();
            userDto.setChatId(myChatId);
            if (update.getMessage().getFrom().getUserName() != null){
                userDto.setUsername(update.getMessage().getFrom().getUserName());
            }
            if (update.getMessage().getFrom().getFirstName() != null){
                userDto.setFirstName(update.getMessage().getFrom().getFirstName());
            }
            if (update.getMessage().getFrom().getLastName() != null){
                userDto.setLastName(update.getMessage().getFrom().getLastName());
            }
            try {
                register(userDto);
                condition.put(myChatId, 1);
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("Choose below!");
                sendMessage.setChatId(myChatId);
                sendMessage.setReplyMarkup(showMenu());
                execute(sendMessage);
            } catch (Exception e) {
                System.out.println(e);
            }
        }else if (update.getMessage().hasText() && update.getMessage().getText().equals("Back")){
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(myChatId);
            sendMessage.setText("Choose below!");
            sendMessage.setReplyMarkup(showMenu());
            try {
                condition.put(myChatId, 1);
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }else if(update.hasMessage() && condition.get(myChatId) == 1){
            String data = update.getMessage().getText();
            if (data.contains("Statistics")){
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("Sending report here!");
                sendMessage.setChatId(myChatId);
                sendMessage.setReplyMarkup(showMenu());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                }
            }else if(data.contains("Fuel")){
                SendMessage sendMessage = new SendMessage();
                sendMessage.setReplyMarkup(fuelMenu());
                sendMessage.setText("Choose fuel type!");
                sendMessage.setChatId(myChatId);
                try {
                    condition.put(myChatId, 2);
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                }
            }
        }else if (update.hasMessage() && condition.get(myChatId) == 2){
            String data = update.getMessage().getText();
            if (data.contains("i80")){
                fuelTypes.put(myChatId, "80");
                try {
                    sendLocation(myChatId);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                }
            }else if(data.contains("i90")){
                fuelTypes.put(myChatId, "90");
                try {
                    sendLocation(myChatId);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                }
            }else if(data.contains("i95")){
                fuelTypes.put(myChatId, "95");
                try {
                    sendLocation(myChatId);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                }
            }else if (data.contains("Back")){
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(myChatId);
                sendMessage.setText("Choose below!");
                sendMessage.setReplyMarkup(showMenu());
                try {
                    condition.put(myChatId, 1);
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }else if (update.getMessage().hasLocation() && condition.get(myChatId) == 3) {
            System.out.println("Sending location!");
            SendMessage msg = new SendMessage();
            msg.setChatId(update.getMessage().getChatId());
            RequestDto requestDto = new RequestDto();
            requestDto.setFuelType(fuelTypes.get(myChatId));
            if (update.getMessage().hasLocation()) {
                Double latitude = update.getMessage().getLocation().getLatitude();
                Double longitude = update.getMessage().getLocation().getLongitude();
                requestDto.setLatitude(latitude);
                requestDto.setLongitude(longitude);

                try {
                    HttpEntity content = sendRequest(requestDto);
                    Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                        @Override
                        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                            Instant instant = Instant.ofEpochMilli(json.getAsJsonPrimitive().getAsLong());
                            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                        }
                    }).create();
                    ResponseDto res = gson.fromJson(EntityUtils.toString(content), ResponseDto.class);
                    SendLocation sendLocation = new SendLocation();
                    sendLocation.setChatId(myChatId);
                    if(res.isFound()) {
                        sendLocation.setLatitude(res.getLatitude());
                        sendLocation.setLongitude(res.getLongitude());
                        execute(sendLocation);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(myChatId);
                        String openH = res.getOpenDate().substring(11, 16);
                        String closeH = res.getCloseDate().substring(11, 16);
                        String lastUpdated = res.getLastUpdated().substring(0, 10);
                        sendMessage.setText("Station: " + res.getName() + "\nOpen time: " + openH + " - " + closeH + "\nLast updated: " + lastUpdated + "\nFuel type: " + res.getFuelType() + "\nFuel price: " + res.getPrice() + "\nAvailable: " + res.getAmount());
                        sendMessage.setReplyMarkup(backMenu());
                        execute(sendMessage);
                    }else{
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(myChatId);
                        sendMessage.setText(requestDto.getFuelType() + " fuel type is not available!");
                        condition.put(myChatId, 1);
                        sendMessage.setReplyMarkup(showMenu());
                        execute(sendMessage);
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        System.out.println("Conditions: " + condition.toString());
        System.out.println("fueltypes: " + fuelTypes.toString());
    }

    private void sendLocation(String chatId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Send your location");
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(backMenuLocation());
        condition.put(chatId, 3);
        execute(sendMessage);
    }

    private ReplyKeyboardMarkup backMenuLocation(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton btn = new KeyboardButton();
        btn.setRequestLocation(true);
        btn.setText("Send location");
        row.add("Back");
        KeyboardRow row2 = new KeyboardRow();
        row2.add(btn);
        keyboardRows.add(row2);
        keyboardRows.add(row);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup backMenu(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Back");
        keyboardRows.add(row);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup fuelMenu(){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("i80");
        row.add("i90");
        row.add("i95");
        keyboard.add(row);
        KeyboardRow row2 = new KeyboardRow();
        row2.add("Back");
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
//        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
//        List<InlineKeyboardButton> row = new ArrayList<>();
//        row.add(createButtonWIthCallBackData("i80", "i80"));
//        row.add(createButtonWIthCallBackData("i90", "i90"));
//        row.add(createButtonWIthCallBackData("i95", "i95"));
//        keyboard.add(row);
//        keyboardMarkup.setKeyboard(keyboard);
//        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup showMenu(){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("Statistics");
        row.add("Fuel");
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    private static InlineKeyboardButton createButtonWIthCallBackData(String text, String condition){
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(condition);
        return btn;
    }

    public HttpEntity sendRequest(RequestDto request) throws Exception {
        String postUrl = "http://localhost:8080/fuelApp/v1/request";
        Gson gson = new Gson();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(gson.toJson(request));
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        return httpClient.execute(post).getEntity();
    }

    public void register(UserDto dto) throws Exception{
        String postUrl = "http://localhost:8080/fuelApp/v1/createUser";
        Gson gson = new Gson();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(gson.toJson(dto));
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        System.out.println(httpClient.execute(post).getEntity().getContent().read());
    }
}
