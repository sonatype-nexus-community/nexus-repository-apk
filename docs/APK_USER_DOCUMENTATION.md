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
## apk Repositories

### Introduction

[apk](https://wiki.alpinelinux.org/wiki/Alpine_Linux_package_management) is the built-in package manager for Alpine Linux, a security-oriented, lightweight Linux distribution based on musl libc and Busybox.

### Proxying apk repository

You can create a proxy repository in Nexus Repository Manager (NXRM) that will cache packages from a remote apk repository such as
http://dl-cdn.alpinelinux.org/alpine/v3.10/main/. To make `Alpine` use your NXRM Proxy, you will need to edit the file located at `/etc/apk/repositories`. More information can be found [here](https://wiki.alpinelinux.org/wiki/Enable_Community_Repository). 
 
To proxy a apk repository, you simply create a new 'apk (proxy)' as documented in 
[Repository Management](https://help.sonatype.com/repomanager3/configuration/repository-management). 

Minimal configuration steps are:
- Define 'Name' - e.g. `apk-proxy`
- Define URL for 'Remote storage' - e.g. [http://dl-cdn.alpinelinux.org/alpine/v3.10/main/](http://dl-cdn.alpinelinux.org/alpine/v3.10/main/)
- Select a `Blob store` for `Storage`

If you haven't already, edit the `Apline` file located at `/etc/apk/repositories` to use your apk-proxy (i.e. add `http://localhost:8081/repository/apk-proxy/` and comment out or delete the other remote repo locations).

Now you can run `apk update` and install a package with the `apk add` command, like `apk add git`.

The command above tells apk to update the index of available packages and install the package from your NXRM apk proxy. The NXRM apk proxy will 
download any missing packages from the remote apk repository, and cache the packages on the NXRM apk proxy.
The next time any client requests the same package from your NXRM apk proxy, the already cached package will
be returned to the client.
