package com.sirdrakeheart.plugin;


import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class DCReasonFilter implements Filter {

	public DCReasonFilter(SirDrakeHeart sirDrakeHeart) {
		
	}

	public boolean isLoggable(LogRecord arg0) {
		if (arg0.getMessage().toLowerCase().contains("disconnect")) {
			Events.filterCheckGeneric = false;
			Events.filterCheckStream = false;
			Events.filterCheckOverflow = false;
			Events.filterCheckTimeout = false;
			Events.filterCheckJoin = false;
			if (arg0.getMessage().toLowerCase().contains("genericreason")) {
				Events.filterCheckGeneric = true;
		        return true;
			}
			if (arg0.getMessage().toLowerCase().contains("endofstream")) {
				Events.filterCheckStream = true;
				return true;
			}
			if (arg0.getMessage().toLowerCase().contains("overflow")) {
				Events.filterCheckOverflow = true;
				return true;
			}
			if (arg0.getMessage().toLowerCase().contains("timeout")) {
				Events.filterCheckTimeout = true;
				return true;
			}
			if (arg0.getMessage().toLowerCase().contains("quitting")) {
				Events.filterCheckQuitting = true;
				return true;
			}
			return true;
	     }
	     if (arg0.getMessage().toLowerCase().contains("entity")) {
	    	 Events.filterCheckJoin = true;
	    	 return true;
	     }
	     return true;
	}
}
