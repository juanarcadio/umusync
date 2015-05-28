package umu.sakai.jsf.converter;

import java.util.Map;
import java.util.TimeZone;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;

public class UmuDateTimeConverter extends DateTimeConverter {

	public UmuDateTimeConverter() {
		super();
		setTimeZone(TimeZone.getDefault());
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		Map<String, Object> attributes = component.getAttributes();

		String pattern = (String) attributes.get("datePattern");

		if (pattern != null) {
			setPattern(pattern);
		}
		return super.getAsObject(context, component, value);
	}
	
	@Override
	public String getAsString(FacesContext ctx, UIComponent component,
			Object value) {

		Map<String, Object> attributes = component.getAttributes();

		String pattern = (String) attributes.get("datePattern");

		if (pattern != null) {
			setPattern(pattern);
		}

		return super.getAsString(ctx, component, value);
	}

}
