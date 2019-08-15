
package com.dy.zserver.event;

import com.dy.zserver.protocal.Protocal;

import java.util.ArrayList;

public interface MessageQoSEventListenerS2C
{
	void messagesLost(ArrayList<Protocal> lostMessages);
	void messagesBeReceived(String theFingerPrint);
}
