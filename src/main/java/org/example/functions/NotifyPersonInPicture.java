package org.example.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.util.Map;

public class NotifyPersonInPicture implements RequestHandler<Map<String, String>, Void> {

    final String CLIENT_REGION = System.getenv("CLIENT_REGION");
    final String TABLE_NAME = System.getenv("TABLE_NAME");
    final String TWILIO_ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    final String TWILIO_AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    final String TWILIO_PHONE_NUMBER = System.getenv("TWILIO_PHONE_NUMBER");
    final String DESTINATION_PHONE_NUMBER = System.getenv("DESTINATION_PHONE_NUMBER");

    @Override
    public Void handleRequest(Map<String, String> stringStringMap, Context context) {

        try {
            Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);

            Message message = Message.creator(new PhoneNumber(DESTINATION_PHONE_NUMBER),
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    "Pessoa identificada!").create();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
