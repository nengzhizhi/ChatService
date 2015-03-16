package base.check;

import static base.check.CheckErrorType.Exception;
import static base.check.CheckErrorType.Fail;


public final class Check<CONTEXT> {
	
	public static final Check<base.check.Check.GeneralException> GeneralException = e(100002,"${info}",GeneralException.class);public static class GeneralException{public String info;}
	
	private CheckErrorType errorType;
	private int errorCode;
	private Template<CONTEXT> template;
	
	
	public static <CONTEXT> Check<CONTEXT> of(CheckErrorType errorType,int errorCode,Template<CONTEXT> template){
		return new Check<CONTEXT>(errorType,errorCode,template);
	}
	
	public static <CONTEXT> Check<CONTEXT> f(int errorCode,String messageTemplate,Class<CONTEXT> contextClass){
		return of(Fail,errorCode,Template.of(messageTemplate, contextClass));
	}
	
	public static <CONTEXT> Check<CONTEXT> e(int errorCode,String messageTemplate,Class<CONTEXT> contextClass){
		return of(Exception,errorCode,Template.of(messageTemplate, contextClass));
	}
	
	private Check(CheckErrorType errorType,int errorCode,Template<CONTEXT> template){
		this.errorType = errorType;
		this.errorCode = errorCode;
		this.template = template;
	}
	
	public boolean check(boolean expression){
		if(!expression){
			fail();
		}
		return expression;
	}
	
	public boolean check(boolean expression,CONTEXT context){
		if(!expression){
			fail(context);
		}
		return expression;
	}
	
	public void fail(){
		throw CheckError.of(errorType,errorCode,message()).toFastException();
	}
	
	public void fail(CONTEXT context){
		throw CheckError.of(errorType,errorCode,message(context)).toFastException();
	}
	
	public CheckError toCheckError(){
		return CheckError.of(errorType,errorCode,message());
	}
	
	public CheckError toCheckError(CONTEXT context){
		return CheckError.of(errorType,errorCode,message(context));
	}
	
	public String message(CONTEXT context){
		return template.get(context);
	}
	
	public String message(){
		return template.get();
	}
	
	public int getErrorCode() {
		return errorCode;
	}	
}
