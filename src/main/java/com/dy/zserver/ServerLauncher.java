package com.dy.zserver;

import com.dy.netserver.bridge.QoS4ReciveDaemonC2B;
import com.dy.netserver.bridge.QoS4SendDaemonB2C;
import com.dy.netserver.netty.MBUDPClientInboundHandler;
import com.dy.netserver.netty.MBUDPServerChannel;
import com.dy.zserver.event.MessageQoSEventListenerS2C;
import com.dy.zserver.event.ServerEventListener;
import com.dy.zserver.qos.QoS4ReciveDaemonC2S;
import com.dy.zserver.qos.QoS4SendDaemonS2C;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class ServerLauncher 
{
	private static Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
	
	public static boolean debug = true;
	public static String appKey = null;
    public static int PORT = 7901;
    public static int SESION_RECYCLER_EXPIRE = 10;
    public static boolean bridgeEnabled = false;
    
    private boolean running = false;
    protected ServerCoreHandler serverCoreHandler = null;
    
 	private final EventLoopGroup __bossGroup4Netty = new NioEventLoopGroup();
 	private final EventLoopGroup __workerGroup4Netty = new DefaultEventLoopGroup();
 	private Channel __serverChannel4Netty = null;

 	public ServerLauncher() throws IOException 
 	{
 		// default do nothing
 	}

 	public boolean isRunning()
 	{
 		return running;
 	}

 	public void startup() throws Exception
 	{	
 		if(!this.running)
 		{
 			serverCoreHandler = initServerCoreHandler();

 			initListeners();

 			ServerBootstrap bootstrap = initServerBootstrap4Netty();

 			QoS4ReciveDaemonC2S.getInstance().startup();
 			QoS4SendDaemonS2C.getInstance().startup(true).setServerLauncher(this);

 			if(ServerLauncher.bridgeEnabled){

 				QoS4ReciveDaemonC2B.getInstance().startup();
 				QoS4SendDaemonB2C.getInstance().startup(true).setServerLauncher(this);

 				serverCoreHandler.lazyStartupBridgeProcessor();

 				logger.info("[IMCORE-netty] 配置项：已开启与MobileIMSDK Web的互通.");
 			}
 			else{
 				logger.info("[IMCORE-netty] 配置项：未开启与MobileIMSDK Web的互通.");
 			}

 			ChannelFuture cf = bootstrap.bind("0.0.0.0", PORT).syncUninterruptibly();
 			__serverChannel4Netty = cf.channel();

 			this.running = true;
 			logger.info("[IMCORE-netty] 基于MobileIMSDK的UDP服务正在端口" + PORT+"上监听中...");

 			__serverChannel4Netty.closeFuture().await();
 		}
 		else
 		{
 			logger.warn("[IMCORE-netty] 基于MobileIMSDK的UDP服务正在运行中" +
 					"，本次startup()失败，请先调用shutdown()后再试！");
 		}
    }

    public void shutdown()
    {
    	if (__serverChannel4Netty != null) 
    		__serverChannel4Netty.close();

		__bossGroup4Netty.shutdownGracefully();
		__workerGroup4Netty.shutdownGracefully();
		
    	QoS4ReciveDaemonC2S.getInstance().stop();
    	QoS4SendDaemonS2C.getInstance().stop();
    	
    	if(ServerLauncher.bridgeEnabled){
    		QoS4ReciveDaemonC2B.getInstance().stop();
    		QoS4SendDaemonB2C.getInstance().stop();
    	}
    	
    	this.running = false;
    }
    
    protected ServerCoreHandler initServerCoreHandler()
    {
    	return new ServerCoreHandler();
    }
    
    protected abstract void initListeners();
    
    protected ServerBootstrap initServerBootstrap4Netty()
    {
    	return new ServerBootstrap()
    		.group(__bossGroup4Netty, __workerGroup4Netty)
    		.channel(MBUDPServerChannel.class)
    		.childHandler(initChildChannelHandler4Netty());
    }
    
	protected ChannelHandler initChildChannelHandler4Netty()
	{
		return new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				channel.pipeline()
					.addLast(new ReadTimeoutHandler(SESION_RECYCLER_EXPIRE))
					.addLast(new MBUDPClientInboundHandler(serverCoreHandler));
			}
		};
	}
    
    public ServerEventListener getServerEventListener()
	{
		return serverCoreHandler.getServerEventListener();
	}
	public void setServerEventListener(ServerEventListener serverEventListener)
	{
		this.serverCoreHandler.setServerEventListener(serverEventListener);
	}
	
	public MessageQoSEventListenerS2C getServerMessageQoSEventListener()
	{
		return serverCoreHandler.getServerMessageQoSEventListener();
	}
	public void setServerMessageQoSEventListener(MessageQoSEventListenerS2C serverMessageQoSEventListener)
	{
		this.serverCoreHandler.setServerMessageQoSEventListener(serverMessageQoSEventListener);
	}

	public ServerCoreHandler getServerCoreHandler()
	{
		return serverCoreHandler;
	}
}
