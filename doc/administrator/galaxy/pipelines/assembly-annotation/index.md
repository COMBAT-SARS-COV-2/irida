---
layout: default
search_title: "IRIDA Assembly and Annotation"
description: "Install guide for the assembly and annotation pipeline."
---

Assembly and Annotation
=======================

This workflow uses the [shovill] and [Prokka][] software for assembly and annotation of genomes, respectively, as well as [QUAST] for assembly quality assessment.  The specific Galaxy tools are listed in the table below.

| Tool Name                  | Owner    | Tool Revision  | Toolshed Installable Revision | Toolshed             |
|:--------------------------:|:--------:|:--------------:|:-----------------------------:|:--------------------:|
| **shovill**                | iuc      | [57d5928f456e] | 1 (2018-03-07)                | [Galaxy Main Shed][] |
| **prokka**                 | crs4     | [eaee459f3d69] | 14 (2018-03-28)               | [Galaxy Main Shed][] |
| **quast**                  | iuc      | [0834c823d4b9] | 4 (2018-02-12)                | [Galaxy Main Shed][] |


To install these tools please proceed through the following steps.

## Step 1: Galaxy Conda Setup

Galaxy makes use of [Conda][conda] to automatically install some dependencies for this workflow.  Please verify that the version of Galaxy is >= v16.01 and has been setup to use conda (by modifying the appropriate configuration settings, see [here][galaxy-config] for additional details).  A method to get this workflow to work with a Galaxy version < v16.01 is available in [FAQ/Conda dependencies][].

### Address shovill related issues 

#### Error 256 from running `kmc`/`samtools` 

You will need to install the correct versions of some dependencies for `kmc`/`samtools` so after installing `shovill`: 

```bash
# activate the Galaxy shovill conda env
source galaxy/deps/_conda/bin/activate galaxy/deps/_conda/envs/__shovill@0.9.0
# install ncurses and bzip2 from conda-forge channel
conda install -c conda-forge ncurses bzip2
```

#### [PILON] Java/JVM heap allocation issues

[PILON] is a Java application and may require the JVM heap size to be set (e.g. `_JAVA_OPTIONS=-Xmx4g`).
If [shovill] under Galaxy submits jobs to a [SLURM] workload manager, it may be necessary to allot about 4G more through SLURM than through [shovill] `--ram` (default is `${SHOVILL_RAM:-4}` or 4G as of tool revision [57d5928f456e]) so if you give [shovill] 4G, give the SLURM job 8G.

One way you can adjust the `$SHOVILL_RAM` environment variable is via the [conda environment][]. That is, if you find the conda environment containing `shovill` you can set up files in `etc/conda/activate.d` and `etc/conda/deactivate.d` to set environment variables.

```bash
cd galaxy/deps/_conda/bin/activate galaxy/deps/_conda/envs/__shovill@0.9.0
mkdir -p etc/conda/activate.d
mkdir -p etc/conda/deactivate.d

echo -e "export _OLD_SHOVILL_RAM=\$SHOVILL_RAM\nexport SHOVILL_RAM=8" >> etc/conda/activate.d/shovill-ram.sh
echo -e "export SHOVILL_RAM=\$_OLD_SHOVILL_RAM" >> etc/conda/activate.d/shovill-ram.sh
```

You could also get fancier with this by setting `SHOVILL_RAM` based on [GALAXY_MEMORY_MB][], which is assigned by Galaxy based on your job configuration and resource requirements. For example, by setting `SHOVILL_RAM=$($GALAXY_MEMORY_MB/1024)`.


## Step 2: Install Galaxy Tools

Please install all the Galaxy tools in the table above by logging into Galaxy, navigating to **Admin > Search and browse tool sheds**, searching for the appropriate **Tool Name** and installing the appropriate **Toolshed Installable Revision**.

The install progress can be checked by monitoring the Galaxy log files `galaxy/*.log`.  On completion you should see a message of `Installed` next to the tool when going to **Admin > Manage installed tool shed repositories**.

**Note**: Prokka downloads several large databases and may take some time to install.

### Updating `tbl2asn`

