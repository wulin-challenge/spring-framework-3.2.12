package cn.wulin.security.oauth2.controller.userA;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 模式a.com的服务端,本质上这是b.com的服务端
 * @author wulin
 *
 */
@Controller
@RequestMapping("/userA")
public class UserAController {
	private static final BlockingQueue<String> CODE_QUEUE = new LinkedBlockingQueue<String>(10000);

	/**
	 * b.com应用认证成功后,返回授权码的地址
	 * @param code
	 * @return
	 */
	@RequestMapping("/responseCode")
	public @ResponseBody Map<String,Object> responseCode(String code){
		try {
			CODE_QUEUE.put(code);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new HashMap<String,Object>();
	}
	
	/**
	 * a.com应用获取b.com的授权码
	 * @return
	 */
	@RequestMapping("/obtainCode")
	public @ResponseBody Map<String,Object> obtainCode(){
		Map<String,Object> codes = new HashMap<String,Object>();
		try {
			codes.put("code", CODE_QUEUE.poll(60, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return codes;
	}
}
