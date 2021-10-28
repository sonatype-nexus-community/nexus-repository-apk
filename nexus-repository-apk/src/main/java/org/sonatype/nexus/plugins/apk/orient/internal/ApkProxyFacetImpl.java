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
package org.sonatype.nexus.plugins.apk.orient.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.apk.internal.AssetKind;
import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.payloads.TempBlob;
import org.sonatype.nexus.transaction.UnitOfWork;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.apk.internal.AssetKind.APK_INDEX;
import static org.sonatype.nexus.plugins.apk.internal.AssetKind.ARCHIVE;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

@Named
public class ApkProxyFacetImpl
    extends ProxyFacetSupport
{
  private final ApkPathUtils pathUtils;
  private final ApkDataAccess dataAccess;

  @Inject
  public ApkProxyFacetImpl(final ApkPathUtils pathUtils,
                           final ApkDataAccess dataAccess)
  {
    this.pathUtils = checkNotNull(pathUtils);
    this.dataAccess = checkNotNull(dataAccess);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = pathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        return getAsset(pathUtils.archivePath(pathUtils.path(matcherState), pathUtils.name(matcherState), pathUtils.version(matcherState)));
      case APK_INDEX:
        return getAsset(pathUtils.path(matcherState));
      default:
        throw new IllegalStateException();
    }
  }

  @TransactionalTouchBlob
  protected Content getAsset(final String name) {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = dataAccess.findAsset(tx, tx.findBucket(getRepository()), name);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }
    return dataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = pathUtils.matcherState(context);
    switch (assetKind) {
      case ARCHIVE:
        log.debug("ARCHIVE" + pathUtils.path(matcherState));
        return putArchive(pathUtils.path(matcherState), pathUtils.name(matcherState), pathUtils.version(matcherState), content);
      case APK_INDEX:
        log.debug(("APK_INDEX" + pathUtils.path(matcherState)));
        return putIndex(pathUtils.path(matcherState), content);
      default:
        throw new IllegalStateException();
    }
  }

  private Content putArchive(final String path, final String filename, final String version, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), ApkDataAccess.HASH_ALGORITHMS)) {
      return doPutArchive(path, filename, version, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutArchive(final String path,
                                 final String name,
                                 final String version,
                                 final TempBlob archiveContent,
                                 final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());
    String assetPath = pathUtils.archivePath(path, name, version);

    Component component = dataAccess.findComponent(tx, getRepository(), name, version);
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(name)
          .version(version);
    }
    tx.saveComponent(component);

    Asset asset = dataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, component);
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, ARCHIVE.name());
    }
    return dataAccess.saveAsset(tx, asset, archiveContent, payload);
  }

  private Content putIndex(final String path, final Content content) throws IOException {
    StorageFacet storageFacet = facet(StorageFacet.class);
    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), ApkDataAccess.HASH_ALGORITHMS)) {
      return doPutIndex(path, tempBlob, content);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutIndex(final String path,
                               final TempBlob metadataContent,
                               final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    String assetPath = pathUtils.buildIndexPath(path);

    Asset asset = dataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, getRepository().getFormat());
      asset.name(assetPath);
      asset.formatAttributes().set(P_ASSET_KIND, APK_INDEX.name());
    }
    return dataAccess.saveAsset(tx, asset, metadataContent, payload);
  }

  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    return context.getRequest().getPath().substring(1);
  }

  @Override
  protected void indicateVerified(final Context context, final Content content, final CacheInfo cacheInfo)
      throws IOException
  {
    setCacheInfo(content, cacheInfo);
  }

  @TransactionalTouchMetadata
  public void setCacheInfo(final Content content, final CacheInfo cacheInfo) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = Content.findAsset(tx, tx.findBucket(getRepository()), content);
    if (asset == null) {
      log.debug(
          "Attempting to set cache info for non-existent APK asset {}", content.getAttributes().require(Asset.class)
      );
      return;
    }
    log.debug("Updating cacheInfo of {} to {}", asset, cacheInfo);
    CacheInfo.applyToAsset(asset, cacheInfo);
    tx.saveAsset(asset);
  }
}
