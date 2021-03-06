package saros.negotiation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.log4j.Logger;
import saros.exceptions.OperationCanceledException;
import saros.filesystem.FileSystem;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IWorkspace;
import saros.filesystem.IWorkspaceRunnable;
import saros.monitoring.CancelableInputStream;
import saros.monitoring.IProgressMonitor;
import saros.session.ISarosSession;

public class DecompressArchiveTask implements IWorkspaceRunnable {

  private static final Logger log = Logger.getLogger(DecompressArchiveTask.class);

  private final File file;
  private final IProgressMonitor monitor;
  private final Map<String, IReferencePoint> idToReferencePointMapping;
  private final String delimiter;

  /**
   * Creates a decompress task for an archive file that can be executed by {@link IWorkspace#run}.
   * All necessary folders will be created on the fly. <b>Important:</b> Existing files will be
   * <b>overwritten without confirmation</b>!
   *
   * @param file Zip file containing the compressed data
   * @param idToReferencePointMapping map containing the id to reference point mapping (see also
   *     {@link ISarosSession#getReferencePointId(IReferencePoint)}
   * @param monitor monitor that is used for progress report and cancellation or <code>null</code>
   *     to use the monitor provided by the {@link #run(IProgressMonitor)} method
   */
  public DecompressArchiveTask(
      final File file,
      final Map<String, IReferencePoint> idToReferencePointMapping,
      final String delimiter,
      final IProgressMonitor monitor) {
    this.file = file;
    this.idToReferencePointMapping = idToReferencePointMapping;
    this.delimiter = delimiter;
    this.monitor = monitor;
  }

  // TODO extract as much as possible even on some failures
  /*
   * optional smoother progress ... use bytes written which will result in
   * better response if there exists big files in the archive
   */
  @Override
  public void run(IProgressMonitor monitor) throws IOException, OperationCanceledException {
    if (this.monitor != null) monitor = this.monitor;

    ZipFile zipFile = null;

    try {

      zipFile = new ZipFile(file);

      monitor.beginTask("Unpacking archive file to workspace", zipFile.size());

      for (Enumeration<? extends ZipEntry> entries = zipFile.entries();
          entries.hasMoreElements(); ) {

        final ZipEntry entry = entries.nextElement();

        final String entryName = entry.getName();

        if (monitor.isCanceled()) throw new OperationCanceledException();

        final int delimiterIdx = entry.getName().indexOf(delimiter);

        if (delimiterIdx == -1) {
          log.warn("skipping zip entry " + entryName + ", entry is not valid");

          monitor.worked(1);
          continue;
        }

        final String id = entryName.substring(0, delimiterIdx);

        final String path = entryName.substring(delimiterIdx + 1, entryName.length());

        final IReferencePoint referencePoint = idToReferencePointMapping.get(id);

        if (referencePoint == null) {
          log.warn("skipping zip entry " + entryName + ", unknown reference point id: " + id);

          monitor.worked(1);
          continue;
        }

        final IFile decompressedFile = referencePoint.getFile(path);

        FileSystem.createFolder(decompressedFile);

        monitor.subTask("decompressing: " + path);

        final InputStream inZip = zipFile.getInputStream(entry);

        CancelableInputStream in;
        in = new CancelableInputStream(inZip, monitor);

        try {
          if (!decompressedFile.exists()) decompressedFile.create(in);
          else decompressedFile.setContents(in);
        } catch (IOException e) {
          /* if triggered by check in CancelableInputStream */
          if (monitor.isCanceled()) {
            throw new OperationCanceledException();
          } else {
            throw e;
          }
        }

        monitor.worked(1);

        if (log.isTraceEnabled()) log.trace("file written to disk: " + path);
      }
    } finally {
      if (monitor != null) monitor.done();

      try {
        if (zipFile != null) zipFile.close();
      } catch (IOException e) {
        log.warn("failed to close zip file " + zipFile.getName() + " : " + e.getMessage());
      }
    }
  }
}
