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
package org.sonatype.nexus.plugins.apk.internal;

import java.net.URL;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.plugins.apk.internal.fixtures.RepositoryRuleApk;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.testsuite.helpers.ComponentAssetTestHelper;
import org.sonatype.nexus.testsuite.testsupport.RepositoryITSupport;

import org.junit.Rule;

import static com.google.common.base.Preconditions.checkNotNull;

public class ApkITSupport
    extends RepositoryITSupport
{
  @Rule
  public RepositoryRuleApk repos = new RepositoryRuleApk(() -> repositoryManager);

  @Inject
  protected ComponentAssetTestHelper componentAssetTestHelper;

  @Override
  protected RepositoryRuleApk createRepositoryRule() {
    return new RepositoryRuleApk(() -> repositoryManager);
  }

  public ApkITSupport() {
    testData.addDirectory(NexusPaxExamSupport.resolveBaseFile("target/it-resources/apk"));
  }

  @Nonnull
  protected ApkClient apkClient(final Repository repository) throws Exception {
    checkNotNull(repository);
    return apkClient(repositoryBaseUrl(repository));
  }

  protected ApkClient apkClient(final URL repositoryUrl) throws Exception {
    return new ApkClient(
        clientBuilder(repositoryUrl).build(),
        clientContext(),
        repositoryUrl.toURI()
    );
  }
}
