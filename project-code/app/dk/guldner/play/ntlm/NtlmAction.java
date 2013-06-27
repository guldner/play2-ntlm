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
package dk.guldner.play.ntlm;

import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

/**
 * Use this in action composition to get the ntlm user id
 * 
 * This action does not trigger authentication if unauthenticated!
 * 
 * @author cfgul
 *
 */
public class NtlmAction extends Action.Simple {

	@Override
	public Result call(Context ctx) throws Throwable {
		String user = new NtlmAuth().getUsername(ctx);
		if(user != null) {
			ctx.args.put(Conf.NTLM_SESSION_KEY(), user);
		}
		return delegate.call(ctx);
	}

}


