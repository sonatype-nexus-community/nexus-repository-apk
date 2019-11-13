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
import org.sonatype.nexus.repository.storage.Component;
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

  private static final String DIRECTORY_MAIN = "v2.6/main/x86/";

  private static final String APK_ARCHIVE = "a2ps-dev-4.14-r5.apk";

  private static final String APK_ARCHIVE_WITH_DOT = "lua5.3-5.3.5-r2.apk";

  private static final String A2PS_VERSION = "4.14-r5";

  private static final String A2PS_COMPONENT_NAME = "a2ps-dev";

  private static final String LUA_COMPONENT_NAME = "lua5.3";

  private static final String LUA_VERSION = "5.3.5-r2";

  private static final String PATH_APK_INDEX = DIRECTORY_MAIN + APK_INDEX;

  private static final String PATH_APK_ARCHIVE = DIRECTORY_MAIN + APK_ARCHIVE;

  private static final String PATH_APK_ARCHIVE_WITH_DOT = DIRECTORY_MAIN + APK_ARCHIVE_WITH_DOT;

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
        .serve("/" + PATH_APK_INDEX)
        .withBehaviours(Behaviours.file(testData.resolveFile(APK_INDEX)))
        .serve("/" + PATH_APK_ARCHIVE)
        .withBehaviours(Behaviours.file(testData.resolveFile(APK_ARCHIVE)))
        .serve("/" + PATH_APK_ARCHIVE_WITH_DOT)
        .withBehaviours(Behaviours.file(testData.resolveFile(APK_ARCHIVE_WITH_DOT)))
        .start();

    proxyRepo = repos.createApkProxy("apk-test-proxy", server.getUrl().toExternalForm());
    proxyClient = apkClient(proxyRepo);
  }

  @Test
  public void unresponsiveRemoteProduces404() throws Exception {
    proxyClient.get("stupid/path");
    assertThat(status(proxyClient.get("stupid/path")), is(HttpStatus.NOT_FOUND));
  }

  @Test
  public void retrieveApkIndexFromProxyWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(PATH_APK_INDEX)), is(HttpStatus.OK));

    final Asset asset = findAsset(proxyRepo, PATH_APK_INDEX);
    assertThat(asset.name(), is(equalTo(PATH_APK_INDEX)));
    assertThat(asset.format(), is(equalTo(FORMAT_NAME)));
  }

  @Test
  public void retrieveApkArchiveFromProxyWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(PATH_APK_ARCHIVE)), is(HttpStatus.OK));

    final Asset asset = findAsset(proxyRepo, PATH_APK_ARCHIVE);
    assertThat(asset.name(), is(equalTo(PATH_APK_ARCHIVE)));
    assertThat(asset.format(), is(equalTo(FORMAT_NAME)));

    final Component component = findComponent(proxyRepo, A2PS_COMPONENT_NAME);
    assertThat(component.version(), is(equalTo(A2PS_VERSION)));
    assertThat(component.name(), is(equalTo(A2PS_COMPONENT_NAME)));
    assertThat(component.format(), is(equalTo(FORMAT_NAME)));
  }

  @Test
  public void retrieveAssetWithDotInNameFromProxyWhenRemoteOnline() throws Exception {
    assertThat(status(proxyClient.get(PATH_APK_ARCHIVE_WITH_DOT)), is(HttpStatus.OK));

    final Asset asset = findAsset(proxyRepo, PATH_APK_ARCHIVE_WITH_DOT);
    assertThat(asset.name(), is(equalTo(PATH_APK_ARCHIVE_WITH_DOT)));
    assertThat(asset.format(), is(equalTo(FORMAT_NAME)));

    final Component component = findComponent(proxyRepo, LUA_COMPONENT_NAME);
    assertThat(component.version(), is(equalTo(LUA_VERSION)));
    assertThat(component.name(), is(equalTo(LUA_COMPONENT_NAME)));
    assertThat(component.format(), is(equalTo(FORMAT_NAME)));
  }

  @After
  public void tearDown() throws Exception {
    server.stop();
  }
}
