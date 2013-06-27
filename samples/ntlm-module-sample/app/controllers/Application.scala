package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import dk.guldner.play.ntlm._
import dk.guldner.play.ntlm.Conf._
import play.api.libs.Crypto

object Application extends Controller with NtlmSecurity {

  //Use the method provided in NtlmSecurity trait to get access to the ntlm user object 
  def index = NtlmAuthenticated { ntlmUser =>
    implicit request =>
      Ok(views.html.index(ntlmUser))
  }

  def hello = Action { request =>
    val userid = getUseridFromSession(request)
    Ok(views.html.hello(userid.getOrElse("Unauthorized stranger")))
  }

  //If you care about user's groups, store them somewhere else and remove them from Cache.  
  def removeGroups = Action { implicit request =>
    val userid = getUseridFromSession(request)
    val key = userid map { u => NtlmFilter.groupsCacheKey(u) }
    key map { k =>
      Logger.info("Removing these groups from cache: " + Cache.get(k))
      Cache.remove(k)
    }
    Ok(views.html.removegroups())
  }

  def logout = NtlmAuthenticated { ntlmUser =>
    implicit request =>
      Ok(views.html.logout(ntlmUser)).withSession{ session - NTLM_SESSION_KEY}
  }

  //Retrieve the userid from the session. Decrypt if necessary
  def getUseridFromSession(implicit request: Request[AnyContent]) = {
    request.session.get(NTLM_SESSION_KEY) map { u => if (ENCRYPT_USERID) Crypto.decryptAES(u) else u }
  }
}