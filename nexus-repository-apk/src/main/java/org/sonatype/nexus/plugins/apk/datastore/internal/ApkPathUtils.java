/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2019-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.apk.datastore.internal;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ApkPathUtils
{
  private static String pkgVersionRegex ="([0-9][0-9A-z\\._]*)(-[r][0-9]+)";

  /**
   * * Returns the name from a {@link TokenMatcher.State}.
   */
  public static String path(final TokenMatcher.State state) {
    return match(state, "path");
  }

  public static String buildIndexPath(final String path) {
    return '/' + path + "/APKINDEX.tar.gz";
  }

  public static String version(final TokenMatcher.State state) {
    Pattern pattern = Pattern.compile(pkgVersionRegex);

    String filename = match(state, "filename");
    Matcher matcher = pattern.matcher(filename);

    if (matcher.find()) {
      MatchResult matchResult = matcher.toMatchResult();

      return matchResult.group(0);
    }
    return "";
  }

  /**
   * Utility method encapsulating getting a particular token by name from a matcher, including preconditions.
   */
  private static String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  /**
   * Builds a path to an archive for a particular path and filename.
   */
  public static String path(final String path, final String filename) {
    return path + "/" + filename;
  }

  public static String name(final TokenMatcher.State state) {

    // See: https://wiki.alpinelinux.org/wiki/APKBUILD_Reference#pkgver
    Pattern pattern = Pattern.compile("(.*)-".concat(pkgVersionRegex));

    String filename = match(state, "filename");
    Matcher matcher = pattern.matcher(filename);

    if (matcher.find()) {
      MatchResult matchResult = matcher.toMatchResult();

      return matchResult.group(1);
    }
    return "";
  }

  /**
   * Returns the filename from a {@link TokenMatcher.State}.
   */
  public static String filename(final TokenMatcher.State state) {
    return match(state, "filename");
  }

  /**
   * Builds a path to the archive for a particular path
   */
  public static String archivePath(final String path, final String filename, final String version) {
    return '/' + path + "/" + filename + "-" + version + ".apk";
  }
  /**
   * Returns the {@link TokenMatcher.State} for the content.
   */
  public static TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
  }

  private ApkPathUtils() {
    // empty
  }
}
