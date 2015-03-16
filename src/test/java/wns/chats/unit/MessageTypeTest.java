/**
 * Copyright (C) 2013 Hongfu Inc. All rights reserved.
 */

//package gir.chats.unit;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import wns.chats.ProgramErrorCode;
//import wns.chats.messages.MessageType;
//
//public class MessageTypeTest {
//	@Test
//	public void fromStringTest() {
//		MessageType type = MessageType.parse("message_push");
//		Assert.assertEquals(MessageType.MESSAGE_PUSH, type);
//		type = MessageType.parse("send");
//		Assert.assertEquals(MessageType.SEND, type);
//	}
//
//	@Test
//	public void fromStringWithNotExistCommandTest() {
//		MessageType type = MessageType.parse("nonexistenum");
//		Assert.assertNotNull(type);
//		Assert.assertEquals(MessageType.UNKNOW, type);
//	}
//
//	@Test
//	public void toStringTest() {
//		MessageType type = MessageType.DISCONNECT;
//		Assert.assertEquals("disconnect", type.toString());
//	}
//	
//	@Test
//	public void errorCodeTest(){
//		System.out.println(ProgramErrorCode.PARAMETER_MISSING.getValue());
//		System.out.println(ProgramErrorCode.PARAMETER_MISSING);
//		System.out.println(Integer.toHexString(Integer.MAX_VALUE));
//	}
//}
