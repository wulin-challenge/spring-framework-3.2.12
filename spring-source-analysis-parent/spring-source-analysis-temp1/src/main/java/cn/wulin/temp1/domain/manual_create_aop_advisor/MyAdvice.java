package cn.wulin.temp1.domain.manual_create_aop_advisor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用该注解标记哪些类要被aop拦截,并实现拦截注解具有继承性,该方式是参考spring transactional 注解
 * @author wulin
 * @See @Transactional
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MyAdvice {

	String value() default "";
}
