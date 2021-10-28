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

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.plugins.apk.datastore.ApkContentFacet;
import org.sonatype.nexus.plugins.apk.internal.AssetKind;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.content.facet.ContentProxyFacetSupport;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

@Named
public class ApkProxyFacetImpl
    extends ContentProxyFacetSupport
{
  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = ApkPathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        return content().get(ApkPathUtils.archivePath(ApkPathUtils.path(matcherState), ApkPathUtils.name(matcherState),
            ApkPathUtils.version(matcherState))).orElse(null);
      case APK_INDEX:
        return content().get(ApkPathUtils.path(matcherState)).orElse(null);
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = ApkPathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        log.debug("ARCHIVE" + ApkPathUtils.path(matcherState));
        return putArchive(ApkPathUtils.path(matcherState), ApkPathUtils.name(matcherState),
            ApkPathUtils.version(matcherState), content);
      case APK_INDEX:
        log.debug(("APK_INDEX" + ApkPathUtils.path(matcherState)));
        return putIndex(ApkPathUtils.path(matcherState), content);
      default:
        throw new IllegalStateException();
    }
  }

  private Content putArchive(
      final String path,
      final String filename,
      final String version,
      final Content content) throws IOException
  {
    return content().putArchive(path, filename, version, content);
  }

  private Content putIndex(final String path, final Content content) throws IOException {
    return content().putIndex(path, content);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    return context.getRequest().getPath().substring(1);
  }

  private ApkContentFacet content() {
    return getRepository().facet(ApkContentFacet.class);
  }
}
