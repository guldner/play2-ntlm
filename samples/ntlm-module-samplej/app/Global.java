import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import dk.guldner.play.ntlm.*;


public class Global extends GlobalSettings {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T extends EssentialFilter> Class<T>[] filters() {
		Class[] filters = {NtlmFilter.class};
		return filters;
	}
}
