package cn.wulin.temp1.domain.event_multicaster;

import org.springframework.context.ApplicationEvent;

/**
 * 定义监听事件
 * @author wubo
 *
 */
public class TestEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	
	private String msg;

	public TestEvent(Object source) {
		super(source);
	}
	
	public TestEvent(Object source,String msg) {
		super(source);
		this.msg = msg;
	}
	
	public void print(){
		System.out.println(msg);
	}
}
