package com.dy.javaclient;

import com.dy.javaclient.core.*;
import com.dy.javaclient.event.ChatBaseEvent;
import com.dy.javaclient.event.ChatTransDataEvent;
import com.dy.javaclient.event.MessageQoSEvent;

public class ClientCoreSDK
{
	private final static String TAG = ClientCoreSDK.class.getSimpleName();
	
	public static boolean DEBUG = true;
	public static boolean autoReLogin = true;
	
	private static ClientCoreSDK instance = null;
	
	private boolean _init = false;
	private boolean connectedToServer = true;
	private boolean loginHasInit = false;
	private String currentLoginUserId = null;
	private String currentLoginToken = null;
	private String currentLoginExtra = null;
	
	private ChatBaseEvent chatBaseEvent = null;
	private ChatTransDataEvent chatTransDataEvent = null;
	private MessageQoSEvent messageQoSEvent = null;
	
	public static ClientCoreSDK getInstance()
	{
		if(instance == null)
			instance = new ClientCoreSDK();
		return instance;
	}
	
	private ClientCoreSDK()
	{
	}
	
	public void init()
	{
		if(!_init)
		{
			_init = true;
		}
	}
	
	public void release()
	{
		LocalUDPSocketProvider.getInstance().closeLocalUDPSocket();
	    AutoReLoginDaemon.getInstance().stop(); // 2014-11-08 add by Jack Jiang
		QoS4SendDaemon.getInstance().stop();
		KeepAliveDaemon.getInstance().stop();
		LocalUDPDataReciever.getInstance().stop();
		QoS4ReciveDaemon.getInstance().stop();
		
		//## Bug FIX: 20180103 by Jack Jiang START
		QoS4SendDaemon.getInstance().clear();
		QoS4ReciveDaemon.getInstance().clear();
		//## Bug FIX: 20180103 by Jack Jiang END
		
		_init = false;
		
		this.setLoginHasInit(false);
		this.setConnectedToServer(false);
	}
	
	public String getCurrentLoginUserId()
	{
		return currentLoginUserId;
	}
	public ClientCoreSDK setCurrentLoginUserId(String currentLoginUserId)
	{
		this.currentLoginUserId = currentLoginUserId;
		return this;
	}
	
	public String getCurrentLoginToken()
	{
		return currentLoginToken;
	}
	public void setCurrentLoginToken(String currentLoginToken)
	{
		this.currentLoginToken = currentLoginToken;
	}
	
	public String getCurrentLoginExtra()
	{
		return currentLoginExtra;
	}
	public ClientCoreSDK setCurrentLoginExtra(String currentLoginExtra)
	{
		this.currentLoginExtra = currentLoginExtra;
		return this;
	}

	public boolean isLoginHasInit()
	{
		return loginHasInit;
	}
	public ClientCoreSDK setLoginHasInit(boolean loginHasInit)
	{
		this.loginHasInit = loginHasInit;
		return this;
	}
	
	public boolean isConnectedToServer()
	{
		return connectedToServer;
	}
	public void setConnectedToServer(boolean connectedToServer)
	{
		this.connectedToServer = connectedToServer;
	}

	public boolean isInitialed()
	{
		return this._init;
	}

	public void setChatBaseEvent(ChatBaseEvent chatBaseEvent)
	{
		this.chatBaseEvent = chatBaseEvent;
	}
	public ChatBaseEvent getChatBaseEvent()
	{
		return chatBaseEvent;
	}
	
	public void setChatTransDataEvent(ChatTransDataEvent chatTransDataEvent)
	{
		this.chatTransDataEvent = chatTransDataEvent;
	}
	public ChatTransDataEvent getChatTransDataEvent()
	{
		return chatTransDataEvent;
	}
	
	public void setMessageQoSEvent(MessageQoSEvent messageQoSEvent)
	{
		this.messageQoSEvent = messageQoSEvent;
	}
	public MessageQoSEvent getMessageQoSEvent()
	{
		return messageQoSEvent;
	}
}
