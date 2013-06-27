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

import play.api.libs.Crypto;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security.Authenticator;

public class NtlmAuth extends Authenticator {
        
        /**
         * Retrieves the ntlm userid from the session cookie.
         *
         * @return null if the user is not authenticated.
         */
        public String getUsername(Context ctx) {
            String userid = ctx.session().get(Conf.NTLM_SESSION_KEY()); 
            if (userid != null && Conf.ENCRYPT_USERID()) { userid = Crypto.decryptAES(userid);}
            return userid;
        }
        
       
        /**
         * Generates an alternative result if the user is not authenticated; the default a simple '401 Not Authorized' page.
         */
        public Result onUnauthorized(Context ctx) {
            return unauthorized(views.html.defaultpages.unauthorized.render());
        }
        
    }