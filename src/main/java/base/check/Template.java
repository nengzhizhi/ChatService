package base.check;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import com.samskivert.mustache.Mustache;

public class Template<Arg> {
	private com.samskivert.mustache.Template template;
	private Class<Arg> argClass;
	
	private Template(com.samskivert.mustache.Template template,Class<Arg> argClass){
		this.template = template;
		this.argClass = argClass;
	}
	
	public static <Arg> Template<Arg> of(String template,Class<Arg> argClass){
		return new Template<Arg>(template(template),argClass);
	}
	
	public static Template<Void> of(String template){
		return new Template<Void>(template(template),Void.class);
	}

	private static com.samskivert.mustache.Template template(String template){
		return Mustache.compiler().withDelims("${ }").nullValue("null").defaultValue("?").compile(Strings.nullToEmpty(template));
	}
	
	public String get(Arg args){
		return template.execute(args==null?ImmutableMap.of():args);
	}
	
	public String get(){
		return get((Arg)null);
	}
}
