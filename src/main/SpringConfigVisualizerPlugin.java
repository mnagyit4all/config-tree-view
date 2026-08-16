package main;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * A bővítmény Activator osztálya, amely az Eclipse plugin életciklusát kezeli.
 */
public class SpringConfigVisualizerPlugin extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "SpringConfigVisualizerPlugin";

    private static SpringConfigVisualizerPlugin plugin;

    public SpringConfigVisualizerPlugin() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Visszaadja a bővítmény egyetlen (singleton) példányát.
     */
    public static SpringConfigVisualizerPlugin getDefault() {
        return plugin;
    }
}