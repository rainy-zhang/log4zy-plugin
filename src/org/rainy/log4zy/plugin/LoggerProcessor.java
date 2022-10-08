package org.rainy.log4zy.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightFieldBuilder;
import com.intellij.psi.impl.light.LightModifierList;

import java.util.Collections;
import java.util.List;

public class LoggerProcessor {

    private static final String LOGGER_TYPE = "org.rainy.log4zy.Logger";
    private static final String LOGGER_SYMBOL = "logger";
    private static final String ANNOTATION_NAME = "org.rainy.log4zy.Log4zy";

    public static List<? super PsiElement> processor(PsiClass psiClass) {
        PsiAnnotation annotation = getAnnotation(psiClass);
        if (annotation == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(createLoggerField(psiClass, annotation));
    }

    private static PsiAnnotation getAnnotation(PsiClass psiClass) {
        PsiAnnotation[] annotations = psiClass.getAnnotations();
        String shortName = StringUtil.getShortName(ANNOTATION_NAME);
        for (PsiAnnotation annotation : annotations) {
            PsiJavaCodeReferenceElement referenceElement = annotation.getNameReferenceElement();
            if (referenceElement != null && shortName.equals(referenceElement.getReferenceName())) {
                return annotation;
            }
        }
        return null;
    }

    private static PsiElement createLoggerField(PsiClass psiClass, PsiAnnotation annotation) {
        Project project = psiClass.getProject();
        PsiManager manager = psiClass.getContainingFile().getManager();
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);

        PsiType psiType = elementFactory.createTypeFromText(LOGGER_TYPE, psiClass);

        LightFieldBuilder loggerField = new LightFieldBuilder(manager, LOGGER_SYMBOL, psiType);
        LightModifierList modifierList = (LightModifierList) loggerField.getModifierList();
        modifierList.addModifier(PsiModifier.FINAL);
        modifierList.addModifier(PsiModifier.PRIVATE);

        loggerField.setContainingClass(psiClass);
        loggerField.setNavigationElement(annotation);

        return loggerField;
    }

}
