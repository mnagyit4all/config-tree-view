package main.core;

import main.model.BeanModel;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.ArrayList;
import java.util.List;

public class SpringConfigAstVisitor extends ASTVisitor {

    private boolean isConfiguration = false;
    private final List<String> importedClassNames = new ArrayList<>();
    private final List<BeanModel> declaredBeans = new ArrayList<>();

    @Override
    public boolean visit(TypeDeclaration node) {
        // Kizárólag az elsődleges/fő osztályt vizsgáljuk
        if (node.isInterface()) {
            return false;
        }

        // Modifikátorok és annotációk átvizsgálása
        for (Object modifier : node.modifiers()) {
            if (modifier instanceof Annotation) {
                Annotation annotation = (Annotation) modifier;
                String annotName = annotation.getTypeName().getFullyQualifiedName();

                if ("Configuration".equals(annotName) || "org.springframework.context.annotation.Configuration".equals(annotName)) {
                    this.isConfiguration = true;
                }

                if ("Import".equals(annotName) || "org.springframework.context.annotation.Import".equals(annotName)) {
                    processImportAnnotation(annotation);
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        boolean isBean = false;
        Annotation beanAnnot = null;
        List<Annotation> allAnnotations = new ArrayList<>();

        for (Object modifier : node.modifiers()) {
            if (modifier instanceof Annotation) {
                Annotation annotation = (Annotation) modifier;
                String annotName = annotation.getTypeName().getFullyQualifiedName();
                allAnnotations.add(annotation);

                if ("Bean".equals(annotName) || "org.springframework.context.annotation.Bean".equals(annotName)) {
                    isBean = true;
                }
            }
        }

        if (isBean) {
            String beanName = node.getName().getIdentifier();
            BeanModel beanModel = new BeanModel(beanName);

            for (Annotation annotation : allAnnotations) {
                String fullAnnotName = annotation.getTypeName().getFullyQualifiedName();
                beanModel.addAnnotation("@" + fullAnnotName);
                beanModel.addAnnotation(fullAnnotName);
            }

            declaredBeans.add(beanModel);
        }
        return super.visit(node);
    }

    private void processImportAnnotation(Annotation annotation) {
        if (annotation instanceof SingleMemberAnnotation) {
            SingleMemberAnnotation sma = (SingleMemberAnnotation) annotation;
            extractTypesFromExpression(sma.getValue());
        } else if (annotation instanceof NormalAnnotation) {
            NormalAnnotation na = (NormalAnnotation) annotation;
            for (Object valuePairObj : na.values()) {
                if (valuePairObj instanceof MemberValuePair) {
                    MemberValuePair pair = (MemberValuePair) valuePairObj;
                    if ("value".equals(pair.getName().getIdentifier())) {
                        extractTypesFromExpression(pair.getValue());
                    }
                }
            }
        }
    }

    private void extractTypesFromExpression(Expression expression) {
        if (expression instanceof TypeLiteral) {
            addTypeLiteral((TypeLiteral) expression);
        } else if (expression instanceof ArrayInitializer) {
            ArrayInitializer arrayInit = (ArrayInitializer) expression;
            for (Object exprObj : arrayInit.expressions()) {
                if (exprObj instanceof TypeLiteral) {
                    addTypeLiteral((TypeLiteral) exprObj);
                }
            }
        }
    }

    private void addTypeLiteral(TypeLiteral typeLiteral) {
        ITypeBinding binding = typeLiteral.getType().resolveBinding();
        if (binding != null) {
            importedClassNames.add(binding.getQualifiedName());
        } else {
            // Ha a típus-kötés (binding) nem áll rendelkezésre, fallback a típus nevére
            importedClassNames.add(typeLiteral.getType().toString());
        }
    }

    public boolean isConfiguration() {
        return isConfiguration;
    }

    public List<String> getImportedClassNames() {
        return importedClassNames;
    }

    public List<BeanModel> getDeclaredBeans() {
        return declaredBeans;
    }
}