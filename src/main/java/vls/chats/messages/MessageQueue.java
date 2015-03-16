package vls.chats.messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import vls.chats.Context;
import vls.chats.db.Repository;
import vls.chats.models.DialogueMessage;

public class MessageQueue {
	private static MessageQueue instance = new MessageQueue();
	private ArrayList<DialogueMessage> list = new ArrayList<DialogueMessage>();

	private int BLOCK_SIZE = Context.instance().appConfig().messageBlockSize();
	private long SCHEDULE_INTERVAL=Context.instance().appConfig().scheduleInterval();
	private long INACTIVE_TIME=Context.instance().appConfig().inactiveTime();
	
	private Timer timer;
	private long timestamp;

	private MessageQueue() {
		timer = new Timer();
		timer.schedule(new MessageTask(), SCHEDULE_INTERVAL, SCHEDULE_INTERVAL);
		timestamp = new Date().getTime();
	}

	public static MessageQueue instance() {
		if (instance == null)
			instance = new MessageQueue();
		return instance;
	}

	public void push(DialogueMessage message) {
		list.add(message);
		timestamp = new Date().getTime();
		synchronized (list) {
			if (list.size() > BLOCK_SIZE) {
				int count = BLOCK_SIZE - 1;
				ArrayList<DialogueMessage> block = new ArrayList<DialogueMessage>(BLOCK_SIZE);
				while (count >= 0) {
					block.add(list.remove(count));
					count--;
				}
				new Thread(new MessageWriter(block)).start();
			}
		}
	}

	class MessageWriter implements Runnable {
		private List<DialogueMessage> messages;

		public MessageWriter(List<DialogueMessage> messages) {
			this.messages = messages;
		}

		@Override
		public void run() {
			Repository repo = new Repository();
			repo.addMessages(messages);
		}
	}

	class MessageTask extends TimerTask {

		@Override
		public void run() {
			long now = new Date().getTime();
			if (now - timestamp > INACTIVE_TIME) {
				synchronized (list) {
					if (list.size() > 0) {
						List<DialogueMessage> block = new ArrayList<DialogueMessage>(
								list.subList(0, list.size()));
						list.clear();
						new Thread(new MessageWriter(block)).start();
					}
				}
			}
		}
	}
}
