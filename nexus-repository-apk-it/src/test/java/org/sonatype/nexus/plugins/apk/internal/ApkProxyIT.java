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

import org.sonatype.goodies.httpfixture.server.fluent.Behaviours;
import org.sonatype.goodies.httpfixture.server.fluent.Server;
import org.sonatype.nexus.pax.exam.NexusPaxExamSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.testsuite.testsupport.NexusITSupport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.testsuite.testsupport.FormatClientSupport.status;

public class ApkProxyIT
    extends ApkITSupport
{
  private static final String FORMAT_NAME = "apk";

  private static final String APK_INDEX = "APKINDEX.tar.gz";

  private static final String APK_INDEX_URL = "v2.6/main/x86/" + APK_INDEX;

  private ApkClient proxyClient;

  private Repository proxyRepo;

  private Server server;

  @Configuration
  public static Option[] configureNexus() {
    return NexusPaxExamSupport.options(
        NexusITSupport.configureNexusBase(),
        nexusFeature("org.sonatype.nexus.plugins", "nexus-repository-apk")
    );
  }

  @Before
  public void setup() throws Exception {
    server = Server.withPort(0)
        .serve("/" + APK_INDEX_URL)
        .withBehaviours(Behaviours.file(testData.resolveFile(APK_INDEX)))
        .start();

    proxyRepo = repos.createApkProxy("apk-test-proxy", server.getUrl().toExternalForm());
    proxyClient = apkClient(proxyRepo);
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    assertThat(status(proxyClient.get("stupid/path")), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void retrieveApkIndexFromProxyWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(APK_INDEX_URL)), is(HttpStatus.OK));

    final Asset asset = findAsset(proxyRepo, APK_INDEX_URL);
    assertThat(asset.name(), is(equalTo(APK_INDEX)));
    assertThat(asset.format(), is(equalTo(FORMAT_NAME)));
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }
}
