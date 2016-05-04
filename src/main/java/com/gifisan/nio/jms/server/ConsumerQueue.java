package com.gifisan.nio.jms.server;

import java.util.List;

public interface ConsumerQueue {

	public abstract int size();

	public abstract void offer(Consumer consumer);

	public abstract void remove(Consumer consumer);
	
	public abstract void remove(List<Consumer> consumers);

	public abstract Consumer[] snapshot();
	
	public abstract Consumer poll(long timeout) ;

}