/*
* Copyright 2013 Christian Fleron Güldner
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package dk.guldner.play.ntlm

import play.api.Play
import play.api.cache.Cache
import play.api.libs.Crypto
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Filter
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results
import play.api.mvc.Security.Authenticated
import play.api.mvc.WrappedRequest
import dk.guldner.play.ntlm.waffle.NtlmAuthenticator

object NtlmFilter {
  import Conf._
  val logger = play.api.Logger("ntlmmodule")

  /**
   * Returns an NtlmFilter instance
   */
  def apply() = {
    play.api.Logger.info("Instantiating NtlmFilter")
    new NtlmFilter()
  }

  

  /** Key used when writing user's groups to Cache */
  def groupsCacheKey(userid: String) = NtlmUser.splitId(userid)._1 + NtlmUser.splitId(userid)._2.getOrElse("") + NTLM_GROUPS_CACHE_SUFFIX

  def getLoggedInUser(request: RequestHeader): Option[NtlmUser] = {
    import play.api.Play.current
    val sessionUserid = request.session.get(NTLM_SESSION_KEY)
    val userid = sessionUserid map { u => if(ENCRYPT_USERID) Crypto.decryptAES(u) else u }
    logger.debug("Logged in user's id: " + userid)
    userid map { u =>
    	logger.trace("Reading user's groups from Cache with key: " + groupsCacheKey(u))
    	logger.trace("Logged in user's groups: " + Cache.getAs[List[String]](groupsCacheKey(u)))
    	NtlmUser(u, Cache.getAs[List[String]](groupsCacheKey(u)))
    }
  }

}

class NtlmFilter extends Filter {

  import NtlmFilter._
  import Conf._
  val logger = play.api.Logger("ntlmmodule")

  /**
   * Invoked by framework. Add filter: 'object Global extends WithFilters(NtlmFilter)'
   * See http://www.playframework.com/documentation/2.1.1/ScalaHttpFilters
   *
   * The filter tries to authenticate the user with Negotiate/Ntlm.
   *
   */
  override def apply(next: RequestHeader => Result)(request: RequestHeader): Result = {
    logger.debug("NtlmFilter invoked. Request uri: " + request.uri);
    if (authorizationRequired(request)) {
      logger.debug("Authorization required")
      val maybeAuthorized = NtlmAuthenticator.doNtlmAuthentication(request)
      logger.debug("Result of doing ntlm authentication: " + maybeAuthorized) //an option of either result (401) or ntlmuser(username/groups)
      maybeAuthorized.map { userAuthorized =>
        userAuthorized match {
          case Left(result) => result 
          case Right(ntlmuser) => {
            import play.api.Play.current
            if (request.session.get(NTLM_SESSION_KEY).isDefined)
              next(request)
            else {
            	val sessionUserid = if(ENCRYPT_USERID) Crypto.encryptAES(ntlmuser.userid) else ntlmuser.userid 
            	Results.Redirect(POST_AUTH_REDIRECT_URI.getOrElse(request.uri)).withSession(request.session + (NTLM_SESSION_KEY, sessionUserid))
            }
          }
        }
      }.getOrElse(Results.Unauthorized("Invalid user")) //Should never happen
    } else {
      logger.debug("Authorization NOT required")
      next(request)
    }
  }

  /**
   * Reads configuration and returns whether this request demands authorization
   * 
   * 1) Checks if request.uri starts with '/assets' (returns 'ntlmmodule.protect.assets') 
   * 2) Checks against 'ntlmmodule.unprotected.uris'
   * 3) Checks against 'ntlmmodule.protected.uris'
   * 4) true
   */
  private def authorizationRequired(request: RequestHeader) = {
    if (request.uri.startsWith("/assets")) {
      PROTECT_ASSESTS
    } else {
  	//If unprotected list of uris is given, this takes precedence over protected list. 
      UNPROTECTED_URIS match {
        case Some(l: Seq[String]) =>
          val patt = l.mkString("|").r
          logger.trace("Unprotected uris pattern: " + patt.toString)
          request.uri match {
            case patt() =>
              logger.trace("Request.uri matched unprotected uri"); false
            case _ => true
          }
        case _ => //No 'unprotected uris' specified - check for 'protected uris' 
          PROTECTED_URIS match {
            case Some(l: Seq[String]) =>
              val patt = l.mkString("|").r
              logger.trace("Protected uris pattern: " + patt.toString)
              request.uri match {
                case patt() =>
                  logger.trace("Request.uri matched protected uri"); true
                case _ => false
              }
            case _ => 
              logger.trace("Request.uri matched neither protected, unprotected or assets. Returning default 'true'")
              true 
          }
      }
    }
  }

}

/**
 * NtlmSecurity trait provides method 'NtlmAuthenticated' for access to authenticated ntlm user in wrapped Action
 * Usage:
 *  {{{
 *  def hello = NtlmAuthenticated { user => implicit request =>
 *     Ok(views.html.hello(user))
 *  }
 *  }}}
 */
