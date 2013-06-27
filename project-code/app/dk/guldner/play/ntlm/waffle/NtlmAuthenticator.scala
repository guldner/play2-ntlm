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
package dk.guldner.play.ntlm.waffle


import scala.annotation.implicitNotFound
import play.api.Play.current
import play.api.cache.Cache
import play.api.http.HeaderNames
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results
import waffle.servlet.NegotiateSecurityFilter
import waffle.servlet.spi.SecurityFilterProviderCollection
import waffle.util.AuthorizationHeader
import waffle.windows.auth.IWindowsAuthProvider
import waffle.windows.auth.IWindowsIdentity
import waffle.windows.auth.PrincipalFormat
import waffle.windows.auth.impl.WindowsAuthProviderImpl
import dk.guldner.play.ntlm.NtlmFilter
import waffle.windows.auth.IWindowsAccount
import com.typesafe.config.ConfigValue
import dk.guldner.play.ntlm.NtlmUser
import scala.collection.mutable.Buffer
import dk.guldner.play.ntlm.Conf

/**
 * Uses Waffle to perform NTLM/Negotiate authentication.
 * Wraps the play RequestHeader in a HttpServletRequest and passes this to the waffle provider together with a dummy HttpServletResponse that "catches" the headers set by waffle.
 * Headers set by waffle is then re-written to a play result.
 *
 * See <a href='http://dblock.github.io/waffle/'>Waffle</a>
 */
object NtlmAuthenticator {
  
  import Conf._
  val logger = play.api.Logger("ntlmmodule")

  logger.debug("Initializing NtlmAuthenticator. Setting waffle providers")
  /* waffle stuff. Stolen from the Tomcat filter sample */
  val _auth: IWindowsAuthProvider = new WindowsAuthProviderImpl()
  //TODO Shouldn't use Default provider, since it includes also 'Basic' 
  val _providers = new SecurityFilterProviderCollection(_auth) //default provider with Negotiate and Basic  
  //  var _principalFormat: PrincipalFormat = PrincipalFormat.fqn
  //  var _roleFormat: PrincipalFormat = PrincipalFormat.fqn
  //  var _allowGuestLogin: Boolean = true
  //  var _impersonate: Boolean = false
  //  val PRINCIPAL_SESSION_KEY = classOf[NegotiateSecurityFilter].getName() + ".PRINCIPAL"

  /**
   * Takes a play.api.mvc.Request and wraps it in a RequestFacade that implements javax.servlet.http.HttpServletRequest needed by waffle.util.AuthorizationHeader
   * Returns either a result (401 'Unauthorized') or an NtlmUser(userid, groups)
   */
  def doNtlmAuthentication(request: RequestHeader): Option[Either[Result, NtlmUser]] = {
    val httpServletRequest = new RequestFacade(request);
    val httpServletResponse = new ResponseFacade();

    val authorizationHeader: AuthorizationHeader = new AuthorizationHeader(httpServletRequest);
    logger.trace("'Authorization' header received: " + authorizationHeader.getHeader())

    if (loggedInAlready(request)) {
      logger.debug("User already authenticated")
      val ntlmUser = NtlmFilter.getLoggedInUser(request).get
      Option(Right(ntlmUser))
    } else {
      //must authenticate 
      logger.trace("User not logged in. Will try to extract windows identity")
      if (!authorizationHeader.isNull()) {
        try {
          val windowsIdentity: IWindowsIdentity = _providers.doFilter(httpServletRequest, httpServletResponse)
          if (windowsIdentity == null) {
            logger.debug("Failed authentication. WindowsIdentity is null. Send 401 'Unauthorized' with Waffle applied reponse headers")
            Option(Left(sendWaffleUnauthorized(httpServletResponse)))
          } else {
            logger.debug(String.format("Authentication successfull. WindowsIdentity is %s", windowsIdentity.getFqn()))
            logger.trace("User's groups will be " + (if (SAVE_GROUPS) "stored" else "ignored"))
            logger.trace("Keeping these groups: " + RELEVANT_GROUPS)
            val groups: Option[List[String]] = if (SAVE_GROUPS) Option(getGroupStrings(windowsIdentity.getGroups, RELEVANT_GROUPS)) else None
            if (groups.isDefined) {
              logger.trace("Writing groups to Cache with key: " + NtlmFilter.groupsCacheKey(windowsIdentity.getFqn()))
              Cache.set(NtlmFilter.groupsCacheKey(windowsIdentity.getFqn()), groups.getOrElse(Nil))
            }
            Option(Right(NtlmUser(windowsIdentity.getFqn(), groups)))
          }
        } catch {
          case e: Exception => {
            logger.error("Error authenticating user: " + e.getMessage() + ". Send 401 'Unauthorized'")
            Option(Left(sendUnauthorized()))
          }
        }
      } else {
        //Send 'Unauthorized'
        logger.trace("Authorization header is null. Send 401 'Unauthorized'")
        Option(Left(sendUnauthorized()))
      }
    }
  }

  /**
   * Sends an Unauthorized response with headers
   * <ul><li>Connection: keep-alive</li>
   * <li>WWW-Authenticate: Negotiate Negotiate (or NTLM) </li></ul>
   */
  def sendUnauthorized() = {
    logger.debug("Sending 401 'Unauthorized' with WWW-Authenticate : " + CHALLENGE)
    Results.Unauthorized("Unauthorized")
      .withHeaders(HeaderNames.CONNECTION -> "Keep-Alive")
      .withHeaders(HeaderNames.WWW_AUTHENTICATE -> CHALLENGE)
  }

  /**
   * Gets headers set by waffle in dummy servlet response and adds them to a play 'Unauthorized' result
   */
  def sendWaffleUnauthorized(response: ResponseFacade) = {
    logger.debug("Response headers set by waffle: " + response.headers.toString)
    var result = Results.Unauthorized("Unauthorized")
    for ((k, s) <- response.headers; v <- s) {
      logger.trace("Adding header to play result from waffle: " + k + " -> " + v)
      result = result.withHeaders((k, v))
    }
    result
  }

  /**
   * Determines whether the user has already been authenticated
   * Check's if the session contains an 'ntlmuser'
   */
  private def loggedInAlready(request: RequestHeader): Boolean = {
    request.session.get(NTLM_SESSION_KEY).isDefined
  }

  private def getGroupStrings(groups: Array[IWindowsAccount], relevantGroups: Option[Buffer[String]]) = {
    import scala.collection.JavaConverters._
    val groupStrings = groups.filter { account =>
      relevantGroups.isEmpty || { val relGroups = relevantGroups.get; relGroups.exists(account.getFqn().endsWith(_)) }
    }.map { account =>
      account.getFqn()
    }
    groupStrings.toList
  }
}
  
  
