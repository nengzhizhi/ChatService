/**
 * Copyright (C) 2013 Hongfu Inc. All rights reserved.
 */

package vls.chats.db;


@SuppressWarnings("serial")
public class DataException extends RuntimeException {
	public DataException() {
		super();
	}

	public DataException(String message) {
		super(message);
	}

	public DataException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataException(Throwable cause) {
		super(cause);
	}
}