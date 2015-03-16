package base.check;

public class CheckException extends RuntimeException{
	private static final long serialVersionUID = 4738068121317144063L;
	private CheckError error;
	
	public CheckException(CheckError error){
		super(error.getMessage());
		this.error = error;
	}
	
	public int getCode(){
		return error.getCode();
	}
	
	public CheckError getError(){
		return error;
	}
}
