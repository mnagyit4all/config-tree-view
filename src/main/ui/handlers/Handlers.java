package main.ui.handlers;

import main.core.GraphBuilderService;
import main.model.ConfigGraph;
import main.ui.dialogs.DiscoveryModeDialog;
import main.ui.views.SpringConfigViewPart;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Context menu handler a "Show config tree" opcióhoz.
 */
public class Handlers extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);

        if (!(selection instanceof IStructuredSelection) || selection.isEmpty()) {
            return null;
        }

        Object selectedElement = ((IStructuredSelection) selection).getFirstElement();
        ICompilationUnit compilationUnit = extractCompilationUnit(selectedElement);

        if (compilationUnit == null) {
            return null;
        }

        String elementName = compilationUnit.getElementName();
        if (!elementName.endsWith("Config.java")) {
            return null;
        }

        DiscoveryModeDialog dialog = new DiscoveryModeDialog(HandlerUtil.getActiveShell(event));
        if (dialog.open() == Window.OK) {
            DiscoveryModeDialog.DiscoveryMode mode = dialog.getSelectedMode();

            GraphBuilderService graphService = new GraphBuilderService();
            ConfigGraph graph = graphService.buildGraph(compilationUnit, mode);

            // Nézet (ViewPart)
            try {
                IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
                SpringConfigViewPart view = (SpringConfigViewPart) page.showView(SpringConfigViewPart.ID);
                view.updateGraph(graph);
            } catch (Exception e) {
                throw new ExecutionException("Hiba a ConfigGraphViewPart megjelenítése során", e);
            }
        }

        return null;
    }

    private ICompilationUnit extractCompilationUnit(Object element) {
        if (element instanceof ICompilationUnit) {
            return (ICompilationUnit) element;
        } else if (element instanceof IFile) {
            IFile file = (IFile) element;
            if ("java".equalsIgnoreCase(file.getFileExtension())) {
                return JavaCore.createCompilationUnitFrom(file);
            }
        }
        return null;
    }
}