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

import javax.servlet.http.HttpServletRequestWrapper
import java.util.Enumeration
import java.util.Map
import javax.servlet.ServletInputStream
import java.io.BufferedReader
import javax.servlet.http.HttpServletRequest
import play.api.mvc.Request
import play.api.mvc.RequestHeader
import java.io.IOException
import java.util.Locale
import javax.servlet.RequestDispatcher
import java.io.UnsupportedEncodingException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpSession
import java.security.Principal


class RequestFacade(val requestHeader: RequestHeader) extends HttpServletRequest {

  	val logger = play.api.Logger("ntlmmodule")

	/* ************************ */
	/* Methods called by Waffle */
	/* ************************ */
	override def getHeader(name: String): String = {
	  logger.trace("Getting header '" + name + "': " + requestHeader.headers.get(name).getOrElse("<<not found>>"))
	  requestHeader.headers.get(name).getOrElse(null) //?
	}

	override def getContentType = requestHeader.contentType.getOrElse(null);
	override def getRemoteAddr = requestHeader.remoteAddress
	override def getRemoteHost = requestHeader.host
	override def getRemotePort = 0 //?
	override def getMethod = requestHeader.method

	/* ********************** */
	/* Unimplemented methods  */
	/* ********************** */
	override def getAttribute(str: String) = ???
	override def getAttributeNames(): Enumeration[String] = ???
	override def getCharacterEncoding = ???
	override def getContentLength = ???
	@throws(classOf[IOException])
	override def getInputStream: ServletInputStream = ???
	override def getLocalAddr = ???
	override def getLocalName = ???
	override def getLocalPort = ???
	override def getLocale: Locale = ???
	override def getLocales: Enumeration[Locale] = ???
	override def getParameter(str: String) = ???
	override def getParameterMap: Map[String, Array[String]] = ???
	override def getParameterNames: Enumeration[String] = ???
	override def getParameterValues(str: String): Array[String] = ???
	override def getProtocol = ???
	override def getReader: BufferedReader = ???
	override def getRealPath(str: String) = ???
	override def getRequestDispatcher(str: String): RequestDispatcher = ???
	override def getScheme = ???
	override def getServerName = ???
	override def getServerPort = ???
	override def isSecure = ???
	override def removeAttribute(str: String) = ???
	override def setAttribute(str: String, value: AnyRef) = ???
	@throws(classOf[UnsupportedEncodingException])
	override def setCharacterEncoding(str: String) = ???
	override def getAuthType = ???
	override def getContextPath = ???
	override def getCookies: Array[Cookie] = ???
	override def getDateHeader(str: String): Long = ???
	override def getHeaderNames: Enumeration[String] = ???
	override def getHeaders(str: String): Enumeration[String] = ???
	override def getIntHeader(str: String) = ???
	override def getPathInfo = ???
	override def getPathTranslated = ???
	override def getQueryString = ???
	override def getRemoteUser = ???
	override def getRequestURI = ???
	override def getRequestURL: StringBuffer = ??? 
	override def getRequestedSessionId = ???
	override def getServletPath = ???
	override def getSession: HttpSession = ???
	override def getSession(create: Boolean): HttpSession = ???
	override def getUserPrincipal: Principal = ???
	override def isRequestedSessionIdFromCookie = ???
	override def isRequestedSessionIdFromURL = ???
	override def isRequestedSessionIdFromUrl = ???
	override def isRequestedSessionIdValid = ???
	override def isUserInRole(str: String) = ???
	//servlet 3.0
//	override def authenticate(x$1: javax.servlet.http.HttpServletResponse): Boolean = ???
//	override def getPart(x$1: String): javax.servlet.http.Part = ???
//	override def getParts(): java.util.Collection[javax.servlet.http.Part] = ???
//	override def login(x$1: String,x$2: String): Unit = ???
//	override def logout(): Unit = ???
//	override def getAsyncContext(): javax.servlet.AsyncContext = ???
//	override def getDispatcherType(): javax.servlet.DispatcherType = ???
//	override def getServletContext(): javax.servlet.ServletContext = ???
//	override def isAsyncStarted(): Boolean = ???
//	override def isAsyncSupported(): Boolean = ???
//	override def startAsync(x$1: javax.servlet.ServletRequest,x$2: javax.servlet.ServletResponse): javax.servlet.AsyncContext = ???
//	override def startAsync(): javax.servlet.AsyncContext  = ???
}