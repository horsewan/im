package com.dy.javaclient.core;

import com.dy.javaclient.ClientCoreSDK;
import com.dy.javaclient.utils.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observer;

public class KeepAliveDaemon
{
	private final static String TAG = KeepAliveDaemon.class.getSimpleName();
	
	private static KeepAliveDaemon instance = null;
	
	public static int NETWORK_CONNECTION_TIME_OUT = 10 * 1000;
	public static int KEEP_ALIVE_INTERVAL = 3000;//1000;
	
	private boolean keepAliveRunning = false;
	private long lastGetKeepAliveResponseFromServerTimstamp = 0;
	private Observer networkConnectionLostObserver = null;
	private boolean _excuting = false;
	private Timer timer = null;
	
	public static KeepAliveDaemon getInstance()
	{
		if(instance == null)
			instance = new KeepAliveDaemon();
		return instance;
	}
	
	private KeepAliveDaemon()
	{
		init();
	}
	
	private void init()
	{
		timer = new Timer(KEEP_ALIVE_INTERVAL, new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				run();
			}
		});
	}
	
	public void run()
	{
		if(!_excuting)
		{
			boolean willStop = false;
			_excuting = true;
			if(ClientCoreSDK.DEBUG)
				Log.i(TAG, "【IMCORE】心跳线程执行中...");
			int code = LocalUDPDataSender.getInstance().sendKeepAlive();

			boolean isInitialedForKeepAlive = (lastGetKeepAliveResponseFromServerTimstamp == 0);
			if(isInitialedForKeepAlive)
				lastGetKeepAliveResponseFromServerTimstamp = System.currentTimeMillis();

			if(!isInitialedForKeepAlive)
			{
				long now = System.currentTimeMillis();
				if(now - lastGetKeepAliveResponseFromServerTimstamp >= NETWORK_CONNECTION_TIME_OUT)
				{
					stop();
					if(networkConnectionLostObserver != null)
						networkConnectionLostObserver.update(null, null);
					willStop = true;
				}
			}

			_excuting = false;
			if(!willStop)
			{
				; // do nothing
			}
			else
			{
				timer.stop();
			}
		}
	}
	
	public void stop()
	{
		if(timer != null)
			timer.stop();
		keepAliveRunning = false;
		lastGetKeepAliveResponseFromServerTimstamp = 0;
	}
	
	public void start(boolean immediately)
	{
		stop();
		
		if(immediately)
			timer.setInitialDelay(0);
		else
			timer.setInitialDelay(KEEP_ALIVE_INTERVAL);
		timer.start();
		
		keepAliveRunning = true;
	}
	
	public boolean isKeepAliveRunning()
	{
		return keepAliveRunning;
	}
	
	public void updateGetKeepAliveResponseFromServerTimstamp()
	{
		lastGetKeepAliveResponseFromServerTimstamp = System.currentTimeMillis();
	}

	public void setNetworkConnectionLostObserver(Observer networkConnectionLostObserver)
	{
		this.networkConnectionLostObserver = networkConnectionLostObserver;
	}
}

