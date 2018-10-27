/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2018, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package schemacrawler.test.utility;


import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.size;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.fail;
import static schemacrawler.test.utility.TestUtility.compareOutput;
import static sf.util.Utility.isBlank;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import sf.util.IOUtility;

public final class TestOutputStream
  extends OutputStream
  implements TestOutputCapture
{

  private final OutputStream out;
  private final Path tempFile;
  private final String outputformat;

  public TestOutputStream()
    throws IOException
  {
    this(null);
  }

  public TestOutputStream(final String outputformat)
    throws IOException
  {
    tempFile = IOUtility.createTempFilePath("test", "out");
    out = newOutputStream(tempFile, WRITE, CREATE, TRUNCATE_EXISTING);
    if (isBlank(outputformat))
    {
      this.outputformat = "text";
    }
    else
    {
      this.outputformat = outputformat;
    }
  }

  @Override
  public void assertEmpty()
    throws Exception
  {
    out.flush();
    out.close();

    if (size(tempFile) > 0)
    {
      fail("Output is not empty");
    }
  }

  @Override
  public void assertEquals(final String referenceFile)
    throws Exception
  {
    final List<String> failures = collectFailures(referenceFile);
    if (!failures.isEmpty())
    {
      fail(failures.toString());
    }
  }

  @Override
  public void close()
    throws IOException
  {
    out.flush();
    out.close();

    try
    {
      deleteIfExists(tempFile);
    }
    catch (final Throwable e)
    {
      // Ignore
    }
  }

  @Override
  public List<String> collectFailures(final String referenceFile)
    throws Exception
  {
    out.flush();
    out.close();

    requireNonNull(referenceFile, "No reference file provided");
    final List<String> failures = compareOutput(referenceFile,
                                                tempFile,
                                                outputformat,
                                                false);
    return failures;
  }

  @Override
  public Path getFilePath()
  {
    return tempFile;
  }

  @Override
  public String getLog()
  {
    try
    {
      out.flush();
      out.close();
      return new String(readAllBytes(tempFile), StandardCharsets.UTF_8);
    }
    catch (final IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toString()
  {
    return tempFile.toString();
  }

  @Override
  public void write(final int b)
    throws IOException
  {
    out.write(b);
  }

}