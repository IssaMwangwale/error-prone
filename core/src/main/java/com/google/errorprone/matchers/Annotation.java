/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.matchers;

import com.google.errorprone.VisitorState;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import java.util.List;

/**
 * Matches if the given annotation matcher matches all of or any of the annotations on the tree
 * node.
 *
 * @author eaftan@google.com (Eddie Aftandilian)
 */
public class Annotation<T extends Tree> implements Matcher<T> {

  private final boolean anyOf;
  private final Matcher<AnnotationTree> annotationMatcher;

  public Annotation(boolean anyOf, Matcher<AnnotationTree> annotationMatcher) {
    this.annotationMatcher = annotationMatcher;
    this.anyOf = anyOf;
  }

  @Override
  public boolean matches(T tree, VisitorState state) {

    List<? extends AnnotationTree> annotations;
    switch (tree.getKind()) {
      case CLASS:
        annotations = ((ClassTree) tree).getModifiers().getAnnotations();
        break;
      case VARIABLE:
        annotations = ((VariableTree) tree).getModifiers().getAnnotations();
        break;
      case METHOD:
        annotations = ((MethodTree) tree).getModifiers().getAnnotations();
        break;
      case COMPILATION_UNIT:
        annotations = ((CompilationUnitTree) tree).getPackageAnnotations();
        break;
      default:
        throw new IllegalArgumentException("Cannot access annotations from tree of kind "
            + tree.getKind());
    }

    for (AnnotationTree annotation : annotations) {
      boolean matches = annotationMatcher.matches(annotation, state);
      if (anyOf && matches) {
        return true;
      }
      if (!anyOf && !matches) {
        return false;
      }
    }
    if (anyOf) {
      return false;
    } else {
      // In allOf case, return true only if there was at least one annotation.
      return annotations.size() >= 1;
    }
  }
}