The assembly workflow makes use of the software [Prokka][] for genome annotation.  Prokka makes use of [tbl2asn][], which has been programmed to stop working after 1 year from being built.  The version of `tbl2asn` installed by default may have to be updated. Please see our [FAQ][] for more details.


## Step 3: Testing Pipeline

A Galaxy workflow and some test data has been included with this documentation to verify that all tools are installed correctly.  To test this pipeline, please proceed through the following steps.

1. Upload the [Assembly Annotation Galaxy Workflow][] by going to **Workflow > Upload or import workflow**.
2. Upload the sequence reads by going to **Analyze Data** and then clicking on the **upload files from disk** icon ![upload-icon][].  Select the [test/reads][] files.  Make sure to change the **Type** of each file from **Auto-detect** to **fastqsanger**.  When uploaded you should see the following in your history.

    ![upload-history][]

3. Construct a dataset collection of the paired-end reads by clicking the **Operations on multiple datasets** icon ![datasets-icon][].  Please check off the two **.fastq** files and then go to **For all selected... > Build List of dataset pairs**.  You should see a screen that looks as follows.

    ![dataset-pair-screen][]

4. This should have properly paired your data and named the sample **a**.  Enter the name of this paired dataset collection at the bottom and click **Create list**.
5. Run the uploaded workflow by clicking on **Workflow**, clicking on the name of the workflow **AssemblyAnnotation-shovill-prokka-quast-paired_reads-v0.5 (imported from uploaded file)** and clicking **Run**.  This should auto fill in the dataset collection.  At the very bottom of the screen click **Run workflow**.
6. If everything was installed correctly, you should see each of the tools run successfully (turn green).  On completion this should look like.

    ![workflow-success][]

    If you see any tool turn red, you can click on the view details icon ![view-details-icon][] for more information.

If everything was successful then all dependencies for this pipeline have been properly installed.

[57d5928f456e]: https://toolshed.g2.bx.psu.edu/repos/iuc/shovill/rev/57d5928f456e
[eaee459f3d69]: https://toolshed.g2.bx.psu.edu/view/crs4/prokka/eaee459f3d69
[0834c823d4b9]: https://toolshed.g2.bx.psu.edu/view/iuc/quast/0834c823d4b9
[galaxy-config]: ../../setup#step-4-modify-configuration-file
[SLURM]: https://slurm.schedmd.com
[PILON]: https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4237348/
[SPAdes]: http://bioinf.spbau.ru/spades
[shovill]: https://github.com/tseemann/shovill/
[Prokka]: http://www.vicbioinformatics.com/software.prokka.shtml
[QUAST]: http://quast.sourceforge.net/quast.html
[tbl2asn]: http://www.ncbi.nlm.nih.gov/genbank/tbl2asn2/
[Galaxy Main Shed]: http://toolshed.g2.bx.psu.edu/
[IRIDA Toolshed]: https://irida.corefacility.ca/galaxy-shed
[Assembly Annotation Galaxy Workflow]: ../test/assembly-annotation/assembly-annotation.ga
[upload-icon]: ../test/snvphyl/images/upload-icon.jpg
[test/reads]: ../test/assembly-annotation/reads
[upload-history]: ../test/assembly-annotation/images/upload-history.jpg
[datasets-icon]: ../test/snvphyl/images/datasets-icon.jpg
[dataset-pair-screen]: ../test/assembly-annotation/images/dataset-pair-screen.jpg
[workflow-success]: ../test/assembly-annotation/images/workflow-success.png
[view-details-icon]: ../test/snvphyl/images/view-details-icon.jpg
[FAQ]: ../../../faq/#tbl2asn-out-of-date
[conda]: https://conda.io/docs/intro.html
[bioconda]: https://bioconda.github.io/
[FAQ/Conda dependencies]: ../../../faq#installing-conda-dependencies-in-galaxy-versions--v1601
[conda environment]: https://conda.io/docs/user-guide/tasks/manage-environments.html#saving-environment-variables
[GALAXY_MEMORY_MB]: https://planemo.readthedocs.io/en/latest/writing_advanced.html#developing-for-clusters-galaxy-slots-galaxy-memory-mb-and-galaxy-memory-mb-per-slot