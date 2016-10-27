package umu.sakai.umusync.tool.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.el.ValueBinding;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

public class JPAValidator implements Validator {

	//private static Log log = LogFactory.getLog(JPAValidator.class);
	
    public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
    	FacesMessage deferredMessage = null;

    	// Obtenemos el valueBinding del component -> "#{XBean.obj.prop}"
		ValueBinding bind = component.getValueBinding("value");
	    String expresion = bind.getExpressionString();
	    
	    // Obtenemos el valor enviado
	    Object valor;
    	if (((EditableValueHolder) component).getConverter()==null) {
    		valor = ((EditableValueHolder) component).getSubmittedValue();
    	}
    	else {
    		try {
    			valor = ((EditableValueHolder) component).getConverter().getAsObject(facesContext, component, ((EditableValueHolder) component).getSubmittedValue().toString());	    					
    		} catch (ConverterException ce){
    			return;
    		}
    	}

	    int lastPoint = expresion.lastIndexOf('.');
	    String exprPadre = expresion.substring(0, lastPoint)+"}";
	    String prop = expresion.substring(lastPoint+1, expresion.length()-1);
	    ValueBinding bindpadre = facesContext.getApplication().createValueBinding(exprPadre);
	    if (bindpadre.getValue(facesContext)!=null) {
			Class clasepadre = null;			
		    // Si es un objeto validable comprabamos si esta propiedad va asociada un DAO
		    if (bindpadre.getValue(facesContext) instanceof Validable) {
		    	clasepadre = ((Validable) bindpadre.getValue(facesContext)).getDaoClass(prop);		    	
		    }
		    // Si es DAO y tiene idDao(tiene varios objetos DAO) la propiedad es .daoidProp 
		    if (clasepadre!=null) {
		    	String proppadre = ((Validable) bindpadre.getValue(facesContext)).getDaoId(prop);
				if (proppadre != null && proppadre.length() < prop.length()) {
					prop=prop.substring(proppadre.length(),prop.length()).toLowerCase();
				}
		    }
		    // No es Validable, tomamos el objeto como DAO
			else {				
				clasepadre = bindpadre.getValue(facesContext).getClass();
		    }
		    ClassValidator classValidator = new ClassValidator(clasepadre);
		    InvalidValue [] invalidValues = classValidator.getPotentialInvalidValues(prop, valor);
		    // Puede violar varias contraints
		    for (InvalidValue i :  invalidValues) {
	    		if (deferredMessage!=null) {
	    			facesContext.addMessage(component.getId(),deferredMessage);
	    		}
	    		else {
	    			deferredMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,i.getMessage(),i.getMessage());
	    		}
		    }
	    	
	    }
	    //Lanzamos la excepcion diferida
        if (deferredMessage!=null) throw new ValidatorException(deferredMessage);   	
    }
}