package org.imixs.application.admin;

import java.util.ArrayList;
import java.util.List;

import jakarta.ejb.Singleton;
import jakarta.ejb.Stateless;

/**
 * Simple Singleton Message Bean storing a list of strings 
 * @author rsoika
 *
 */
@Singleton
//@Stateless
public class MessageService {

	private List<String> messages;

	public void create(String message) {
		if (messages == null) {
			messages = new ArrayList<String>();
		}
		messages.add(message);
	}

	public List<String> list() {
		if (messages == null) {
			messages = new ArrayList<String>();
		}
		return messages;
	}
}
