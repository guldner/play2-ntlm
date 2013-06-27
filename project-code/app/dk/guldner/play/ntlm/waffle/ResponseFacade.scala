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

import javax.servlet.http.HttpServletResponse
import java.util.Collection
import javax.servlet.ServletOutputStream
import java.io.PrintWriter
import play.api.mvc.Result
import java.io.IOException
import play.api.mvc.PlainResult
import play.api.http.Status
import play.api.mvc.Results
import collection.mutable.{ HashMap, MultiMap, Set }

/**
 * Response is used in waffle's NegotiateSecurityFilterProvider.doFilter only to set status and set headers
 */
class ResponseFacade() extends HttpServletResponse {

  val logger = play.api.Logger("ntlmmodule")

  //"Catch" status and headers set by Waffle provider 
  var statusCode: Int = 200
  val headers = new HashMap[String, Set[String]] with MultiMap[String, String]

  /* ************************ */
  /* Methods called by Waffle */
  /* ************************ */
  override def setHeader(name: String, value: String) = {
    logger.trace("response.setHeader called by waffle with: " + name + ", " + value);
    headers.removeBinding(name, value)
    headers.addBinding(name, value)
  }

  override def addHeader(name: String, value: String) {
    logger.trace("response.addHeader called by waffle with: " + name + ", " + value);
    headers.addBinding(name, value)
  }

  override def setStatus(status: Int) = {
    logger.trace("response.setStatus called by waffle with: " + status);
    statusCode = status
  }

  @throws(classOf[IOException])
  override def sendError(status: Int) = {
    logger.trace("response.sendError called by waffle with: " + status);
    statusCode = status;
  }

  @throws(classOf[IOException])
  override def flushBuffer() = {
    logger.trace("response.flushBuffer called by waffle (ignoring this)");
  }

  /* ********************** */
  /* Unimplemented methods  */
  /* ********************** */
  def addCookie(x$1: javax.servlet.http.Cookie): Unit = ???
  def addDateHeader(x$1: String, x$2: Long): Unit = ???
  def addIntHeader(x$1: String, x$2: Int): Unit = ???
  def containsHeader(x$1: String): Boolean = ???
  def encodeRedirectURL(x$1: String): String = ???
  def encodeRedirectUrl(x$1: String): String = ???
  def encodeURL(x$1: String): String = ???
  def encodeUrl(x$1: String): String = ???
  def getHeader(x$1: String): String = ???
  def getHeaderNames(): java.util.Collection[String] = ???
  def getHeaders(x$1: String): java.util.Collection[String] = ???
  def getStatus(): Int = ???
  def sendError(x$1: Int, x$2: String): Unit = ???
  def sendRedirect(x$1: String): Unit = ???
  def setDateHeader(x$1: String, x$2: Long): Unit = ???
  def setIntHeader(x$1: String, x$2: Int): Unit = ???
  def setStatus(x$1: Int, x$2: String): Unit = ???
  def getBufferSize(): Int = ???
  def getCharacterEncoding(): String = ???
  def getContentType(): String = ???
  def getLocale(): java.util.Locale = ???
  def getOutputStream(): javax.servlet.ServletOutputStream = ???
  def getWriter(): java.io.PrintWriter = ???
  def isCommitted(): Boolean = ???
  def reset(): Unit = ???
  def resetBuffer(): Unit = ???
  def setBufferSize(x$1: Int): Unit = ???
  def setCharacterEncoding(x$1: String): Unit = ???
  def setContentLength(x$1: Int): Unit = ???
  def setContentType(x$1: String): Unit = ???
  def setLocale(x$1: java.util.Locale): Unit = ???

}