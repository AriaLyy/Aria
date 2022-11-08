/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aria.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

/**
 * FTPFileEntryParser defines the interface for parsing a single FTP file
 * listing and converting that information into an
 * {@link FTPFile} instance.
 * Sometimes you will want to parse unusual listing formats, in which
 * case you would create your own implementation of FTPFileEntryParser and
 * if necessary, subclass FTPFile.
 * <p>
 * Here are some examples showing how to use one of the classes that
 * implement this interface.
 * <p>
 *
 * The first example uses the <code>FTPClient.listFiles()</code>
 * API to pull the whole list from the subfolder <code>subfolder</code> in
 * one call, attempting to automatically detect the parser type.  This
 * method, without a parserKey parameter, indicates that autodection should
 * be used.
 *
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = f.listFiles("subfolder");
 * </pre>
 *
 * The second example uses the <code>FTPClient.listFiles()</code>
 * API to pull the whole list from the current working directory in one call,
 * but specifying by classname the parser to be used.  For this particular
 * parser class, this approach is necessary since there is no way to
 * autodetect this server type.
 *
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = f.listFiles(
 *      "org.apache.commons.net.ftp.parser.EnterpriseUnixFTPFileEntryParser",
 *      ".");
 * </pre>
 *
 * The third example uses the <code>FTPClient.listFiles()</code>
 * API to pull a single file listing in an arbitrary directory in one call,
 * specifying by KEY the parser to be used, in this case, VMS.
 *
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = f.listFiles("VMS", "subfolder/foo.java");
 * </pre>
 *
 * For an alternative approach, see the {@link FTPListParseEngine} class
 * which provides iterative access.
 *
 * @version $Id: FTPFileEntryParser.java 1747119 2016-06-07 02:22:24Z ggregory $
 * @see FTPFile
 * @see FTPClient#listFiles()
 */
public interface FTPFileEntryParser {
  /**
   * Parses a line of an FTP server file listing and converts it into a usable
   * format in the form of an <code> FTPFile </code> instance.  If the
   * file listing line doesn't describe a file, <code> null </code> should be
   * returned, otherwise a <code> FTPFile </code> instance representing the
   * files in the directory is returned.
   *
   * @param listEntry A line of text from the file listing
   * @return An FTPFile instance corresponding to the supplied entry
   */
  FTPFile parseFTPEntry(String listEntry);

  /**
   * Reads the next entry using the supplied BufferedReader object up to
   * whatever delemits one entry from the next.  Implementors must define
   * this for the particular ftp system being parsed.  In many but not all
   * cases, this can be defined simply by calling BufferedReader.readLine().
   *
   * @param reader The BufferedReader object from which entries are to be
   * read.
   * @return A string representing the next ftp entry or null if none found.
   * @throws IOException thrown on any IO Error reading from the reader.
   */
  String readNextEntry(BufferedReader reader) throws IOException;

  /**
   * This method is a hook for those implementors (such as
   * VMSVersioningFTPEntryParser, and possibly others) which need to
   * perform some action upon the FTPFileList after it has been created
   * from the server stream, but before any clients see the list.
   *
   * The default implementation can be a no-op.
   *
   * @param original Original list after it has been created from the server stream
   * @return Original list as processed by this method.
   */
  List<String> preParse(List<String> original);
}


/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
