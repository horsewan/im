package com.dy;

import io.netty.channel.Channel;
import com.dy.zserver.protocal.Protocal;
import  com.dy.zserver.event.ServerEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 与客服端的所有数据交互事件在此ServerEventListener子类中实现即可。
 * 
 * @
 * @version 1.0
 * @since 3.1
 */
public class ServerEventListenerImpl implements ServerEventListener
{
	private static Logger logger = LoggerFactory.getLogger(ServerEventListenerImpl.class);
	

	//@Override
	public int onVerifyUserCallBack(String userId, String token, String extra, Channel session)
	{
		logger.debug("【DEBUG_回调通知】正在调用回调方法：OnVerifyUserCallBack...(extra="+extra+")");
		return 0;
	}


	//@Override
	public void onUserLoginAction_CallBack(String userId, String extra, Channel session)
	{
		logger.debug("【IM_回调通知OnUserLoginAction_CallBack】用户："+userId+" 上线了！");
	}

	/**
	 * 用户退出登录回调方法定义（可理解为下线通知回调）。
	 * <p>
	 * 服务端的应用层通常可在本方法中实现用户下线通知等。
	 * 
	 * @param userId 下线的用户user_id
	 * @param obj
	 * @param session 此客户端连接对应的 netty “会话”
	 */
	//@Override
	public void onUserLogoutAction_CallBack(String userId, Object obj, Channel session)
	{
		logger.debug("【DEBUG_回调通知OnUserLogoutAction_CallBack】用户："+userId+" 离线了！");
	}


	//@Override
	public boolean onTransBuffer_C2S_CallBack(Protocal p, Channel session)
	{
		// 接收者uid
		String userId = p.getTo();
		// 发送者uid
		String from_user_id = p.getFrom();
		// 消息或指令内容
		String dataContent = p.getDataContent();
		// 消息或指令指纹码（即唯一ID）
		String fingerPrint = p.getFp();
		// 【重要】用户定义的消息或指令协议类型（开发者可据此类型来区分具体的消息或指令）
		int typeu = p.getTypeu();
				
		logger.debug("【DEBUG_回调通知】[typeu="+typeu+"]收到了客户端"+from_user_id+"发给服务端的消息：str="+dataContent);
		return true;
	}


	//@Override
	public void onTransBuffer_C2C_CallBack(Protocal p)
	{
		// 接收者uid
		String userId = p.getTo();
		// 发送者uid
		String from_user_id = p.getFrom();
		// 消息或指令内容
		String dataContent = p.getDataContent();
		// 消息或指令指纹码（即唯一ID）
		String fingerPrint = p.getFp();
		// 【重要】用户定义的消息或指令协议类型（开发者可据此类型来区分具体的消息或指令）
		int typeu = p.getTypeu();
				
		logger.debug("【DEBUG_回调通知】[typeu="+typeu+"]收到了客户端"+from_user_id+"发给客户端"+userId+"的消息：str="+dataContent);
	}


	//@Override
	public boolean onTransBuffer_C2C_RealTimeSendFaild_CallBack(Protocal p)
	{
		// 接收者uid
		String userId = p.getTo();
		// 发送者uid
		String from_user_id = p.getFrom();
		// 消息或指令内容
		String dataContent = p.getDataContent();
		// 消息或指令指纹码（即唯一ID）
		String fingerPrint = p.getFp();
		// 【重要】用户定义的消息或指令协议类型（开发者可据此类型来区分具体的消息或指令）
		int typeu = p.getTypeu();

		logger.debug("【DEBUG_回调通知】[typeu="+typeu+"]客户端"+from_user_id+"发给客户端"+userId+"的消息：str="+dataContent
				+"，因实时发送没有成功，需要上层应用作离线处理哦，否则此消息将被丢弃.");
		return false;
	}
}
