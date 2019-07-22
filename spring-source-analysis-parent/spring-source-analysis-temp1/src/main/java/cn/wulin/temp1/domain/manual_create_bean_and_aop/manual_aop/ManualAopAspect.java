package cn.wulin.temp1.domain.manual_create_bean_and_aop.manual_aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;

/**
 * 手动aop的aspectJ
 */
@Aspect
public class ManualAopAspect implements Ordered{


    @Pointcut("@annotation(cn.wulin.temp1.domain.manual_create_bean_and_aop.manual_aop.ManualAop)")
    public void manualAopCall() {

    }

    @Around("manualAopCall()")
    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {
    	
    	System.out.println("手动创建aop成功!!现在进入@Around拦截点!");
        return pjp.proceed();
    }

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}


}
