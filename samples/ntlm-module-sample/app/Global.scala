import play.api.mvc.WithFilters
import dk.guldner.play.ntlm.NtlmFilter
import play.api.GlobalSettings
import play.api.Application
import dk.guldner.play.ntlm.Conf

object Global extends WithFilters(NtlmFilter()) with GlobalSettings {

  override def onStart(app: Application) {
    play.api.Logger.info("Application has started")
    //Conf.logNtlmModuleConfiguration
  }  
} 