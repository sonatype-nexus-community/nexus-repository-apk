/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
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
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.plugins.apk.datastore.ApkContentFacet;
import org.sonatype.nexus.plugins.apk.internal.AssetKind;
import org.sonatype.nexus.repository.content.facet.ContentFacetSupport;
import org.sonatype.nexus.repository.content.fluent.FluentAsset;
import org.sonatype.nexus.repository.content.fluent.FluentComponent;
import org.sonatype.nexus.repository.content.store.FormatStoreManager;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.TempBlob;

import com.google.common.collect.ImmutableList;

import static org.sonatype.nexus.common.hash.HashAlgorithm.SHA1;

@Named
public class ApkContentFacetImpl
    extends ContentFacetSupport
    implements ApkContentFacet
{
  public static final List<HashAlgorithm> HASH_ALGORITHMS = ImmutableList.of(SHA1);

  @Inject
  public ApkContentFacetImpl(final FormatStoreManager formatStoreManager) {
    super(formatStoreManager);
  }

  @Override
  public Optional<Content> get(final String path) throws IOException {
    return assets()
        .path(path)
        .find()
        .map(FluentAsset::download);
  }

  @Override
  public Content putArchive(final String path, final String filename, final String version, final Payload payload) {
    String assetPath = ApkPathUtils.archivePath(path, filename, version);
    try (TempBlob tempBlob = blobs().ingest(payload, HASH_ALGORITHMS)) {
      FluentComponent component = components()
          .name(filename)
          .version(version)
          .getOrCreate();

      return assets().path(assetPath)
          .component(component)
          .kind(AssetKind.ARCHIVE.name())
          .save()
          .markAsCached(payload)
          .download();
    }
  }

  @Override
  public Content putIndex(final String path, final Payload payload) {
    String assetPath = ApkPathUtils.buildIndexPath(path);
    try (TempBlob tempBlob = blobs().ingest(payload, HASH_ALGORITHMS)) {
      return assets().path(assetPath)
          .kind(AssetKind.APK_INDEX.name())
          .save()
          .markAsCached(payload)
          .download();
    }
  }
}
