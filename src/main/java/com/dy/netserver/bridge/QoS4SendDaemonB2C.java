
package com.dy.netserver.bridge;

import com.dy.zserver.qos.QoS4SendDaemonRoot;

public class QoS4SendDaemonB2C extends QoS4SendDaemonRoot
{
	private static QoS4SendDaemonB2C instance = null;
	
	public static QoS4SendDaemonB2C getInstance()
	{
		if(instance == null)
			instance = new QoS4SendDaemonB2C();
		return instance;
	}
	
	private QoS4SendDaemonB2C()
	{
		super(3000    
			, 2 * 1000 
			, -1       
			, true
			, "-桥接QoS！");
	}
}
