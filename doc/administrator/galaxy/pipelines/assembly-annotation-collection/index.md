---
layout: default
search_title: "IRIDA Assembly and Annotation Collection"
description: "Install guide for the assembly and annotation collection pipeline."
---

Assembly and Annotation Collection
==================================

This workflow can be used for assemblying and annotating many genomes in one submission.  The results from one submission will be packaged together into a single file.  The workflow uses the software [SPAdes][] and [Prokka][] for assembly and annotation of genomes as well as a few tools for filtering of data and generating assembly statistics.  The specific Galaxy tools are listed in the table below.

| Tool Name                  | Tool Revision | Toolshed Installable Revision | Toolshed             |
|:--------------------------:|:-------------:|:-----------------------------:|:--------------------:|
| **flash**                  | 4287dd541327  | 0 (2015-05-05)                | [IRIDA Toolshed][]   |
| **filter_spades_repeats**  | f9fc830fa47c  | 0 (2015-05-05)                | [IRIDA Toolshed][]   |
| **assemblystats**          | 51b76a5d78a5  | 1 (2015-05-07)                | [IRIDA Toolshed][]   |
| **bundle_collections**     | 7bc329e1ada4  | 0 (2015-05-20)                | [IRIDA Toolshed][]   |
| **combine_assembly_stats** | c970d74729e9  | 0 (2015-05-20)                | [IRIDA Toolshed][]   |
| **spades**                 | 21734680d921  | 14 (2015-02-27)               | [Galaxy Main Shed][] |
| **prokka**                 | f5e44aad6498  | 7 (2015-10-01)                | [Galaxy Main Shed][] |
| **regex_find_replace**     | 9ea374bb0350  | 0 (2014-03-29)                | [Galaxy Main Shed][] |

To install these tools please proceed through the following steps.

## Step 1: Install Dependencies

Some of these tools require additional dependencies to be installed.  For a cluster environment please make sure these are available on all cluster nodes by installing to a shared directory.

1. [Java][]:  Please download and install [Java][] version 1.6+ or make sure it is available in your execution environment.
2. [gnuplot][]: Please download and install [gnuplot][] or make sure this is available in your execution environment.
2. **Perl Modules**: Please download and install dependency Perl modules with the command.

```bash
cpanm Time::Piece XML::Simple Data::Dumper
```

In addition, [BioPerl][] version 1.6.901 must be installed.  Please run the following command to install.

```bash
cpanm http://search.cpan.org/CPAN/authors/id/C/CJ/CJFIELDS/BioPerl-1.6.901.tar.gz
```

## Step 2: Install Galaxy Tools

Please install all the Galaxy tools in the table above by logging into Galaxy, navigating to **Admin > Search and browse tool sheds**, searching for the appropriate **Tool Name** and installing the appropriate **Toolshed Installable Revision**.

The install progress can be checked by monitoring the Galaxy log file `$GALAXY_BASE_DIR/main.log`.  On completion you should see a message of `Installed` next to the tool when going to **Admin > Manage installed tool shed repositories**.

**Note**: Prokka downloads several large databases and may take some time to install.

### Updating `tbl2asn`

The assembly workflow makes use of the software [Prokka][] for geneome annotation.  Prokka makes use of [tbl2asn][], which has been programmed to stop working after 1 year from being built.  The next date of expiry will be **March 31, 2016**.  After this date, `tbl2asn` will have to be updated before the assembly workflow is to be run.  A quick way to update `tbl2asn` is the following.

1. Download the new `tbl2asn` binary from <ftp://ftp.ncbi.nih.gov/toolbox/ncbi_tools/converters/by_program/tbl2asn/>.
2. Copy the binary over the previously installed locations in Galaxy.  These can be found with the command:

        find tool_dependencies/ -wholename '*f5e44aad6498/*tbl2asn' -or -wholename '*package_tbl2asn_24_3/*tbl2asn'

    Where **tool_dependencies/** is the location of all the installed Galaxy tool dependencies.

## Step 3: Testing Pipeline

A Galaxy workflow and some test data has been included with this documentation to verify that all tools are installed correctly.  To test this pipeline, please proceed through the following steps.

1. Upload the [Assembly Annotation Galaxy Workflow][] by going to **Workflow > Upload or import workflow**.
2. Upload the sequence reads by going to **Analyze Data** and then clicking on the **upload files from disk** icon ![upload-icon][].  Select the [test/reads][] files.  Make sure to change the **Type** of each file from **Auto-detect** to **fastqsanger**.  When uploaded you should see the following in your history.

    ![upload-history][]

3. Construct a dataset collection of the paired-end reads by clicking the **Operations on multiple datasets** icon ![datasets-icon][].  Please check off the two **.fastq** files and then go to **For all selected... > Build List of dataset pairs**.  You should see a screen that looks as follows.

    ![dataset-pair-screen][]

4. This should have properly paired your data and named the sample **a**.  Enter the name of this paired dataset collection at the bottom and click **Create list**.
5. Run the uploaded workflow by clicking on **Workflow**, clicking on the name of the workflow **FLASH, SPAdes and Prokka (imported from uploaded file)** and clicking **Run**.  This should auto fill in the dataset collection.  At the very bottom of the screen click **Run workflow**.
6. If everything was installed correctly, you should see each of the tools run successfully (turn green).  On completion this should look like.

    ![workflow-success][]

    If you see any tool turn red, you can click on the view details icon ![view-details-icon][] for more information.

If everything was successfull then all dependencies for this pipeline have been properly installed.

[SPAdes]: http://bioinf.spbau.ru/spades
[Prokka]: http://www.vicbioinformatics.com/software.prokka.shtml
[tbl2asn]: http://www.ncbi.nlm.nih.gov/genbank/tbl2asn2/
[Galaxy Main Shed]: http://toolshed.g2.bx.psu.edu/
[IRIDA Toolshed]: https://irida.corefacility.ca/galaxy-shed
[Java]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[gnuplot]: http://www.gnuplot.info/
[BioPerl]: http://www.bioperl.org/wiki/Main_Page
[Assembly Annotation Galaxy Workflow]: ../test/assembly-annotation-collection/assembly-annotation-collection.ga
[upload-icon]: ../test/snvphyl/images/upload-icon.jpg
[test/reads]: ../test/assembly-annotation/reads
[upload-history]: ../test/assembly-annotation/images/upload-history.jpg
[datasets-icon]: ../test/snvphyl/images/datasets-icon.jpg
[dataset-pair-screen]: ../test/assembly-annotation/images/dataset-pair-screen.jpg
[workflow-success]: ../test/assembly-annotation/images/workflow-success.png
[view-details-icon]: ../test/snvphyl/images/view-details-icon.jpg
