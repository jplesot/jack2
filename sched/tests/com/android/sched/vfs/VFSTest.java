/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sched.vfs;

import com.android.sched.util.config.AsapConfigBuilder;
import com.android.sched.util.config.ConfigurationException;
import com.android.sched.util.config.MessageDigestFactory;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.CannotChangePermissionException;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileAlreadyExistsException;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;
import com.android.sched.util.file.FileUtils;
import com.android.sched.util.file.InputZipFile;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.NotFileOrDirectoryException;
import com.android.sched.util.file.OutputZipFile;
import com.android.sched.util.file.OutputZipFile.Compression;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.file.ZipException;
import com.android.sched.util.location.DirectoryLocation;
import com.android.sched.util.location.FileLocation;
import com.android.sched.util.location.ZipLocation;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class VFSTest {

  @Before
  public void setUp() throws ConfigurationException {
    ThreadConfig.setConfig(new AsapConfigBuilder(/* debug = */ false).build());
  }

  @Test
  public void testDirectFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());
      VFS vfs1 = new DirectFS(new Directory(path, null, Existence.NOT_EXIST,
          Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE);

      ioVFS1 = new GenericInputOutputVFS(vfs1);

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      ioVFS1.close();

      VFS vfs2 = new DirectFS(new Directory(path, null, Existence.MUST_EXIST,
          Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE);
      ioVFS2 = new GenericInputOutputVFS(vfs2);
      testInputVFS(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testCachedDirectFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());
      VFS vfs1 = new CachedDirectFS(new Directory(path, null,
          Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE);

      ioVFS1 = new GenericInputOutputVFS(vfs1);

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      checkUnicity(vfs1);
      ioVFS1.close();

      VFS vfs2 = new DirectFS(new Directory(path, null, Existence.MUST_EXIST,
          Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE);

      ioVFS2 = new GenericInputOutputVFS(vfs2);
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testDeflateFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      ioVFS1 =
          new GenericInputOutputVFS(new DeflateFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      ioVFS1.close();

      ioVFS2 =
          new GenericInputOutputVFS(new DeflateFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testCaseInsensitiveFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException, BadVFSFormatException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      ioVFS1 =
          new GenericInputOutputVFS(new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      ioVFS1.close();

      ioVFS2 =
          new GenericInputOutputVFS(new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE)));
      testInputVFS(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testMessageDigestFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException, BadVFSFormatException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      Provider.Service sha1 = getSha1Service();

      ioVFS1 = new GenericInputOutputVFS(new MessageDigestFS(new DirectFS(new Directory(path,
          null, Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE), new MessageDigestFactory(getSha1Service())));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      InputVFile fileAAB1 =
          ioVFS1.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest = ((GenericInputVFile) fileAAB1).getDigest();
      Assert.assertNotNull(fileAAB1digest);
      String vfsDigest = ioVFS1.getDigest();
      Assert.assertNotNull(vfsDigest);
      ioVFS1.close();

      ioVFS2 = new GenericInputOutputVFS(new MessageDigestFS(new DirectFS(new Directory(path,
          null, Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE), new MessageDigestFactory(sha1)));
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

      InputVFile fileAAB1b =
          ioVFS2.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest2 = ((GenericInputVFile) fileAAB1b).getDigest();
      Assert.assertEquals(fileAAB1digest, fileAAB1digest2);
      String vfsDigest2 = ioVFS2.getDigest();
      Assert.assertEquals(vfsDigest, vfsDigest2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testMessageDigestFSWithCaseInsensitiveFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException, BadVFSFormatException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      Provider.Service sha1 = null;
      for (Provider provider : Security.getProviders()) {
        for (Provider.Service service : provider.getServices()) {
          if (service.getType().equals("MessageDigest") && service.getAlgorithm().equals("SHA")) {
            sha1 = service;
          }
        }
      }
      Assert.assertNotNull(sha1);

      ioVFS1 = new GenericInputOutputVFS(new MessageDigestFS(new CaseInsensitiveFS(
          new DirectFS(new Directory(path, null, Existence.NOT_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE)),
              new MessageDigestFactory(sha1)));
      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      InputVFile fileAAB1 =
          ioVFS1.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest = ((GenericInputVFile) fileAAB1).getDigest();
      Assert.assertNotNull(fileAAB1digest);
      String vfsDigest = ioVFS1.getDigest();
      Assert.assertNotNull(vfsDigest);
      ioVFS1.close();

      ioVFS2 = new GenericInputOutputVFS(new MessageDigestFS(new CaseInsensitiveFS(
          new DirectFS(new Directory(path, null, Existence.MUST_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE)),
              new MessageDigestFactory(sha1)));
      testInputVFS(ioVFS2);

      InputVFile fileAAB1b =
          ioVFS2.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest2 = ((GenericInputVFile) fileAAB1b).getDigest();
      Assert.assertEquals(fileAAB1digest, fileAAB1digest2);
      String vfsDigest2 = ioVFS2.getDigest();
      Assert.assertEquals(vfsDigest, vfsDigest2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testDeflatePrefixedCaseInsensitiveDirectFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException, BadVFSFormatException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      CaseInsensitiveFS ciFS = new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
          Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE));

      ioVFS1 =
          new GenericInputOutputVFS(new DeflateFS(new PrefixedFS(ciFS, new VPath("stuff", '/'))));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      ciFS.close();
      ioVFS1.close();

      CaseInsensitiveFS ciFS2 = new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
          Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE));

      ioVFS2 =
          new GenericInputOutputVFS(new DeflateFS(new PrefixedFS(ciFS2, new VPath("stuff", '/'))));
      testInputVFS(ioVFS2);
      ciFS2.close();

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testMessageDigestFSWithCachedDirectFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException, BadVFSFormatException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      Provider.Service sha1 = null;
      for (Provider provider : Security.getProviders()) {
        for (Provider.Service service : provider.getServices()) {
          if (service.getType().equals("MessageDigest") && service.getAlgorithm().equals("SHA")) {
            sha1 = service;
          }
        }
      }
      Assert.assertNotNull(sha1);

      VFS vfs1 = new MessageDigestFS(new CachedDirectFS(
          new Directory(path, null, Existence.NOT_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE),
              new MessageDigestFactory(sha1));

      ioVFS1 = new GenericInputOutputVFS(vfs1);
      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      InputVFile fileAAB1 =
          ioVFS1.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest = ((GenericInputVFile) fileAAB1).getDigest();
      Assert.assertNotNull(fileAAB1digest);
      String vfsDigest = ioVFS1.getDigest();
      Assert.assertNotNull(vfsDigest);
      ioVFS1.close();

      ioVFS2 = new GenericInputOutputVFS(new MessageDigestFS(
          new DirectFS(new Directory(path, null, Existence.MUST_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE),
              new MessageDigestFactory(sha1)));
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

      InputVFile fileAAB1b =
          ioVFS2.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest2 = ((GenericInputVFile) fileAAB1b).getDigest();
      Assert.assertEquals(fileAAB1digest, fileAAB1digest2);
      String vfsDigest2 = ioVFS2.getDigest();
      Assert.assertEquals(vfsDigest, vfsDigest2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testPrefixedFSWithDirectFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, NotFileOrDirectoryException,
      CannotDeleteFileException, CannotCloseException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      ioVFS1 =
          new GenericInputOutputVFS(new PrefixedFS(new DirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE), new VPath("stuff", '/')));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      checkFileLocations(ioVFS1);
      testInputVFS(ioVFS1);
      ioVFS1.close();

      ioVFS2 =
          new GenericInputOutputVFS(new PrefixedFS(new DirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ
              | Permission.WRITE), new VPath("stuff", '/')));
      testInputVFS(ioVFS2);
      checkFileLocations(ioVFS2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testPrefixedFSWithZip()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, ZipException,
      NotFileOrDirectoryException, CannotCloseException {
    String prefix = "stuff";
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();

      WriteZipFS writeZipFS = new WriteZipFS(new OutputZipFile(path, null, Existence.MAY_EXIST,
          ChangePermission.NOCHANGE, Compression.COMPRESSED));

      ioVFS1 = new GenericInputOutputVFS(new PrefixedFS(writeZipFS, new VPath(prefix, '/')));

      testOutputVFS(ioVFS1);
      ioVFS1.close();
      writeZipFS.close();

      ReadZipFS readZipFS = new ReadZipFS(new InputZipFile(path));

      ioVFS2 = new GenericInputOutputVFS(new PrefixedFS(readZipFS, new VPath(prefix, '/')));
      testInputVFS(ioVFS2);
      checkZipLocations(ioVFS2, prefix);

      readZipFS.close();

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        Assert.assertTrue(file.delete());
      }
    }
  }

  @Test
  public void testPrefixedFSWithCaseInsensitiveFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException, BadVFSFormatException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      CaseInsensitiveFS ciFS = new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
          Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE));

      ioVFS1 = new GenericInputOutputVFS(new PrefixedFS(ciFS, new VPath("stuff", '/')));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      ciFS.close();
      ioVFS1.close();

      CaseInsensitiveFS ciFS2 = new CaseInsensitiveFS(new DirectFS(new Directory(path, null,
          Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
          Permission.READ | Permission.WRITE));

      ioVFS2 = new GenericInputOutputVFS(new PrefixedFS(ciFS2, new VPath("stuff", '/')));
      testInputVFS(ioVFS2);
      ciFS2.close();

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testMessageDigestFSWithPrefixedFSAndCaseInsensitiveFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, CannotCloseException, BadVFSFormatException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputOutputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", "dir");
      String path = file.getAbsolutePath();
      Assert.assertTrue(file.delete());

      Provider.Service sha1 = null;
      for (Provider provider : Security.getProviders()) {
        for (Provider.Service service : provider.getServices()) {
          if (service.getType().equals("MessageDigest") && service.getAlgorithm().equals("SHA")) {
            sha1 = service;
          }
        }
      }
      Assert.assertNotNull(sha1);

      CaseInsensitiveFS ciFS =
          new CaseInsensitiveFS(new MessageDigestFS(new CachedDirectFS(new Directory(path, null,
              Existence.NOT_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
              Permission.READ | Permission.WRITE), new MessageDigestFactory(sha1)));

      ioVFS1 = new GenericInputOutputVFS(new PrefixedFS(ciFS, new VPath("stuff", '/')));

      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      ciFS.close();
      ioVFS1.close();

      CaseInsensitiveFS ciFS2 =
          new CaseInsensitiveFS(new MessageDigestFS(new CachedDirectFS(new Directory(path, null,
              Existence.MUST_EXIST, Permission.WRITE, ChangePermission.NOCHANGE),
              Permission.READ | Permission.WRITE), new MessageDigestFactory(sha1)));

      ioVFS2 = new GenericInputOutputVFS(new PrefixedFS(ciFS2, new VPath("stuff", '/')));
      testInputVFS(ioVFS2);
      ciFS2.close();

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        FileUtils.deleteDir(file);
      }
    }
  }

  @Test
  public void testWriteZipVFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, ZipException,
      NotFileOrDirectoryException, CannotCloseException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputVFS iVFS2 = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();
      ioVFS1 = new GenericInputOutputVFS(new WriteZipFS(new OutputZipFile(path, null,
          Existence.MAY_EXIST, ChangePermission.NOCHANGE, Compression.COMPRESSED)));
      testOutputVFS(ioVFS1);
      ioVFS1.close();
      iVFS2 = new GenericInputVFS(new ReadZipFS(new InputZipFile(path)));
      testInputVFS(iVFS2);
      checkZipLocations(iVFS2);
    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (iVFS2 != null) {
        iVFS2.close();
      }
      if (file != null) {
        Assert.assertTrue(file.delete());
      }
    }
  }

  @SuppressWarnings("resource")
  @Test
  public void testUnionVFS() throws IOException, WrongPermissionException,
      CannotChangePermissionException, NoSuchFileException, FileAlreadyExistsException,
      CannotCreateFileException, ZipException, NotFileOrDirectoryException,
      CannotDeleteFileException, CannotCloseException {
    File zipFile = null;
    File dir = null;
    try {
      zipFile = File.createTempFile("vfs", ".zip");
      dir = File.createTempFile("vfs", "dir");
      String dirPath = dir.getPath();
      Assert.assertTrue(dir.delete());

      // fill up zip
      VFS writeZipVFS = new ReadWriteZipFS(
          new OutputZipFile(zipFile.getPath(), null, Existence.MAY_EXIST, ChangePermission.NOCHANGE,
              Compression.COMPRESSED), /* numGroups = */ 1, /* groupSize = */ 2,
          new MessageDigestFactory(getSha1Service()), /* debug = */ false);
      testOutputVFS(new GenericInputOutputVFS(writeZipVFS));
      writeZipVFS.close();

      // create Dir
      VFS dirVFS = new CachedDirectFS(new Directory(dirPath, null, Existence.NOT_EXIST,
          Permission.WRITE, ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE);

      // create UnionVFS with dir and read-only zip
      List<VFS> vfsList = new ArrayList<VFS>(2);
      vfsList.add(dirVFS);
      vfsList.add(new ReadZipFS(new InputZipFile(zipFile.getPath())));
      VFS unionVFS = new UnionVFS(vfsList);
      testInputVFS(new GenericInputVFS(unionVFS));

      // write new stuff to UnionVFS
      InputOutputVFS outputUnionVFS = new GenericInputOutputVFS(unionVFS);
      OutputVFile fileBA2 = outputUnionVFS.getRootOutputVDir().createOutputVFile(
          new VPath("dirB/dirBA/fileBA2", '/'));
      writeToFile(fileBA2, "dirB/dirBA/fileBA2");
      InputOutputVDir dirA =
          outputUnionVFS.getRootInputOutputVDir().getInputVDir(new VPath("dirA", '/'));
      InputOutputVDir dirAA = dirA.getInputVDir(new VPath("dirAA", '/'));
      InputOutputVDir dirAAB = (InputOutputVDir) dirAA.createOutputVDir(new VPath("dirAAB", '/'));
      OutputVFile fileAAB2 = dirAAB.createOutputVFile(new VPath("fileAAB2", '/'));
      writeToFile(fileAAB2, "dirA/dirAA/dirAAB/fileAAB2");
      InputOutputVDir dirC =
          outputUnionVFS.getRootInputOutputVDir().getInputVDir(new VPath("dirC", '/'));
      // write on top of an already existing file
      OutputVFile fileC1 = dirC.getInputVFile(new VPath("fileC1", '/'));
      writeToFile(fileC1, "dirC/fileC1v2");

      // try deleting some old stuff from zip (which is not yet supported)
      try {
        outputUnionVFS.getRootInputOutputVDir().getInputVFile(new VPath("dirB/dirBB/fileBB1", '/'))
            .delete();
        Assert.fail();
      } catch (UnionVFSReadOnlyException e) {
        // expected because not yet supported
      }

      // read new stuff
      InputVFS inputUnionVFS = new GenericInputVFS(unionVFS);
      InputVFile readFileC1 = inputUnionVFS.getRootInputVDir().getInputVFile(
          new VPath("dirC/fileC1", '/'));
      Assert.assertEquals("dirC/fileC1v2", readFromFile(readFileC1));
      Assert.assertTrue(
          readFileC1.getPathFromRoot().equals(new VPath("dirC/fileC1", '/')));
      InputVDir readDirA = inputUnionVFS.getRootInputVDir().getInputVDir(new VPath("dirA", '/'));
      InputVDir readDirAA = readDirA.getInputVDir(new VPath("dirAA", '/'));
      InputVDir readDirAAB = readDirAA.getInputVDir(new VPath("dirAAB", '/'));
      InputVFile readFileAAB2 = readDirAAB.getInputVFile(new VPath("fileAAB2", '/'));
      Assert.assertEquals("dirA/dirAA/dirAAB/fileAAB2", readFromFile(readFileAAB2));
      Assert.assertTrue(
          readFileAAB2.getPathFromRoot().equals(new VPath("dirA/dirAA/dirAAB/fileAAB2", '/')));

      // delete some new stuff
      readFileAAB2.delete();
      try {
        readDirAAB.getInputVFile(new VPath("fileAAB2", '/'));
        Assert.fail();
      } catch (NoSuchFileException e) {
        // expected
      }

      // try to delete a file that is in the dir and the zip, then check that the one from the zip
      // remains
      inputUnionVFS.getRootInputVDir().getInputVFile(new VPath("dirC/fileC1", '/')).delete();
      Assert.assertEquals("dirC/fileC1", readFromFile(
          inputUnionVFS.getRootInputVDir().getInputVFile(new VPath("dirC/fileC1", '/'))));

      // list contents of "dirB/dirBA", which has files in the dir and in the zip
      InputVDir dirBA = inputUnionVFS.getRootInputVDir().getInputVDir(new VPath("dirB/dirBA", '/'));
      Collection<? extends InputVElement> dirBAList = dirBA.list();
      Assert.assertEquals(2, dirBAList.size());
      for (InputVElement subElement : dirBAList) {
        if (subElement.getName().equals("fileBA2")) {
          InputVFile readFileBA22 = (InputVFile) subElement;
          Assert.assertEquals("dirB/dirBA/fileBA2", readFromFile(readFileBA22));
          Assert.assertTrue(
              readFileBA22.getPathFromRoot().equals(new VPath("dirB/dirBA/fileBA2", '/')));
        } else if (subElement.getName().equals("fileBA1")) {
          InputVFile readFileBA1 = (InputVFile) subElement;
          Assert.assertEquals("dirB/dirBA/fileBA1", readFromFile(readFileBA1));
          Assert.assertTrue(
              readFileBA1.getPathFromRoot().equals(new VPath("dirB/dirBA/fileBA1", '/')));
        } else {
          throw new AssertionError();
        }
      }

      // retest input
      testInputVFS(new GenericInputVFS(unionVFS));

      outputUnionVFS.close();

    } finally {
      if (zipFile != null) {
        Assert.assertTrue(zipFile.delete());
      }
      if (dir != null) {
        FileUtils.deleteDir(dir);
      }
    }
  }

  @SuppressWarnings("resource")
  @Test
  public void testIncrementalStack() throws IOException, CannotCreateFileException,
      WrongPermissionException, CannotChangePermissionException, NoSuchFileException,
      FileAlreadyExistsException, ZipException, NotFileOrDirectoryException,
      CannotDeleteFileException, CannotCloseException, BadVFSFormatException {
    final VPath prefix = new VPath("pre", '/');
    File inputZipFile = null;
    File outputZipFile = null;
    File dir = null;
    try {
      inputZipFile = File.createTempFile("input", ".zip");
      dir = File.createTempFile("vfs", "dir");
      outputZipFile = File.createTempFile("output", ".zip");
      String dirPath = dir.getPath();
      Assert.assertTrue(dir.delete());

      // fill up zip that will be used as input
      {
        VFS writeZipVFS = new ReadWriteZipFS(
            new OutputZipFile(inputZipFile.getPath(), null, Existence.MAY_EXIST,
                ChangePermission.NOCHANGE, Compression.COMPRESSED),
            /* numGroups = */ 1, /* groupSize = */ 2, new MessageDigestFactory(getSha1Service()),
            /* debug = */ false);
        InputOutputVFS ioVFS1 = new GenericInputOutputVFS(new PrefixedFS(writeZipVFS, prefix));
        testOutputVFS(ioVFS1);
        ioVFS1.close();
        writeZipVFS.close();
      }

      // create Dir
      VFS ciVFS = new CaseInsensitiveFS(
          new CachedDirectFS(new Directory(dirPath, null, Existence.NOT_EXIST, Permission.WRITE,
              ChangePermission.NOCHANGE), Permission.READ | Permission.WRITE));
      VFS prefixedFS1 = new PrefixedFS(ciVFS, prefix);
      prefixedFS1.close();

      // create R/W output zip that uses as temp dir an UnionVFS between dir and read-only zip
      ReadWriteZipFS rwzfs = new ReadWriteZipFS(
          new OutputZipFile(outputZipFile.getPath(), null, Existence.MAY_EXIST,
              ChangePermission.NOCHANGE, Compression.COMPRESSED),
          /* numGroups = */ 1, /* groupSize = */ 2, new MessageDigestFactory(getSha1Service()),
          /* debug = */ false);
      {
        List<VFS> vfsList = new ArrayList<VFS>(2);
        vfsList.add(ciVFS);
        vfsList.add(new ReadZipFS(new InputZipFile(inputZipFile.getPath())));
        VFS unionVFS = new UnionVFS(vfsList);
        rwzfs.setWorkVFS(unionVFS);
      }
      VFS prefixedFS = new PrefixedFS(rwzfs, prefix);

      testInputVFS(new GenericInputVFS(prefixedFS));

      // write new stuff to R/W output zip
      InputOutputVFS ioVFS = new GenericInputOutputVFS(prefixedFS);
      OutputVFile fileBA2 = ioVFS.getRootOutputVDir().createOutputVFile(
          new VPath("dirB/dirBA/fileBA2", '/'));
      writeToFile(fileBA2, "dirB/dirBA/fileBA2");
      InputOutputVDir dirA =
          ioVFS.getRootInputOutputVDir().getInputVDir(new VPath("dirA", '/'));
      InputOutputVDir dirAA = dirA.getInputVDir(new VPath("dirAA", '/'));
      InputOutputVDir dirAAB = (InputOutputVDir) dirAA.createOutputVDir(new VPath("dirAAB", '/'));
      OutputVFile fileAAB2 = dirAAB.createOutputVFile(new VPath("fileAAB2", '/'));
      writeToFile(fileAAB2, "dirA/dirAA/dirAAB/fileAAB2");
      InputOutputVDir dirC =
          ioVFS.getRootInputOutputVDir().getInputVDir(new VPath("dirC", '/'));
      // write on top of an already existing file
      OutputVFile fileC1 = dirC.getInputVFile(new VPath("fileC1", '/'));
      writeToFile(fileC1, "dirC/fileC1v2");

      // try deleting some old stuff from zip (which is not yet supported)
      try {
        ioVFS.getRootInputOutputVDir().getInputVFile(new VPath("dirB/dirBB/fileBB1", '/'))
            .delete();
        Assert.fail();
      } catch (UnionVFSReadOnlyException e) {
        // expected because not yet supported
      }

      // read new stuff
      InputVFS inputVFS = new GenericInputVFS(prefixedFS);
      InputVFile readFileC1 = inputVFS.getRootInputVDir().getInputVFile(
          new VPath("dirC/fileC1", '/'));
      Assert.assertEquals("dirC/fileC1v2", readFromFile(readFileC1));
      Assert.assertTrue(
          readFileC1.getPathFromRoot().equals(new VPath("dirC/fileC1", '/')));
      InputVDir readDirA = inputVFS.getRootInputVDir().getInputVDir(new VPath("dirA", '/'));
      InputVDir readDirAA = readDirA.getInputVDir(new VPath("dirAA", '/'));
      InputVDir readDirAAB = readDirAA.getInputVDir(new VPath("dirAAB", '/'));
      InputVFile readFileAAB2 = readDirAAB.getInputVFile(new VPath("fileAAB2", '/'));
      Assert.assertEquals("dirA/dirAA/dirAAB/fileAAB2", readFromFile(readFileAAB2));
      Assert.assertTrue(
          readFileAAB2.getPathFromRoot().equals(new VPath("dirA/dirAA/dirAAB/fileAAB2", '/')));

      // delete some new stuff
      readFileAAB2.delete();
      try {
        readDirAAB.getInputVFile(new VPath("fileAAB2", '/'));
        Assert.fail();
      } catch (NoSuchFileException e) {
        // expected
      }

      // try to delete a file that is in the dir and the zip, then check that the one from the zip
      // remains
      inputVFS.getRootInputVDir().getInputVFile(new VPath("dirC/fileC1", '/')).delete();
      Assert.assertEquals("dirC/fileC1", readFromFile(
          inputVFS.getRootInputVDir().getInputVFile(new VPath("dirC/fileC1", '/'))));

      // list contents of "dirB/dirBA", which has files in the dir and in the zip
      InputVDir dirBA = inputVFS.getRootInputVDir().getInputVDir(new VPath("dirB/dirBA", '/'));
      Collection<? extends InputVElement> dirBAList = dirBA.list();
      Assert.assertEquals(2, dirBAList.size());
      for (InputVElement subElement : dirBAList) {
        if (subElement.getName().equals("fileBA2")) {
          InputVFile readFileBA22 = (InputVFile) subElement;
          Assert.assertEquals("dirB/dirBA/fileBA2", readFromFile(readFileBA22));
          Assert.assertTrue(
              readFileBA22.getPathFromRoot().equals(new VPath("dirB/dirBA/fileBA2", '/')));
        } else if (subElement.getName().equals("fileBA1")) {
          InputVFile readFileBA1 = (InputVFile) subElement;
          Assert.assertEquals("dirB/dirBA/fileBA1", readFromFile(readFileBA1));
          Assert.assertTrue(
              readFileBA1.getPathFromRoot().equals(new VPath("dirB/dirBA/fileBA1", '/')));
        } else {
          throw new AssertionError();
        }
      }

      // retest input
      testInputVFS(new GenericInputVFS(prefixedFS));

      prefixedFS.close();
      rwzfs.close();

    } finally {
      if (inputZipFile != null) {
        Assert.assertTrue(inputZipFile.delete());
      }
      if (outputZipFile != null) {
        Assert.assertTrue(outputZipFile.delete());
      }
      if (dir != null) {
        FileUtils.deleteDir(dir);
      }
    }
  }

  private void testOutputVFS(@Nonnull InputOutputVFS outputVFS) throws NotDirectoryException,
      CannotCreateFileException, IOException, WrongPermissionException {

    // create stuff from root dir
    InputOutputVDir dirA = (InputOutputVDir) outputVFS.getRootInputOutputVDir().createOutputVDir(
        new VPath("dirA", '/'));
    outputVFS.getRootInputOutputVDir().createOutputVDir(
        new VPath("dirB/dirBA", '/'));
    OutputVFile file1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("file1", '/'));
    writeToFile(file1, "file1");
    OutputVFile fileA1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirA/fileA1", '/'));
    writeToFile(fileA1, "dirA/fileA1");
    OutputVFile fileB1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirB/fileB1", '/'));
    writeToFile(fileB1, "dirB/fileB1");
    OutputVFile fileBA1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirB/dirBA/fileBA1", '/'));
    writeToFile(fileBA1, "dirB/dirBA/fileBA1");
    OutputVFile fileBB1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirB/dirBB/fileBB1", '/'));
    writeToFile(fileBB1, "dirB/dirBB/fileBB1");
    OutputVFile fileC1 =
        outputVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirC/fileC1", '/'));
    writeToFile(fileC1, "dirC/fileC1");

    // create stuff from dirA
    InputOutputVDir dirAA = (InputOutputVDir) dirA.createOutputVDir(new VPath("dirAA", '/'));
    OutputVFile fileA2 = dirA.createOutputVFile(new VPath("fileA2", '/'));
    writeToFile(fileA2, "dirA/fileA2");

    // create stuff from dirAA
    dirAA.createOutputVDir(new VPath("dirAAA/dirAAAA", '/'));
    OutputVFile fileAAB1 = dirAA.createOutputVFile(new VPath("dirAAB/fileAAB1", '/'));
    writeToFile(fileAAB1, "dirA/dirAA/dirAAB/fileAAB1");
  }

  private void testInputVFS(@Nonnull InputVFS inputVFS) throws NoSuchFileException, IOException,
      NotFileOrDirectoryException, WrongPermissionException {
    InputVFile file1 = inputVFS.getRootInputVDir().getInputVFile(new VPath("file1", '/'));
    Assert.assertEquals("file1", readFromFile(file1));
    Assert.assertTrue(file1.getPathFromRoot().equals(new VPath("file1", '/')));

    InputVDir dirA = inputVFS.getRootInputVDir().getInputVDir(new VPath("dirA", '/'));
    Collection<? extends InputVElement> dirAElements = dirA.list();
    Assert.assertEquals(3, dirAElements.size());
    Assert.assertTrue(containsFile(dirAElements, "fileA1", "dirA/fileA1"));
    Assert.assertTrue(containsFile(dirAElements, "fileA2", "dirA/fileA2"));
    Assert.assertTrue(containsDir(dirAElements, "dirAA"));

    InputVFile fileAAB1 =
        inputVFS.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
    Assert.assertEquals("dirA/dirAA/dirAAB/fileAAB1", readFromFile(fileAAB1));
    Assert.assertTrue(
        fileAAB1.getPathFromRoot().equals(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/')));

    InputVDir dirB = inputVFS.getRootInputVDir().getInputVDir(new VPath("dirB", '/'));
    InputVDir dirBA = dirB.getInputVDir(new VPath("dirBA", '/'));
    InputVFile fileBA1 = dirBA.getInputVFile(new VPath("fileBA1", '/'));
    Assert.assertEquals("dirB/dirBA/fileBA1", readFromFile(fileBA1));
    Assert.assertTrue(fileBA1.getPathFromRoot().equals(new VPath("dirB/dirBA/fileBA1", '/')));

    InputVDir dirBB = inputVFS.getRootInputVDir().getInputVDir(new VPath("dirB/dirBB", '/'));
    InputVFile fileBB1 = dirBB.getInputVFile(new VPath("fileBB1", '/'));
    Assert.assertEquals("dirB/dirBB/fileBB1", readFromFile(fileBB1));
    Assert.assertTrue(fileBB1.getPathFromRoot().equals(new VPath("dirB/dirBB/fileBB1", '/')));
  }

  @Test
  public void testReadWriteZipFSAndReadZipFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, ZipException, CannotCloseException {
    File file = null;
    InputOutputVFS zipVFS = null;
    InputVFS inputZipVFS = null;
    try {
      file = File.createTempFile("vfs", ".zip");
      String path = file.getAbsolutePath();
      Provider.Service sha1 = getSha1Service();
      zipVFS = new GenericInputOutputVFS(new ReadWriteZipFS(
          new OutputZipFile(path, null, Existence.MAY_EXIST, ChangePermission.NOCHANGE,
              Compression.COMPRESSED),
          /* numGroups = */ 1, /* groupSize = */ 2, new MessageDigestFactory(sha1),
          /* debug = */ false));
      testOutputVFS(zipVFS);
      testDelete(zipVFS);
//      checkZipLocations(zipVFS);
      testInputVFS(zipVFS);
      zipVFS.close();
      inputZipVFS = new GenericInputVFS(new ReadZipFS(new InputZipFile(path)));
      testInputVFS(inputZipVFS);
      checkZipLocations(inputZipVFS);
    } finally {
      if (zipVFS != null) {
        zipVFS.close();
      }
      if (inputZipVFS != null) {
        inputZipVFS.close();
      }
      if (file != null) {
        Assert.assertTrue(file.delete());
      }
    }
  }

  @Test
  public void testDeflateFSWithMessageDigestFS()
      throws CannotCreateFileException, WrongPermissionException, CannotChangePermissionException,
      NoSuchFileException, FileAlreadyExistsException, IOException, CannotDeleteFileException,
      NotFileOrDirectoryException, ZipException, CannotCloseException, BadVFSFormatException {
    File file = null;
    InputOutputVFS ioVFS1 = null;
    InputVFS ioVFS2 = null;
    try {
      file = File.createTempFile("vfs", ".zip");

      Provider.Service sha1 = getSha1Service();

      ioVFS1 = new GenericInputOutputVFS(new DeflateFS(new MessageDigestFS(
          new ReadWriteZipFS(
              new OutputZipFile(file.getPath(), null, Existence.MAY_EXIST,
                  ChangePermission.NOCHANGE, Compression.UNCOMPRESSED),
              /* numGroups = */ 1, /* groupSize = */ 2, new MessageDigestFactory(sha1),
              /* debug = */ false),
          new MessageDigestFactory(sha1))));
      testOutputVFS(ioVFS1);
      testDelete(ioVFS1);
      testInputVFS(ioVFS1);
      InputVFile fileAAB1 =
          ioVFS1.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest = ((GenericInputVFile) fileAAB1).getDigest();
      Assert.assertNotNull(fileAAB1digest);
      String vfsDigest = ioVFS1.getDigest();
      Assert.assertNotNull(vfsDigest);
      ioVFS1.close();

      ioVFS2 = new GenericInputVFS(new DeflateFS(new MessageDigestFS(new ReadZipFS(
          new InputZipFile(file.getPath())),
          new MessageDigestFactory(sha1))));
      testInputVFS(ioVFS2);

      InputVFile fileAAB1b =
          ioVFS2.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
      String fileAAB1digest2 = ((GenericInputVFile) fileAAB1b).getDigest();
      Assert.assertEquals(fileAAB1digest, fileAAB1digest2);
      String vfsDigest2 = ioVFS2.getDigest();
      Assert.assertEquals(vfsDigest, vfsDigest2);

    } finally {
      if (ioVFS1 != null) {
        ioVFS1.close();
      }
      if (ioVFS2 != null) {
        ioVFS2.close();
      }
      if (file != null) {
        Assert.assertTrue(file.delete());
      }
    }
  }

  private void testDelete(@Nonnull InputOutputVFS ioVFS)
      throws NoSuchFileException, CannotDeleteFileException, NotFileOrDirectoryException,
      CannotCreateFileException, IOException, WrongPermissionException {

    // let's delete "dirA/dirAA/dirAAB/fileAAB1"
    InputOutputVDir dirA = ioVFS.getRootInputOutputVDir().getInputVDir(new VPath("dirA", '/'));
    {
      InputOutputVFile fileAAB1 = dirA.getInputVFile(new VPath("dirAA/dirAAB/fileAAB1", '/'));
      fileAAB1.delete();
      try {
        ioVFS.getRootInputVDir().getInputVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
        Assert.fail();
      } catch (NoSuchFileException e) {
        // expected
      }
    }

    // let's delete "dirB/dirBB/fileBB1"
    InputOutputVDir dirBB =
        ioVFS.getRootInputOutputVDir().getInputVDir(new VPath("dirB/dirBB", '/'));
    {
      InputOutputVFile fileBB1 = dirBB.getInputVFile(new VPath("fileBB1", '/'));
      fileBB1.delete();
      try {
        ioVFS.getRootInputVDir().getInputVFile(new VPath("dirB/dirBB/fileBB1", '/'));
        Assert.fail();
      } catch (NoSuchFileException e) {
        // expected
      }
    }

    // let's delete "dirC/fileC1"
    {
      InputOutputVFile fileC1 = ioVFS.getRootInputOutputVDir().getInputVFile(
          new VPath("dirC/fileC1", '/'));
      fileC1.delete();
      try {
        ioVFS.getRootInputVDir().getInputVFile(new VPath("dirC/fileC1", '/'));
        Assert.fail();
      } catch (NoSuchFileException e) {
        // expected
      }
    }

    // let's re-create the files we've deleted to leave the VFS in the same state as before.
    {
      OutputVFile fileAAB1 = dirA.createOutputVFile(new VPath("dirAA/dirAAB/fileAAB1", '/'));
      writeToFile(fileAAB1, "dirA/dirAA/dirAAB/fileAAB1");
      OutputVFile fileBB1 = dirBB.createOutputVFile(new VPath("fileBB1", '/'));
      writeToFile(fileBB1, "dirB/dirBB/fileBB1");
      OutputVFile fileC1 =
          ioVFS.getRootInputOutputVDir().createOutputVFile(new VPath("dirC/fileC1", '/'));
      writeToFile(fileC1, "dirC/fileC1");
    }
  }


  private void checkFileLocations(@Nonnull InputVFS inputVFS) throws NotFileOrDirectoryException,
      NoSuchFileException {
    VPath fileAAB1Path = new VPath("dirA/dirAA/dirAAB/fileAAB1", '/');
    InputVFile fileAAB1 = inputVFS.getRootInputVDir().getInputVFile(fileAAB1Path);
    FileLocation fileAAB1Location = (FileLocation) fileAAB1.getLocation();
    Assert.assertTrue(fileAAB1Location.getDescription().contains("file"));
    Assert.assertTrue(fileAAB1Location.getDescription().contains(
        fileAAB1Path.getPathAsString(File.separatorChar)));

    VPath dirBBPath = new VPath("dirB/dirBB", '/');
    InputVDir dirBB = inputVFS.getRootInputVDir().getInputVDir(dirBBPath);
    DirectoryLocation dirBBLocation = (DirectoryLocation) dirBB.getLocation();
    Assert.assertTrue(dirBBLocation.getDescription().contains("directory"));
    Assert.assertTrue(dirBBLocation.getDescription().contains(
        dirBBPath.getPathAsString(File.separatorChar)));
  }

  private void checkZipLocations(@Nonnull InputVFS inputVFS, @CheckForNull String prefix)
      throws NotFileOrDirectoryException, NoSuchFileException {
    VPath fileAAB1Path = new VPath("dirA/dirAA/dirAAB/fileAAB1", '/');
    InputVFile fileAAB1 = inputVFS.getRootInputVDir().getInputVFile(fileAAB1Path);
    ZipLocation fileAAB1Location = (ZipLocation) fileAAB1.getLocation();
    Assert.assertTrue(fileAAB1Location.getDescription().contains(".zip"));
    String fileAAB1EntryName = getEntryName(fileAAB1Path, prefix, false);
    Assert.assertTrue(fileAAB1Location.getDescription().contains(fileAAB1EntryName));

    VPath dirBBPath = new VPath("dirB/dirBB", '/');
    InputVDir dirBB = inputVFS.getRootInputVDir().getInputVDir(dirBBPath);
    ZipLocation dirBBLocation = (ZipLocation) dirBB.getLocation();
    Assert.assertTrue(dirBBLocation.getDescription().contains(".zip"));
    String dirBBEntryName = getEntryName(dirBBPath, prefix, true);
    Assert.assertTrue(dirBBLocation.getDescription().contains(dirBBEntryName));
  }
  private void checkZipLocations(@Nonnull InputVFS inputVFS)
      throws NotFileOrDirectoryException, NoSuchFileException {
    checkZipLocations(inputVFS, null);
  }

  private String getEntryName(@Nonnull VPath path, @CheckForNull String prefix, boolean isDir) {
    StringBuffer entryName = new StringBuffer("entry '");
    if (prefix != null) {
      entryName.append(ZipUtils.ZIP_SEPARATOR);
      entryName.append(prefix);
    }
    entryName.append(ZipUtils.ZIP_SEPARATOR);
    entryName.append(path.getPathAsString(ZipUtils.ZIP_SEPARATOR));
    if (isDir) {
      entryName.append(ZipUtils.ZIP_SEPARATOR);
    }
    entryName.append('\'');
    return entryName.toString();
  }

  private void checkUnicity(@Nonnull VFS vfs) throws NotDirectoryException, NoSuchFileException,
      NotFileException {
    // check file unicity
    VDir dirA = vfs.getRootDir().getVDir("dirA");
    VDir dirAAv1 = dirA.getVDir("dirAA");
    VDir dirAAv2 = vfs.getRootDir().getVDir(new VPath("dirA/dirAA", '/'));
    VDir dirAAv3 = null;
    for (VElement subElement : dirA.list()) {
      if (subElement.getName().equals("dirAA")) {
        dirAAv3 = (VDir) subElement;
        break;
      }
    }
    Assert.assertNotNull(dirAAv3);
    Assert.assertTrue(dirAAv1 == dirAAv2);
    Assert.assertTrue(dirAAv2 == dirAAv3);

    // check dir unicity
    VDir dirAAB = dirAAv1.getVDir("dirAAB");
    VFile fileAAB1v1 = dirAAB.getVFile("fileAAB1");
    VFile fileAAB1v2 = dirAAv2.getVFile(new VPath("dirAAB/fileAAB1", '/'));
    VFile fileAAB1v3 = vfs.getRootDir().getVFile(new VPath("dirA/dirAA/dirAAB/fileAAB1", '/'));
    VFile fileAAB1v4 = null;
    for (VElement subElement : dirAAB.list()) {
      if (subElement.getName().equals("fileAAB1")) {
        fileAAB1v4 = (VFile) subElement;
        break;
      }
    }
    Assert.assertNotNull(fileAAB1v4);
    Assert.assertTrue(fileAAB1v1 == fileAAB1v2);
    Assert.assertTrue(fileAAB1v2 == fileAAB1v3);
    Assert.assertTrue(fileAAB1v3 == fileAAB1v4);
  }

  private boolean containsFile(@Nonnull Collection<? extends InputVElement> elements,
      @Nonnull String fileSimpleName, @Nonnull String fileContent)
      throws IOException, WrongPermissionException {
    for (VElement element : elements) {
      if (element.getName().equals(fileSimpleName)) {
        return !element.isVDir()
            && fileContent.equals(readFromFile((InputVFile) element));
      }
    }

    return false;
  }

  private boolean containsDir(@Nonnull Collection<? extends InputVElement> elements,
      @Nonnull String fileSimpleName) {
    for (VElement element : elements) {
      if (element.getName().equals(fileSimpleName)) {
        return element.isVDir();
      }
    }

    return false;
  }

  private void writeToFile(@Nonnull OutputVFile file, @Nonnull String string)
      throws IOException, WrongPermissionException {
    OutputStreamWriter writer = new OutputStreamWriter(file.getOutputStream());
    writer.write(string);
    writer.close();
  }

  @Nonnull
  private String readFromFile(@Nonnull InputVFile file)
      throws IOException, WrongPermissionException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
    String string = reader.readLine();
    Assert.assertNull(reader.readLine());
    reader.close();
    return string;
  }

  @Nonnull
  private static Provider.Service getSha1Service() {
    Provider.Service sha1 = null;
    for (Provider provider : Security.getProviders()) {
      for (Provider.Service service : provider.getServices()) {
        if (service.getType().equals("MessageDigest") && service.getAlgorithm().equals("SHA")) {
          sha1 = service;
        }
      }
    }
    Assert.assertNotNull(sha1);
    return sha1;
  }

}
