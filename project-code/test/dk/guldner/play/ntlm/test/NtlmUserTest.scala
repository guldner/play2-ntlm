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
package dk.guldner.play.ntlm.test

import org.junit.Assert._
import org.junit.Test;
import dk.guldner.play.ntlm.NtlmUser

class NtlmUserTest {

	@Test
	def test() {
		val id = "domain\\bobhope"; val groups = List("actors")
		val user = new NtlmUser(id, Option(groups))
		assertEquals(user.userid, id)
		assertTrue(user.groups.isDefined)
		assertTrue(user.groups.get.contains("actors"))
		assertEquals(user.domain, "domain")
		assertEquals(user.username, "bobhope")
	}

}
