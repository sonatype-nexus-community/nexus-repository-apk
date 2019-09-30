<!--

    Sonatype Nexus (TM) Open Source Version
    Copyright (c) 2019-present Sonatype, Inc.
    All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.

    This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
    which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.

    Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
    of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
    Eclipse Foundation. All other trademarks are the property of their respective owners.

-->
## APK Repositories

### Introduction

[APK](https://wiki.alpinelinux.org/wiki/Alpine_Linux_package_management) is the built-in package manager for Alpine Linux, a security-oriented, lightweight Linux distribution based on musl libc and Busybox.

### Proxying APK repository

You can create a proxy repository in Nexus Repository Manager that will cache packages from a remote APK repository, like
http://dl-cdn.alpinelinux.org/alpine/v3.10/main/. Then, you can make `Alpine` use your Nexus Repository Proxy 
instead of the remote repository by editing `/etc/apk/repositories`. More information can be found [here](https://wiki.alpinelinux.org/wiki/Enable_Community_Repository). You may need to first install nano/vim/etc first for this to work (i.e. `apk add nano`). 
 
To proxy a APK repository, you simply create a new 'apk (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management) in
detail. Minimal configuration steps are:

- Define 'Name' - e.g. `apk-proxy`
- Define URL for 'Remote storage' e.g. [http://dl-cdn.alpinelinux.org/alpine/v3.10/main/](http://dl-cdn.alpinelinux.org/alpine/v3.10/main/)

If you haven't already, edit in Alpine `/etc/apk/repositories` to use your apk-proxy (i.e. add `http://localhost:8081/repository/apk-proxy/` and comment out or delete the other repo locations).

Now you can run `apk update` and install a package with the `apk add` command, like `apk add git`

The commands above tells APK to update the index of available packages and install the package from your Nexus APK proxy. The Nexus APK proxy will 
download any missing packages from the remote APK repository, and cache the packages on the Nexus APK proxy.
The next time any client requests the same package from your Nexus APK proxy, the already cached package will
be returned to the client.