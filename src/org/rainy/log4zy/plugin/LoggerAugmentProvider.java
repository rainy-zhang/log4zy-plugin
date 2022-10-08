package org.rainy.log4zy.plugin;

import com.intellij.openapi.util.RecursionGuard;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LoggerAugmentProvider extends PsiAugmentProvider {

    @NotNull
    @Override
    protected <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {
        List<Psi> emptyResult = Collections.emptyList();
        if (type != PsiField.class) {
            return emptyResult;
        }

        if (!element.isValid()) {
            return emptyResult;
        }

        PsiClass psiClass = (PsiClass) element;
        if (psiClass.isAnnotationType() || psiClass.isInterface()) {
            return emptyResult;
        }

        List<Psi> cachedValue = CachedValuesManager.getCachedValue(element, new LoggerCacheValueProvider<>(psiClass));
        return cachedValue != null ? cachedValue : emptyResult;
    }


    private static class LoggerCacheValueProvider<Psi> implements CachedValueProvider<List<Psi>> {

        private final PsiClass psiClass;
        private final RecursionGuard<PsiClass> recursionGuard;

        private LoggerCacheValueProvider(PsiClass psiClass) {
            this.psiClass = psiClass;
            this.recursionGuard = RecursionManager.createGuard("log4zy.augment.field");
        }

        @Nullable
        @Override
        public Result<List<Psi>> compute() {
            return recursionGuard.doPreventingRecursion(psiClass, true, this::getPsis);
        }

        private Result<List<Psi>> getPsis() {
            List<? super PsiElement> elements = LoggerProcessor.processor(psiClass);
            return Result.create(
                    elements.stream().map(e -> (Psi) e).collect(Collectors.toList()),
                    psiClass
            );
        }

    }


}
