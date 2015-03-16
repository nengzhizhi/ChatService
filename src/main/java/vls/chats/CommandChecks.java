package vls.chats;

import base.check.Check;

public class CommandChecks {
	public Check<Void> UnknowCommand			= Check.e(300001, "unknow command",Void.class);
	public Check<Void> InvalidInput				= Check.e(300002, "输入参数错误！",Void.class);
	public Check<InvalidToken> InvalidToken		= Check.f(300003, "无效的token${token}", InvalidToken.class);
														public static class InvalidToken{public String token;};
	public Check<RoomNoExist> RoomNoExist		= Check.f(300004, "房间${roomId}不存在",RoomNoExist.class);
														public static class RoomNoExist{public String roomId;};
	public Check<Void> NotLogin					= Check.f(300005, "未登陆", Void.class);
	public Check<Void> Silencing				= Check.f(300006, "暂时无法发言", Void.class);
	
	public Check<Void> SendTooFast				= Check.f(300008, "消息发送太频繁", Void.class);												
	public Check<Void> NotRoomManager			= Check.f(300009, "不是房间管理员", Void.class);													
	public Check<Void> ConnectTimeOut			= Check.f(300010, "连接超时",Void.class);
	public Check<Void> ChatterNoExist			= Check.f(300011, "用户不存在",Void.class);
}
