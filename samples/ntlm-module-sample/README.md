Sample Play Scala project using the ntlm module
===============================================


## Configuration
In this sample project the ntlm-module is configured as follows:

```
#ntlmmodule.challenge = Negotiate
#ntlmmodule.session.key = ntlmuser
#ntlmmodule.encrypt.userid = false
#ntlmmodule.savegroups = true
#ntlmmodule.relevantgroups = [
#    Administrators
#    "\\Users" 
#    "CORP\\IT_Development" 
#]
ntlmmodule.unprotected.uris = [
    /hello
    /logout
]
#ntlmmodule.protected.uris = [
#   /
#   /removegroups
#]
#ntlmmodule.protect.assets = false
ntlmmodule.post.auth.redirect.uri = /hello
```

## Routes
4 actions are created.

```
GET     /                           controllers.Application.index
GET     /hello                      controllers.Application.hello
GET     /removegroups               controllers.Application.removeGroups
GET     /logout               		controllers.Application.logout
```

These actions display the use of the module.
### /index
Shows how to use the NtlmAuthenticated method provided by the NtlmSecurity trait.
```
def index = NtlmAuthenticated { ntlmUser =>
    implicit request =>
      Ok(views.html.index(ntlmUser))
  }
```

### /hello
In the configuration '/hello' is set as 'unprotected'. You can use it to check whether there's an ntlm userid stored on the session or not. (See /logout)
```
  def hello = Action { request =>
    val userid = getUseridFromSession(request)
    Ok(views.html.hello(userid.getOrElse("Unauthorized stranger")))
  }
```
The getUseridFromSession method picks the ntlm userid from the session:
```
request.session.get(NTLM_SESSION_KEY) map { u => if (ENCRYPT_USERID) Crypto.decryptAES(u) else u }
```

### /removegroups
Shows how to remove the user's groups from the Cache.
```
  def removeGroups = Action { implicit request =>
    val userid = getUseridFromSession(request)
    val key = userid map { u => NtlmFilter.groupsCacheKey(u) }
    key map { k =>
      Logger.info("Removing these groups from cache: " + Cache.get(k))
      Cache.remove(k)
    }
    Ok(views.html.removegroups())
  }
```

### /logout
Simply removes the 'ntlmuser' from the session.
```
  def logout = NtlmAuthenticated { ntlmUser =>
    implicit request =>
      Ok(views.html.logout(ntlmUser)).withSession{ session - NTLM_SESSION_KEY}
  }
```
Note: '/logout' should be added to 'unprotected.uris'. If it is not then an unauthenticated user accessing /logout will be authenticated before being "logged out" again.
In most internal webapps (where using ntlm makes the most sense) everything would be protected and no 'logout' would be necessary. In this case both 'protected.uris' and 'unprotected.uris' would be left at default values 'All' and 'None'.

### Note
There's an NtlmPlugin added in the play.plugins file in this project. This is not necessary. All it does is logging the module configuration to 'info'