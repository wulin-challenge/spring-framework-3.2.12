package cn.wulin.temp1.domain.event_multicaster;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class TestListener implements ApplicationListener{

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof TestEvent){
			TestEvent testEvent = (TestEvent) event;
			testEvent.print();
		}
	}

}
