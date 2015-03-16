/**
 * Copyright (C) 2013 Hongfu Inc. All rights reserved.
 */

//package gir.chats.unit;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import wns.chats.commands.CommandType;
//
//public class CommandTypeTest {
//	@Test
//	public void fromStringTest() {
//		CommandType type = CommandType.fromString("disconnect");
//		Assert.assertNotNull(type);
//		Assert.assertEquals(CommandType.DISCONNECT, type);
//	}
//
//	@Test
//	public void fromStringWithNotExistCommandTest() {
//		CommandType type = CommandType.fromString("nonexistenum");
//		Assert.assertNotNull(type);
//		Assert.assertEquals(CommandType.UNKNOW, type);
//	}
//
//	@Test
//	public void toStringTest() {
//		CommandType type = CommandType.PING;
//		Assert.assertEquals("ping", type.toString());
//	}
//}
