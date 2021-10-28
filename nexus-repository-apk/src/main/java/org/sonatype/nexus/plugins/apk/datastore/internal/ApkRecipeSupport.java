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

import javax.inject.Inject;
import javax.inject.Provider;

import org.sonatype.nexus.plugins.apk.internal.ApkSecurityFacet;
import org.sonatype.nexus.plugins.apk.internal.AssetKind;
import org.sonatype.nexus.repository.BrowseUnsupportedHandler;
import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.RecipeSupport;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.attributes.AttributesFacet;
import org.sonatype.nexus.repository.cache.NegativeCacheHandler;
import org.sonatype.nexus.repository.content.maintenance.LastAssetMaintenanceFacet;
import org.sonatype.nexus.repository.content.search.SearchFacet;
import org.sonatype.nexus.repository.http.PartialFetchHandler;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.purge.PurgeUnusedFacet;
import org.sonatype.nexus.repository.view.ConfigurableViewFacet;
import org.sonatype.nexus.repository.view.Matcher;
import org.sonatype.nexus.repository.view.handlers.ConditionalRequestHandler;
import org.sonatype.nexus.repository.view.handlers.ContentHeadersHandler;
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler;
import org.sonatype.nexus.repository.view.handlers.HandlerContributor;
import org.sonatype.nexus.repository.view.handlers.SecurityHandler;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.ActionMatcher;
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static org.sonatype.nexus.repository.http.HttpMethods.GET;
import static org.sonatype.nexus.repository.http.HttpMethods.HEAD;

public abstract class ApkRecipeSupport
  extends RecipeSupport
{
  protected final Provider<ApkSecurityFacet> securityFacet;

  protected final Provider<ConfigurableViewFacet> viewFacet;

  protected final Provider<SearchFacet> searchFacet;

  protected final Provider<AttributesFacet> attributesFacet;

  protected final ExceptionHandler exceptionHandler;

  protected final TimingHandler timingHandler;

  protected final SecurityHandler securityHandler;

  protected final PartialFetchHandler partialFetchHandler;

  protected final ConditionalRequestHandler conditionalRequestHandler;

  protected final ContentHeadersHandler contentHeadersHandler;

  protected final BrowseUnsupportedHandler browseUnsupportedHandler;

  protected final HandlerContributor handlerContributor;

  protected final Provider<LastAssetMaintenanceFacet> componentMaintenanceFacet;

  protected final Provider<HttpClientFacet> httpClientFacet;

  protected final Provider<PurgeUnusedFacet> purgeUnusedFacet;

  protected final NegativeCacheHandler negativeCacheHandler;

  @Inject
  public ApkRecipeSupport(
      final Type type,
      final Format format,
      final Provider<ApkSecurityFacet> securityFacet,
      final Provider<ConfigurableViewFacet> viewFacet,
      final Provider<SearchFacet> searchFacet,
      final Provider<AttributesFacet> attributesFacet,
      final ExceptionHandler exceptionHandler,
      final TimingHandler timingHandler,
      final SecurityHandler securityHandler,
      final PartialFetchHandler partialFetchHandler,
      final ConditionalRequestHandler conditionalRequestHandler,
      final ContentHeadersHandler contentHeadersHandler,
      final BrowseUnsupportedHandler browseUnsupportedHandler,
      final HandlerContributor handlerContributor,
      final Provider<LastAssetMaintenanceFacet> componentMaintenanceFacet,
      final Provider<HttpClientFacet> httpClientFacet,
      final Provider<PurgeUnusedFacet> purgeUnusedFacet,
      final NegativeCacheHandler negativeCacheHandler)
  {
    super(type, format);
    this.securityFacet = securityFacet;
    this.viewFacet = viewFacet;
    this.searchFacet = searchFacet;
    this.attributesFacet = attributesFacet;
    this.exceptionHandler = exceptionHandler;
    this.timingHandler = timingHandler;
    this.securityHandler = securityHandler;
    this.partialFetchHandler = partialFetchHandler;
    this.conditionalRequestHandler = conditionalRequestHandler;
    this.contentHeadersHandler = contentHeadersHandler;
    this.browseUnsupportedHandler = browseUnsupportedHandler;
    this.handlerContributor = handlerContributor;
    this.componentMaintenanceFacet = componentMaintenanceFacet;
    this.httpClientFacet = httpClientFacet;
    this.purgeUnusedFacet = purgeUnusedFacet;
    this.negativeCacheHandler = negativeCacheHandler;
  }


  static Matcher apkIndexMatcher() {
    return buildTokenMatcherForPatternAndAssetKind("/{path:.+}/APKINDEX.tar.gz", AssetKind.APK_INDEX, GET, HEAD);
  }

  static Matcher archiveMatcher() {
    return buildTokenMatcherForPatternAndAssetKind("/{path:.+}/{filename:.+}.apk", AssetKind.ARCHIVE, GET, HEAD);
  }

  static Matcher buildTokenMatcherForPatternAndAssetKind(
      final String pattern,
      final AssetKind assetKind,
      final String... actions)
  {
    return LogicMatchers.and(
        new ActionMatcher(actions),
        new TokenMatcher(pattern),
        context -> {
            context.getAttributes().set(AssetKind.class, assetKind);
            return true;
          }
        );
  }
}
