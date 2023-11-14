package com.bulpros.integrations.camel.routes.notification;

import com.bulpros.integrations.notification.model.NotificationMailRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.AttachmentMessage;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AttachmentProcessor implements Processor {

    public void process(Exchange exchange) throws Exception {
        NotificationMailRequest requestBody = exchange.getIn().getBody(NotificationMailRequest.class);

        if (requestBody.getAttachment() != null && !requestBody.getAttachment().getContent().isEmpty()
                && !requestBody.getAttachment().getContentType().isEmpty() && !requestBody.getAttachment().getName().isEmpty()) {

            NotificationMailRequest.Attachment attachment = requestBody.getAttachment();

            exchange.getOut().setHeader(attachment.getName(), attachment.getContent());

            Map<String, DataHandler> attachmentForEmail = new HashMap<>();
            attachmentForEmail.put(attachment.getName(), new DataHandler(new ByteArrayDataSource(//
                    Base64.getDecoder().decode(attachment.getContent()), //
                    attachment.getContentType() //
            )));

            exchange.getOut(AttachmentMessage.class).setAttachments(attachmentForEmail);
            exchange.getOut().setBody(requestBody);
        }
    }
}