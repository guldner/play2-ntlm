package controllers;

import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security.Authenticated;
import scala.collection.immutable.Seq;
import views.html.index;
import views.html.hello;
import views.html.hi; 
import dk.guldner.play.ntlm.NtlmAuth;
import dk.guldner.play.ntlm.NtlmAuthenticated;
import dk.guldner.play.ntlm.NtlmFilter;
import dk.guldner.play.ntlm.NtlmAuth;
import dk.guldner.play.ntlm.Conf;

public class Application extends Controller {
  
    public static Result index() {
    	String userid = (String) ctx().session().get(Conf.NTLM_SESSION_KEY());
    	Seq<String> groups = (Seq<String>) Cache.get(NtlmFilter.groupsCacheKey(userid));
        return ok(index.render(userid, groups));        
    }
    
    //Will send default '401 Unauthorized' if user not authenticated
    @Authenticated(NtlmAuth.class)
    public static Result hello(){
    	return ok(hello.render());
    }

    @NtlmAuthenticated
    public static Result hi(){
    	Object user = ctx().args.get(Conf.NTLM_SESSION_KEY());
    	user = user == null ? "Unauthenticated" : user;
    	return ok(hi.render(user.toString()));
    }
  
}
