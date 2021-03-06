package com.zutubi.diff;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a collection of patches within a single patch file.  For example,
 * the patch file could be the output of a unified diff over a directory tree.
 */
public class PatchFile
{
    private List<Patch> patches = new LinkedList<Patch>();
    private List<String> extendedInfo;

    /**
     * Returns a list of patches, one for each changed file in this patch file.
     *
     * @return the patches in this patch file (unmodifiable)
     */
    public List<Patch> getPatches()
    {
        return Collections.unmodifiableList(patches);
    }

    /**
     * Returns the extended header lines in this patch file that may contain
     * a information such as commit message.
     *
     * @return the extended info lines in this patch file (unmodifiable)
     */
    public List<String> getExtendedInfo()
    {
        return Collections.unmodifiableList(extendedInfo);
    }

    /**
     * Adds the given patch to this file.
     *
     * @param patch the patch to add
     */
    public void addPatch(Patch patch)
    {
        patches.add(patch);
    }

    /**
     * Adds the given extended header lines to this file.
     *
     * @param extendedInfo the extended header lines to add
     */
    public void addExtendedInfo(List<String> extendedInfo)
    {
        this.extendedInfo = new LinkedList<String>(extendedInfo);
    }

    /**
     * Apply this patch to files within the given directory.  The equivalent of
     * the Unix patch command.  Only supports clean patches - no "fuzz" is
     * allowed.
     * 
     * @param baseDir          base directory used to locate files to patch
     *                         (equivalent to the working directory or -d flag
     *                         to patch)
     * @param prefixStripCount number of path elements to strip from the paths
     *                         of patches when locating files to patch
     *                         (equivalent to the -p option to patch)
     * @throws PatchApplyException if the patch does not apply cleanly or an
     *         I/O error occurs
     */
    public void apply(File baseDir, int prefixStripCount) throws PatchApplyException
    {
        for (Patch patch: patches)
        {
            File destinationFile = resolveDestination(baseDir, patch.getNewFile(), prefixStripCount);
            if (!patch.getType().isFileCreated() && !destinationFile.exists())
            {
                throw new PatchApplyException("Expected destination file '" + destinationFile.getAbsolutePath() + "' does not exist");
            }

            patch.apply(destinationFile);
        }
    }

    private File resolveDestination(File baseDir, String newFile, int prefixStripCount)
    {
        newFile = newFile.replace('\\', '/');
        int offset = 0;
        int i = 0;
        while (prefixStripCount-- > 0)
        {
            i = newFile.indexOf('/', offset);
            if (i < 0 || i == newFile.length())
            {
                break;
            }

            offset = i + 1;
        }

        return new File(baseDir, newFile.substring(i));
    }
}
