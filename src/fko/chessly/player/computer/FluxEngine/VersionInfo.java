/*
 * Copyright (C) 2007-2014 Phokham Nonava
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxchess.flux;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class VersionInfo {

  private static final String versionInfoProperty = "/com/fluxchess/flux/version-info.properties";
  private static final VersionInfo CURRENT;

  private final String version;
  private final String buildNumber;
  private final String revisionNumber;

  static {
    try (InputStream inputStream = VersionInfo.class.getResourceAsStream(versionInfoProperty)) {
      if (inputStream != null) {
        Properties properties = new Properties();
        properties.load(inputStream);

        CURRENT = new VersionInfo(
            properties.getProperty("version", "n/a"),
            properties.getProperty("buildNumber", "n/a"),
            properties.getProperty("revisionNumber", "n/a")
        );
      } else {
        throw new FileNotFoundException(String.format("Cannot find the properties file %s", versionInfoProperty));
      }
    } catch (IOException e) {
      throw new RuntimeException(String.format("Cannot load the properties file %s", versionInfoProperty), e);
    }
  }

  static VersionInfo current() {
    return CURRENT;
  }

  private VersionInfo(String version, String buildNumber, String revisionNumber) {
    if (version == null) throw new IllegalArgumentException();
    if (buildNumber == null) throw new IllegalArgumentException();
    if (revisionNumber == null) throw new IllegalArgumentException();

    this.version = version;
    this.buildNumber = buildNumber;
    this.revisionNumber = revisionNumber;
  }

  @Override
  public String toString() {
    return String.format("Flux %s", version);
  }

  String getVersion() {
    return version;
  }

  String getBuildNumber() {
    return buildNumber;
  }

  String getRevisionNumber() {
    return revisionNumber;
  }

}
