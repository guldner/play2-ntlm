# Play 2.1 module for Ntlm authentication using Waffle

The ntlm-module authenticates the user with either Ntlm or Negotiate.
An authenticated user's user-id is stored on the session. 

The module makes use of Waffle's [NegotiateSecurityFilterProvider](https://github.com/dblock/waffle/blob/master/Source/JNA/waffle-jna/src/waffle/servlet/spi/NegotiateSecurityFilterProvider.java). This class' doFilter method expects a javax.servlet.http.HttpServletRequest and -Response, so the module contains "custom" implementations of these interfaces. Whatever http headers Waffle sets in the response is picked up and added to a Play Result.

The module will send a redirect after the user has successfully authenticated. In this redirect the user's id (Domain\Username) is added to the Play session.

### Configuration
#### Setup
Add the module to your project in Build.scala: 

```
  val appDependencies = Seq(
    ...
    "ntlm-module" % "ntlm-module_2.10" % "1.0-SNAPSHOT"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Local Play Repository" at "file://.../play-2.1.1/repository/local" 
  )
```

In Global.scala:

`object Global extends WithFilters(NtlmFilter()) with GlobalSettings {`

or 
Global.java:

```
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T extends EssentialFilter> Class<T>[] filters() {
        Class[] filters = {NtlmFilter.class};
        return filters;
    }
```

#### Configuration
in application.conf:

`ntlmmodule.challenge = Negotiate`<br/>
Specifies what challenge to use. Options are Negotiate and NTLM. Default is Negotiate.

`ntlmmodule.session.key = ntlmuser`<br/>
Session key used to store the ntlm userid. Default is 'ntlmuser'

`ntlmmodule.encrypt.userid = false`<br/>
Shall the userid be encrypted? Default is false.

`ntlmmodule.savegroups = true`<br/>
Do you care about user's groups? Default is true. Groups are stored in Cache, as there could be too many to fit in the session cookie. You should store them somewhere else and remove them from Cache.

```
ntlmmodule.relevantgroups = [
    Administrators
    "\\Users" 
    "CORP\\IT_Development" 
]
```

Do you only care about certain groups? List them here, comma/newline-separated. Will match groups whose fully qualified name *ends* in any of the given values.
In the second entry ("\\Users") a backslash is included to avoid matching on other groups ending in 'Users' such as "NT AUTHORITY\Authenticated Users". 
The third entry (`)"CORP\\IT_Development") shows a fully qualified name of a group.
Note: Backslash must be escaped and the value in quotes.

```
ntlmmodule.unprotected.uris = [
    /logout
]
```
URIs that does not demand NTLM authentication. Default is 'None'


`ntlmmodule.protected.uris = []`<br/>
URIs that demand NTLM authentication. Default is 'All' 


`ntlmmodule.protect.assets = false`<br/>
Protect assets? Default is false.

`ntlmmodule.post.auth.redirect.uri = /`<br/>
Where to go after successful authentication? Default is originally requested uri 

### Sample applications
Two sample projects are provided. One for [Play Scala](../samples/ntlm-module-sample/), one for [Play Java](../samples/ntlm-module-samplej/).
Check the sample projects for configuration and use

### TODO
* Tests
* Add to central repo
* Error handling (Hard error in some scenarios needs to be handled)

### Disclaimer
I don't know much about Ntlm and I don't know much about [Waffle](https://github.com/dblock/waffle).
I've written this module in an effort to grow my infant Play 2 and Scala skills. There will likely be room for improvements.    Comments and pull requests are welcome.

I've seen a couple of people asking for an Ntlm module for Play. Hopefully this module will allow someone to introduce Play in their organisation.

### Inspiration
<dl>    
  <dt>Writing modules</dt>
  <dd>http://www.objectify.be/wordpress/?p=363</dd>
  <dt>Filter examples</dt>
  <dd>http://jazzy.id.au/default/2013/02/16/understanding_the_play_filter_api.html</dd>
</dl>

### Known Issues
* It seems that Play does not support more than one value for a response header. Because of this you have to choose _either_ NTLM or Negotiate. I've tried adding the header twice and I've tried adding it with two challenges in the header value. I believe both approaches are acceptable according to the http spec. Because of this it's *all or nothing* - the module can't fall back to another authentication mechanism in case the client doesn't support Ntlm (or Negotiate)

* In a JEE servlet filter I would set the userid on the session and forward the request to the controller. In Play I have to do a redirect in order to store a value in the session cookie

### Troubleshooting
Negotiate not working in Firefox? See http://people.redhat.com/mikeb/negotiate/