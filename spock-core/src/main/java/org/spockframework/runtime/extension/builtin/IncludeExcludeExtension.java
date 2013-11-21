/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.regex.Pattern;

import org.spockframework.runtime.extension.AbstractGlobalExtension;
import org.spockframework.runtime.model.*;

import spock.config.IncludeExcludeCriteria;
import spock.config.RunnerConfiguration;

@SuppressWarnings("UnusedDeclaration")
public class IncludeExcludeExtension extends AbstractGlobalExtension {
  private RunnerConfiguration config;

  public void visitSpec(SpecInfo spec) {
    handleSpecIncludes(spec, config.include);
    handleSpecExcludes(spec, config.exclude);
    if (spec.isExcluded())
      excludeAllFeatures(spec);

    handleFeatureIncludes(spec, config.include);
    handleFeatureExcludes(spec, config.exclude);
    if (spec.isExcluded() && !allFeaturesExcluded(spec)) 
      spec.setExcluded(false);
  }

  private void handleSpecIncludes(SpecInfo spec, IncludeExcludeCriteria criteria) {
    if (criteria.isEmpty()) return;

    if (!hasAnyAnnotation(spec, criteria.annotations)
        && !hasAnyBaseClass(spec, criteria.baseClasses)
        && !matchesAnyPattern(spec, criteria.patterns))
      spec.setExcluded(true);
  }

  private void handleSpecExcludes(SpecInfo spec, IncludeExcludeCriteria criteria) {
    if (criteria.isEmpty()) return;

    if (hasAnyAnnotation(spec, criteria.annotations)
        || hasAnyBaseClass(spec, criteria.baseClasses)
        || matchesAnyPattern(spec, criteria.patterns))
      spec.setExcluded(true);
  }

  // in contrast to the three other handleXXX methods, this one includes nodes
  private void handleFeatureIncludes(SpecInfo spec, IncludeExcludeCriteria criteria) {
    if (criteria.isEmpty()) return;

    for (FeatureInfo feature : spec.getAllFeatures())
      if (hasAnyAnnotation(feature.getFeatureMethod(), criteria.annotations)
          || matchesAnyPattern(feature.getFeatureMethod(), criteria.patterns))
        feature.setExcluded(false);
  }

  private void handleFeatureExcludes(SpecInfo spec, IncludeExcludeCriteria criteria) {
    if (criteria.isEmpty()) return;
    
    for (FeatureInfo feature : spec.getAllFeatures())
      if (hasAnyAnnotation(feature.getFeatureMethod(), criteria.annotations)
          || matchesAnyPattern(feature.getFeatureMethod(), criteria.patterns))
        feature.setExcluded(true);
  }

  private void excludeAllFeatures(SpecInfo spec) {
    for (FeatureInfo feature : spec.getAllFeatures())
      feature.setExcluded(true);
  }
  
  private boolean allFeaturesExcluded(SpecInfo spec) {
    for (FeatureInfo feature : spec.getAllFeatures())
      if (!feature.isExcluded()) return false;

    return true;
  }

  private boolean hasAnyAnnotation(NodeInfo<?, ?> node, List<Class<? extends Annotation>> annotationClasses) {
    for (Class<? extends Annotation> annClass : annotationClasses)
      if (node.getReflection().isAnnotationPresent(annClass))
        return true;

    return false;
  }

  private boolean hasAnyBaseClass(SpecInfo spec, List<Class<?>> baseClasses) {
    for (Class<?> clazz : baseClasses)
      if (clazz.isAssignableFrom(spec.getReflection()))
        return true;

    return false;
  }

  private boolean matchesAnyPattern(NodeInfo<?, ?> node, List<Pattern> patterns) {
    if (matchesAnyPattern(node.getName(), patterns))
      return true;

    for (Annotation ann : node.getAnnotations())
      if (matchesAnyPattern(ann.annotationType().getSimpleName(), patterns))
        return true;

    return false;
  }

  private boolean matchesAnyPattern(String s, List<Pattern> patterns) {
    for (Pattern p : patterns)
      if (p.matcher(s).matches())
        return true;

    return false;
  }
}

