package pro.sky.petshelterbot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.petshelterbot.handler.*;
import pro.sky.petshelterbot.handler.ShelterHandler;


import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotListener implements UpdatesListener {

    final private Logger logger = LoggerFactory.getLogger(TelegramBotListener.class);
    final private TelegramBot telegramBot;

    final private Handler[] handlers;
    private final ShelterHandler shelterHandler;


    public TelegramBotListener(TelegramBot telegramBot,
                               VolunteerHandler volunteerHandler,
                               DevStageDBHandler catsDevStageHandler,
                               StartHandler startHandler,
                               ShelterHandler shelterHandler) {
        this.telegramBot = telegramBot;

        handlers = new Handler[]{
                startHandler,
                volunteerHandler,
                catsDevStageHandler
        };
        this.shelterHandler = shelterHandler;

    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.debug("Processing update: {}", update);
                if (update.message() != null) {
                    processMessage(update.message());
                } else if (update.callbackQuery().data() != null) {
                    shelterHandler.processCallbackQuery(update.callbackQuery());
                }
            });
        } catch (Exception e) {
            logger.error("Exception` e={} was caught", e, e);
            e.printStackTrace();
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processMessage(Message message) {
        logger.debug("Processing update message: {}", message);
        if (message.text() == null) {
            throw new IllegalArgumentException("Message.text() is null");
        }
        for (Handler handler : handlers) {
            if (handler.handle(message)) {
                return;
            }
        }
        logger.info("- There is no suitable handler for text=\"{}\" received from user={}",
                message.text(), message.chat().firstName());
    }
}
