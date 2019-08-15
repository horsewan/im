
package com.dy.zserver.processor;

import com.dy.netserver.bridge.QoS4SendDaemonB2C;
import com.dy.netserver.netty.MBObserver;
import com.dy.zserver.ServerCoreHandler;
import com.dy.zserver.protocal.Protocal;
import com.dy.zserver.protocal.ProtocalFactory;
import com.dy.zserver.protocal.c.PLoginInfo;
import com.dy.zserver.qos.QoS4ReciveDaemonC2S;
import com.dy.zserver.qos.QoS4SendDaemonS2C;
import com.dy.zserver.utils.GlobalSendHelper;
import com.dy.zserver.utils.LocalSendHelper;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicProcessor
{
	private static Logger logger = LoggerFactory.getLogger(LogicProcessor.class);
	
	private ServerCoreHandler serverCoreHandler = null;

	public LogicProcessor(ServerCoreHandler serverCoreHandler)
	{
		this.serverCoreHandler = serverCoreHandler;
	}
	
	public void processC2CMessage(BridgeProcessor bridgeProcessor,
                                  Channel session, Protocal pFromClient, String remoteAddress) throws Exception
	{
		GlobalSendHelper.sendDataC2C(bridgeProcessor, session, pFromClient
				, remoteAddress, this.serverCoreHandler);
	}
	
	public void processC2SMessage(Channel session, final Protocal pFromClient
			, String remoteAddress) throws Exception
	{
		if(pFromClient.isQoS())// && processedOK)
		{
			if(QoS4ReciveDaemonC2S.getInstance().hasRecieved(pFromClient.getFp()))
			{
				if(QoS4ReciveDaemonC2S.getInstance().isDebugable())
					logger.debug("[IMCORE-本机QoS！]【QoS机制】"+pFromClient.getFp()
							+"已经存在于发送列表中，这是重复包，通知业务处理层收到该包罗！");
				
				QoS4ReciveDaemonC2S.getInstance().addRecieved(pFromClient);
				LocalSendHelper.replyDelegateRecievedBack(session
						, pFromClient
						, new MBObserver(){
							@Override
							public void update(boolean receivedBackSendSucess, Object extraObj)
							{
								if(receivedBackSendSucess)
									logger.debug("[IMCORE-本机QoS！]【QoS_应答_C2S】向"+pFromClient.getFrom()+"发送"+pFromClient.getFp()
											+"的应答包成功了,from="+pFromClient.getTo()+".");
							}
						}
				);

				return;
			}
			
			QoS4ReciveDaemonC2S.getInstance().addRecieved(pFromClient);
			LocalSendHelper.replyDelegateRecievedBack(session
					, pFromClient
					, new MBObserver(){
						@Override
						public void update(boolean receivedBackSendSucess, Object extraObj)
						{
							if(receivedBackSendSucess)
								logger.debug("[IMCORE-本机QoS！]【QoS_应答_C2S】向"+pFromClient.getFrom()+"发送"+pFromClient.getFp()
										+"的应答包成功了,from="+pFromClient.getTo()+".");
						}
					}
			);
		}

//		boolean processedOK = this.serverCoreHandler.getServerEventListener().onTransBuffer_CallBack(
//				pFromClient.getTo(), pFromClient.getFrom(), pFromClient.getDataContent()
//				, pFromClient.getFp(), pFromClient.getTypeu(), session);
		boolean processedOK = this.serverCoreHandler.getServerEventListener().onTransBuffer_C2S_CallBack(pFromClient, session);
	}
	
	public void processACK(final Protocal pFromClient, final String remoteAddress) throws Exception
	{
		if("0".equals(pFromClient.getTo()))
		{
			String theFingerPrint = pFromClient.getDataContent();
			logger.debug("[IMCORE-本机QoS！]【QoS机制_S2C】收到接收者"+pFromClient.getFrom()+"回过来的指纹为"+theFingerPrint+"的应答包.");

			if(this.serverCoreHandler.getServerMessageQoSEventListener() != null)
				this.serverCoreHandler.getServerMessageQoSEventListener()
					.messagesBeReceived(theFingerPrint);

			QoS4SendDaemonS2C.getInstance().remove(theFingerPrint);
		}
		else
		{
			// TODO just for DEBUG
			OnlineProcessor.getInstance().__printOnline();
			
			final String theFingerPrint = pFromClient.getDataContent();
			boolean isBridge = pFromClient.isBridge();
			
			if(isBridge)
			{
				logger.debug("[IMCORE-桥接QoS！]【QoS机制_S2C】收到接收者"+pFromClient.getFrom()+"回过来的指纹为"+theFingerPrint+"的应答包.");
				QoS4SendDaemonB2C.getInstance().remove(theFingerPrint);
			}
			else
			{
				MBObserver sendResultObserver = new MBObserver(){
					@Override
					public void update(boolean _sendOK, Object extraObj)
					{
						logger.debug("[IMCORE-本机QoS！]【QoS机制_C2C】"+pFromClient.getFrom()+"发给"+pFromClient.getTo()
								+"的指纹为"+theFingerPrint+"的应答包已成功转发？"+_sendOK);
					}
				};
				
				LocalSendHelper.sendData(pFromClient, sendResultObserver);
			}
		}
	}
	
	public void processLogin(final Channel session, final Protocal pFromClient, final String remoteAddress) throws Exception
	{
		final PLoginInfo loginInfo = ProtocalFactory.parsePLoginInfo(pFromClient.getDataContent());
		logger.info("[IMCORE]>> 客户端"+remoteAddress+"发过来的登陆信息内容是：loginInfo="
				+loginInfo.getLoginUserId()+"|getToken="+loginInfo.getLoginToken());
		
		if(loginInfo == null || loginInfo.getLoginUserId() == null)
		{
			logger.warn("[IMCORE]>> 收到客户端"+remoteAddress
					+"登陆信息，但loginInfo或loginInfo.getLoginUserId()是null，登陆无法继续[loginInfo="+loginInfo
					+",loginInfo.getLoginUserId()="+loginInfo.getLoginUserId()+"]！");
			return;
		}
		
		if(serverCoreHandler.getServerEventListener() != null)
		{
			boolean alreadyLogined = OnlineProcessor.isLogined(session);//(_try_user_id != -1);
			if(alreadyLogined)
			{
				logger.debug("[IMCORE]>> 【注意】客户端"+remoteAddress+"的会话正常且已经登陆过，而此时又重新登陆：getLoginName="
        				+loginInfo.getLoginUserId()+"|getLoginPsw="+loginInfo.getLoginToken());
				
				MBObserver retObserver = new MBObserver(){
					@Override
					public void update(boolean _sendOK, Object extraObj)
					{
						if(_sendOK)
						{
							session.attr(OnlineProcessor.USER_ID_IN_SESSION_ATTRIBUTE_ATTR).set(loginInfo.getLoginUserId());
							OnlineProcessor.getInstance().putUser(loginInfo.getLoginUserId(), session);
							serverCoreHandler.getServerEventListener().onUserLoginAction_CallBack(
									loginInfo.getLoginUserId(), loginInfo.getExtra(), session);
						}
						else
						{
							logger.warn("[IMCORE]>> 发给客户端"+remoteAddress+"的登陆成功信息发送失败了！");
						}
					}
				};
				
				LocalSendHelper.sendData(session
						, ProtocalFactory.createPLoginInfoResponse(0, loginInfo.getLoginUserId()), retObserver);
			}
			else
			{
				int code = serverCoreHandler.getServerEventListener().onVerifyUserCallBack(
						loginInfo.getLoginUserId(), loginInfo.getLoginToken(), loginInfo.getExtra(), session);
				if(code == 0)
				{
					MBObserver sendResultObserver = new MBObserver(){
						@Override
						public void update(boolean __sendOK, Object extraObj)
						{
							if(__sendOK)
							{
								session.attr(OnlineProcessor.USER_ID_IN_SESSION_ATTRIBUTE_ATTR).set(loginInfo.getLoginUserId());
								OnlineProcessor.getInstance().putUser(loginInfo.getLoginUserId(), session);
								serverCoreHandler.getServerEventListener()
									.onUserLoginAction_CallBack(loginInfo.getLoginUserId(), loginInfo.getExtra(), session);
							}
							else
								logger.warn("[IMCORE]>> 发给客户端"+remoteAddress+"的登陆成功信息发送失败了！");
							
						}
					};
					LocalSendHelper.sendData(session
							, ProtocalFactory.createPLoginInfoResponse(code, loginInfo.getLoginUserId())
							, sendResultObserver);
				}
				else
				{
					LocalSendHelper.sendData(session, ProtocalFactory.createPLoginInfoResponse(code, "-1"), null);
				}
			}
		}
		else
		{
			logger.warn("[IMCORE]>> 收到客户端"+remoteAddress+"登陆信息，但回调对象是null，没有进行回调.");
		}
	}

	public void processKeepAlive(Channel session, Protocal pFromClient
			, String remoteAddress) throws Exception
	{
		String userId = OnlineProcessor.getUserIdFromSession(session);
		if(userId != null)
		{
			LocalSendHelper.sendData(ProtocalFactory.createPKeepAliveResponse(userId), null);
		}
		else
		{
			logger.warn("[IMCORE]>> Server在回客户端"+remoteAddress+"的响应包时，调用getUserIdFromSession返回null，用户在这一瞬间掉线了？！");
		}
	}
}


