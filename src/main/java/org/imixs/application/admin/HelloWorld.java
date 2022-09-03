package org.imixs.application.admin;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class HelloWorld {

    private String message;

    @Inject
    private MessageService messageService;

    public void submit() {
        messageService.create(message);
        // reset current message
        message = null;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getMessages() {
        return messageService.list();
    }
}