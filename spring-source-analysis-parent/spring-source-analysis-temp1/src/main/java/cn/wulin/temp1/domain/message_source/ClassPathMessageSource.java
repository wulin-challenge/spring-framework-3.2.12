package cn.wulin.temp1.domain.message_source;

import java.io.IOException;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ClassPathMessageSource extends ResourceBundleMessageSource{
	
	ResourcePatternResolver resourcePatternResolver =new PathMatchingResourcePatternResolver();

	@Override
	public void setBasename(String basename) {
		
		basename = getPath(basename);
		super.setBasename(basename);
	}

	@Override
	public void setBasenames(String... basenames) {
		String[] basenamesPath = new String[basenames.length];
		
		for (int i = 0; i < basenames.length; i++) {
			basenamesPath[i] = getPath(basenames[i]);
		}
		super.setBasenames(basenamesPath);
	}
	
	private String getPath(String basename){
		int i=1;
		if(i==1){
			return basename;
		}
		try {
			Resource[] resources = resourcePatternResolver.getResources(basename);
			if(resources != null){
				String path = resources[0].getURL().toString();
				path = path.substring(0, path.lastIndexOf("_"));
				path = path.substring(0, path.lastIndexOf("_"));
				return path;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

}
