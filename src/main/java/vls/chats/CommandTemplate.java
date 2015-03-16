package vls.chats;

public interface CommandTemplate<TYPE> {
	Class<TYPE> execute(String input);
}
