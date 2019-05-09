package cn.wulin.temp1.test.other.object_key;

import java.lang.reflect.Method;

import org.springframework.util.ObjectUtils;

/**
 * Default cache key for the TransactionAttribute cache.
 * 
 * <p> TransactionAttribute缓存的默认缓存键。
 * 
 */
public class ObjectKey {

	private final Method method;

	private final Class<?> targetClass;

	public ObjectKey(Method method, Class<?> targetClass) {
		this.method = method;
		this.targetClass = targetClass;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ObjectKey)) {
			return false;
		}
		ObjectKey otherKey = (ObjectKey) other;
		return (this.method.equals(otherKey.method) &&
				ObjectUtils.nullSafeEquals(this.targetClass, otherKey.targetClass));
	}

	@Override
	public int hashCode() {
		return this.method.hashCode();
	}
}