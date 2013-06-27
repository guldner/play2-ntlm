Sample Play Java project using the ntlm module
===============================================

## Configuration
In this sample project the ntlm-module is configured with all defaults except 
'ntlmmodule.protected.uris':

```
#ntlmmodule.challenge = Negotiate
#ntlmmodule.session.key = ntlmuser
#ntlmmodule.encrypt.userid = false
#ntlmmodule.savegroups = true
#ntlmmodule.relevantgroups # = All
#ntlmmodule.unprotected.uris # = None
ntlmmodule.protected.uris = [/]
#ntlmmodule.protect.assets = false
#ntlmmodule.post.auth.redirect.uri # request.uri 
```

## Routes
3 actions are created.

```
GET     /                           controllers.Application.index()
GET     /hello                      controllers.Application.hello()
GET     /hi                         controllers.Application.hi()
```

### /index
Shows how to get the userid from the session and the groups from Cache

```
    public static Result index() {
    	String userid = (String) ctx().session().get(Conf.NTLM_SESSION_KEY());
    	Seq<String> groups = (Seq<String>) Cache.get(NtlmFilter.groupsCacheKey(userid));
        return ok(index.render(userid, groups));        
    }
```

### /hello
Shows how to use the framework provided @Authenticated annotation. This will return the default `unauthorized(views.html.defaultpages.unauthorized.render())` if user is not authenticated

```
    @Authenticated(NtlmAuth.class)
    public static Result hello(){
    	return ok(hello.render());
    }
```

### /hi
The @NtlmAuthenticated annotation will wrap the action in a `dk.guldner.play.ntlm.NtlmAction` which adds the userid to Http.Context.args *if* a userid was found 

```
    @NtlmAuthenticated
    public static Result hi(){
    	Object user = ctx().args.get(Conf.NTLM_SESSION_KEY());
    	user = user == null ? "Unauthenticated" : user;
    	return ok(hi.render(user.toString()));
    }

```

Remove the session cookie to test the behaviour of /hello and /hi for an un-authenticated user. The provided annotation/action and authenticator is ment as an inspiration. Create your own to suit your needs