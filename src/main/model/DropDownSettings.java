package main.model;

import org.eclipse.jface.preference.IPreferenceStore;
import main.SpringConfigVisualizerPlugin;

public class DropDownSettings {
	
	public static final String SHOW_BEAN_DETAILS = "showBeanDetails";
	public static final String SHOW_DETAILS = "showDetails";
	public static final String SHOW_SEARCH_BAR = "showSearchBar";

	private static IPreferenceStore getStore() {
		return SpringConfigVisualizerPlugin.getDefault().getPreferenceStore();
	}
	
	public DropDownSettings() {}
	
	public boolean getShowBeanDetails() {
		return getStore().getBoolean(SHOW_BEAN_DETAILS);
	}
	
	public boolean getShowDetails() {
		return getStore().getBoolean(SHOW_DETAILS);
	}
	
	public boolean getShowSearchBar() {
		return getStore().getBoolean(SHOW_SEARCH_BAR);
	}
	
	public void setShowBeanDetails(boolean b) {
		getStore().setValue(SHOW_BEAN_DETAILS, b);
	}
	
	public void setShowDetails(boolean b) {
		getStore().setValue(SHOW_DETAILS, b);
	}
	
	public void setShowSearchBar(boolean b) {
		getStore().setValue(SHOW_SEARCH_BAR, b);
	}
	
	public static void setupDefault() {
		IPreferenceStore store = getStore();
		store.setDefault(SHOW_BEAN_DETAILS, false);
		store.setDefault(SHOW_DETAILS, false);
		store.setDefault(SHOW_SEARCH_BAR, false);
	}
}