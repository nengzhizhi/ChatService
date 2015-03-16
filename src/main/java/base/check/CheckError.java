package base.check;

public class CheckError {
	private int code;
	private String message;
	private CheckErrorType type;
	
	private CheckError(CheckErrorType type, int code,String message){
		this.type = type;
		this.code = code;
		this.message = message;
	}
	
	public static CheckError of(CheckErrorType type,int code,String message){
		return new CheckError(type,code,message);
	}
	
	public CheckErrorType getType() {
		return type;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
	public CheckException toFastException(){
		return new CheckException(this);
	}
}
