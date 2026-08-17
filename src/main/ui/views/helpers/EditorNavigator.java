package main.ui.views.helpers;

import main.model.ConfigNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IWorkbenchPage;

public class EditorNavigator {
    public static void openInEditor(IWorkbenchPage page, ConfigNode configNode) {
        if (configNode == null) return;
        try {
            if (configNode.getCompilationUnit() != null) {
                JavaUI.openInEditor(configNode.getCompilationUnit());
            } else if (configNode.getFile() != null) {
                IFile file = configNode.getFile();
                org.eclipse.ui.ide.IDE.openEditor(page, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}