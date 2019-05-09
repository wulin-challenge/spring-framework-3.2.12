package cn.wulin.temp1.domain.conversion_service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;

public class String2DateConverter implements Converter<String,Date>{

	@Override
	public Date convert(String source) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
		
		try {
			Date date = sdf.parse(source);
			return date;
		} catch (ParseException e) {
			Date date;
			try {
				date = sdf2.parse(source);
				return date;
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

}