trait NtlmSecurity {
  def username(request: RequestHeader) = { NtlmFilter.getLoggedInUser(request) }
  def onUnauthorized(request: RequestHeader) = Results.Unauthorized
  def NtlmAuthenticated(f: => NtlmUser => Request[AnyContent] => Result) = {
    Authenticated(username, onUnauthorized)(user => Action(request => f(user)(request)))
  }
}

object Conf {
    import play.api.Play.current
    import scala.collection.JavaConverters._

    def conf = Play.configuration

    val KEY_CHALLENGE = "ntlmmodule.challenge"
    val CHALLENGE = conf.getString(KEY_CHALLENGE).getOrElse("Negotiate")
    val KEY_SAVE_GROUPS = "ntlmmodule.savegroups"
    val SAVE_GROUPS = conf.getBoolean(KEY_SAVE_GROUPS).getOrElse(true)
    val KEY_NTLM_SESSION_KEY = "ntlmmodule.session.key"
    val NTLM_SESSION_KEY = conf.getString(KEY_NTLM_SESSION_KEY).getOrElse("ntlmuser")
    val KEY_ENCRYPT_USERID = "ntlmmodule.encrypt.userid" 
    val ENCRYPT_USERID = conf.getBoolean(KEY_ENCRYPT_USERID).getOrElse(false) //TODO Not implemented
    val KEY_NTLM_GROUPS_CACHE_SUFFIX = "ntlmmodule.groups.key.suffix"
    val NTLM_GROUPS_CACHE_SUFFIX = conf.getString(KEY_NTLM_GROUPS_CACHE_SUFFIX).getOrElse("_ntlm_groups")
    val KEY_RELEVANT_GROUPS = "ntlmmodule.relevantgroups" 
    val RELEVANT_GROUPS = conf.getStringList(KEY_RELEVANT_GROUPS).map(_.asScala)  
    val KEY_PROTECTED_URIS = "ntlmmodule.protected.uris" 
    val PROTECTED_URIS = conf.getStringList(KEY_PROTECTED_URIS).map(_.asScala) 
    val KEY_UNPROTECTED_URIS = "ntlmmodule.unprotected.uris"
    val UNPROTECTED_URIS = conf.getStringList(KEY_UNPROTECTED_URIS).map(_.asScala)  
    val KEY_PROTECT_ASSESTS = "ntlmmodule.protect.assets"
    val PROTECT_ASSESTS = conf.getBoolean(KEY_PROTECT_ASSESTS).getOrElse(false)
    val KEY_POST_AUTH_REDIRECT_URI = "ntlmmodule.post.auth.redirect.uri"
    val POST_AUTH_REDIRECT_URI = conf.getString(KEY_POST_AUTH_REDIRECT_URI)

    def logNtlmModuleConfiguration = {
      val buf = new StringBuilder
      buf ++= "NtlmFilter configuration:\n\n"
      buf ++= " * "; buf ++= KEY_CHALLENGE; buf ++= ": "; buf ++= CHALLENGE; buf += '\n'
      buf ++= " * "; buf ++= KEY_SAVE_GROUPS; buf ++= ": "; buf ++= SAVE_GROUPS.toString; buf += '\n'
      buf ++= " * "; buf ++= KEY_NTLM_SESSION_KEY; buf ++= ": "; buf ++= NTLM_SESSION_KEY; buf += '\n'
      buf ++= " * "; buf ++= KEY_ENCRYPT_USERID; buf ++= ": "; buf ++= ENCRYPT_USERID.toString; buf += '\n'
//      buf ++= " * "; buf ++= KEY_NTLM_GROUPS_CACHE_SUFFIX; buf ++= ": "; buf ++= CONF_NTLM_GROUPS_CACHE_SUFFIX; buf += '\n'
      buf ++= " * "; buf ++= KEY_RELEVANT_GROUPS; buf ++= ": "; buf ++= RELEVANT_GROUPS.toString; buf += '\n'
      buf ++= " * "; buf ++= KEY_PROTECTED_URIS; buf ++= ": "; buf ++= PROTECTED_URIS.toString; buf += '\n'
      buf ++= " * "; buf ++= KEY_UNPROTECTED_URIS; buf ++= ": "; buf ++= UNPROTECTED_URIS.toString; buf += '\n'
      buf ++= " * "; buf ++= KEY_PROTECT_ASSESTS; buf ++= ": "; buf ++= PROTECT_ASSESTS.toString; buf += '\n'
      buf ++= " * "; buf ++= KEY_POST_AUTH_REDIRECT_URI; buf ++= ": "; buf ++= POST_AUTH_REDIRECT_URI.toString; buf += '\n'
      play.api.Logger.info(buf.toString)
    }
  }