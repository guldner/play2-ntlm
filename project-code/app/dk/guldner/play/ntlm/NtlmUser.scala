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

case class NtlmUser(val userid: String, val groups: Option[List[String]]) {
  def username = userid.split("\\\\") match {
    case Array(d, u) => u
    case Array(u) => u
    case _ => throw new RuntimeException("Invalid NTLM Userid")
  }
  def domain = userid.split("\\\\") match {
    case Array(d, u) => d
    case Array(u) => ""
    case _ => throw new RuntimeException("Invalid NTLM Userid")
  }
}

object NtlmUser {
  /**
   * Splits a userid (or group) of the form '[domain\]username' in (username, Option(domain))
   */
  def splitId(userid: String): (String, Option[String]) = {
    userid.split("\\\\") match {
    	case Array(d, u) => (u, Some(d))
    	case Array(u) => (u, None)
    	case _ => throw new RuntimeException("Invalid NTLM id")
    }
  }
}