---
layout: default
---

IRIDA Pipeline Development
==========================

This document describes the necessary steps for integrating new pipelines into IRIDA.

* This comment becomes the table of contents.
{:toc}

Introduction
------------

Pipelines in IRIDA take as input data managed by IRIDA and run through a collection of tools to produce some meaningful result.  Pipelines are implemented as a [Galaxy Workflow][] and executed using an instance of [Galaxy][] that has been setup for IRIDA.  Pipelines are versioned and are stored and distributed along with the IRIDA software.  Tools used by a pipeline are versioned and are stored and distributed using [Galaxy Toolsheds][].  In particular, the [Galaxy Main Toolshed][] and the [IRIDA Toolshed][] are used to store and distribute tools for a pipeline.

![irida-pipelines][]

Currently, there are two pipelines integrated into IRIDA:

1. A pipeline for constructing whole genome phylogenies.
2. A pipeline for performing *de novo* assembly and annotation on genomes.

IRIDA provides support for developing and integrating additional pipelines from Galaxy.  This process can be divided into two stages: **Galaxy Workflow Development** and **IRIDA Integration**.  The necessary steps, in brief, are:

1. Galaxy Workflow Development
    1. Develop a Galaxy Workflow
    2. Upload dependency tools to a Galaxy Toolshed
    3. Export Workflow
2. IRIDA Integration
    1. Pipeline Data Model
    2. IRIDA Workflow Description
    3. Additional IRIDA Updates
    4. Run IRIDA

Galaxy Workflow Development
---------------------------

Galaxy provides the ability to organize different bioinformatics tools together into a single workflow for producing specific results.  These workflows can make use of already existing bioinformatics tools in Galaxy, or can include customized tools which can be distributed using a [Galaxy Toolshed][Galaxy Toolsheds].

### 1. Develop a Galaxy Workflow

Galaxy provides a built-in editor for constructing and modifying workflows.

![galaxy-workflow-editor][]

This editor allows for the definition of input files and file types, tools and parameter settings for the tools, as well as which files will be used as output from the workflow.  More information on constructing Galaxy workflows can be found in the [Galaxy Workflow Editor][] documentation.

In order for a workflow to properly be integrated into IRIDA, the input and output to this workflow must be in a specific format.

#### A. Input Format

IRIDA currently only supports two types of input files: a collection of **paired-end sequence reads** in *FASTQ* format, and an optional **reference genome** in FASTA format.

For the **paired-end sequence reads** this must be a dataset collection of type **list:paired**.

![sequence-reads-input-editor][]

For the optional **reference genome**, if you wish to use a reference genome, the type must be an **input dataset**, not a dataset collection.

![reference-input-editor][]

Please also make note of the names given to each input dataset, in this case *sequence_reads_paired* and *reference*, as the names will be used to link up data sent from IRIDA to the Galaxy workflow.

#### B. Output Format

Output datasets within IRIDA can be of any file type and there can be many outputs for each workflow.  Each output should have a consistent name which will be used by IRIDA to find and download the appropriate file from Galaxy.  This can be accomplished by adding a **Rename Dataset** action to each output file.  In this case, for the tool **PhyML** the name is **phylogeneticTree.tre**.

![output-editor][]

In addition, each output dataset should be marked as a **workflow output** by selecting the asterix `*` icon, in this case both the **output_tree** and the **csv** files from the PhyML and SNP Matrix tools have been selected as output.

### 2. Upload dependency tools to a Galaxy Toolshed

If the workflow being developed includes custom tools that do not already exist in Galaxy these tools should be uploaded to a [Galaxy ToolShed][Galaxy Toolsheds] to allow for distribution of this workflow.  This should be done before building and exporting the final workflow __*.ga__ file, since the ids of each tool in the Galaxy workflow include the name of the toolshed.  For example, the id for Prokka, which is used for annotation of genomes, is `toolshed.g2.bx.psu.edu/repos/crs4/prokka/prokka/1.4.0`, which includes the name of the toolshed where Prokka can be found <https://toolshed.g2.bx.psu.edu/>.

More information on developing a tool for Galaxy can be found in the [Galaxy Tool Development][] documentation.

### 3. Export Workflow

Once the workflow is written in Galaxy, it can be exported to a file by going to the **Workflow** menu at the top, finding your particular workflow and selecting **Download or Export**.  This will save the workflow as a __*.ga__ file, which is a JSON-formatted file defining the tools, tool versions, and structure of the workflow.

![export-workflow][]

IRIDA Integration
-----------------

### 1. Pipeline Data Model

The [Analysis][] class is the root class for all analyses. In IRIDA, this class will be used to store the output files once an analysis is complete.  No modification or extension is needed for this class unless your pipeline is storing special results which need to be stored in the database.

In order to properly integrate the workflow with IRIDA your pipeline may require adding a new [AnalysisType][].  The [AnalysisType][] enum defines the different types of analyses/pipelines available in IRIDA.  You will need to add a new constant here for your particular analysis type.  For example:

```java
@XmlEnumValue("mypipeline")
MY_PIPELINE("mypipeline")
```

This links the constant `AnalysisType.MY_PIPELINE` to the string `mypipeline`.

### 2. IRIDA Workflow Definition

In order to integrate the Galaxy workflow with IRIDA, two files must be defined: (a) a **Workflow Structure** and (b) a **Workflow Description** file.  Both these files should be placed in a directory structure defining the name and version of the workflow.  For example, for the  pipeline version **0.1**, the directory structure should look like:

```
MyPipeline
└── 0.1
    ├── irida_workflow_structure.ga
    └── irida_workflow.xml
```

#### A. Workflow Structure

This is the __*.ga__ that that was exported from Galaxy in a previous step.  This file must be named **irida_workflow_structure.ga**.

#### B. Workflow Description

This is an **XML** file which is used to link up a Galaxy workflow with IRIDA.  It defines the particular **Analysis Type** a workflow belongs to as well as any dependency tools needed to be installed in an instance of Galaxy.  For a very simple workflow, this file would look like:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<iridaWorkflow>
	<id>49507566-e10c-41b2-ab6f-0fb5383be997</id>
	<name>MyPipeline</name>
	<version>0.1</version>
	<analysisType>mypipeline</analysisType>
	<inputs>
		<sequenceReadsPaired>sequence_reads_paired</sequenceReadsPaired>
		<reference>reference</reference>
		<requiresSingleSample>false</requiresSingleSample>
	</inputs>
	<parameters>
		<parameter name="my_parameter" defaultValue="1">
			<toolParameter
				toolId="my_tool"
				parameterName="my_parameter" />
		</parameter>
	</parameters>
	<outputs>
		<output name="tree" fileName="phylogeneticTree.tre" />
	</outputs>
	<toolRepositories>
		<repository>
			<name>my_tool</name>
			<owner>irida</owner>
			<url>https://irida.corefacility.ca/galaxy-shed</url>
			<revision>de3e46eaf5ba</revision>
		</repository>
	</toolRepositories>
</iridaWorkflow>
```

A few things to note:

  1. `<id>` defines a unique id for the workflow.  This must be a UUID.  A quick way to generate a random UUID on linux is the command `uuid -v 4`.
  2. `<analysisType>` defines what type of analysis this workflow belongs to.  This string should match the string defined for the custom [AnalysisType][] above.
  2. `<sequenceReadsPaired>` defines the name of the input dataset in Galaxy for the paired-end sequence reads chosen previously.  In this case it is *sequence_reads_paired*.
  3. `<reference>` defines the name of the input dataset in Galaxy for the reference file.  In this case it is *reference*.
  4. `<toolParameter>` defines how to map parameters a user selects in IRIDA to those in Galaxy (defined in the **irida_workflow_structure.ga** file).
  5. `<output>` defines, for an output file, a data model name in IRIDA and maps it to the name of the file in Galaxy that was chosen previously.  In this case it is *phylogeneticTree.tre*.
  6. `<toolRepositories>` defines the different Galaxy ToolSheds from which the dependency tools come from, as well as a revision number for the tool.

For more information, please see the [IRIDA Workflow Description][] documentation.  This file must be named **irida_workflow.xml**.

#### C. Move Workflow Definition

In order for IRIDA to automatically load up the workflow definition files, the entire directory structure for `MyPipeline` should be moved to `src/main/resources/ca/corefacility/bioinformatics/irida/model/enums/workflows/MyPipeline`.  So, this should look like:

```
src/main/resources/ca/corefacility/bioinformatics/irida/model/enums/workflows/MyPipeline
└── 0.1
    ├── irida_workflow_structure.ga
    └── irida_workflow.xml
```

### 3. Additional IRIDA Updates

A few other smaller steps need to be taken before the workflow is properly integrated into IRIDA.  These include the following.

#### A. Adding a default workflow entry

The file `src/main/resources/ca/corefacility/bioinformatics/irida/config/workflows.properties` defines the default workflows associated with a particular analysis pipeline type.  This is in the format of **irida.workflow.default.[analysis_type]=[analysis_id]**.  Please fill in the **[analysis_type]** and **[analysis_id]** entries for your specific workflow and add this line to the `workflows.properties` file.  For example:

```
irida.workflow.default.mypipeline=49507566-e10c-41b2-ab6f-0fb5383be997
```

In this case, the **[analysis_type]** is *mypipeline*, which comes from the `<analysisType>` XML tag from the workflow description file.  The **[analysis_id]** is *49507566-e10c-41b2-ab6f-0fb5383be997*, which comes from the `<id>` XML tag from the workflow description file.

#### B. Adding messages for the UI

Some messages need to be defined in order to display the pipeline in the UI.  These are stored in the file `src/main/resources/i18n/messages_en.properties` and include messages for the title and description displayed in the UI as well as error messages for each parameter.  This should look similar to:

```
workflow.mypipeline.title=My Pipeline
workflow.mypipeline.description=Run my custom pipeline.

pipeline.title.MyPipeline=Pipelines - My Pipeline
pipeline.h1.MyPipeline=My Pipeline

pipeline.parameters.modal-title.mypipeline=My Pipeline Parameters
pipeline.parameters.mypipeline.my_parameter=My Parameter Description.
```

### 4. Run IRIDA

Once you've made all the above modifications, you can attempt to load up the pipeline in IRIDA with the command:

```
mvn clean jetty:run
```

This should launch an instance of IRIDA on <http://localhost:8080>. If you log in with **admin** and **password1** you should be able to navigate to the pipelines page, which should now display:

![my-pipeline-irida][]

If you select some samples and attempt to run this pipeline you should see:

![my-pipeline-launch][]

If you attempt to modify the parameters of this pipeline you should see:

![my-pipeline-parameters][]

[Galaxy]: http://galaxyproject.org/
[Galaxy Toolsheds]: https://wiki.galaxyproject.org/ToolShed
[Galaxy Main Toolshed]: https://toolshed.g2.bx.psu.edu/
[IRIDA Toolshed]: https://irida.corefacility.ca/galaxy-shed
[galaxy-workflow-editor]: images/galaxy-workflow-editor.png
[irida-pipelines]: images/irida-pipelines.png
[Galaxy Workflow]: https://wiki.galaxyproject.org/Learn/AdvancedWorkflow
[Galaxy Workflow Editor]: https://wiki.galaxyproject.org/Learn/AdvancedWorkflow/BasicEditing
[reference-input-editor]: images/reference-input-editor.png
[sequence-reads-input-editor]: images/sequence-reads-input-editor.png
[output-editor]: images/output-editor.png
[export-workflow]: images/export-workflow.png
[AnalysisType]: ../../apidocs/ca/corefacility/bioinformatics/irida/model/enums/AnalysisType.html
[Analysis]: ../../apidocs/ca/corefacility/bioinformatics/irida/model/workflow/analysis/Analysis.html
[AnalysisPhylogenomicsPipeline]: ../../apidocs/ca/corefacility/bioinformatics/irida/model/workflow/analysis/AnalysisPhylogenomicsPipeline.html
[IRIDA Workflow Description]: workflow-description/
[Galaxy Tool Development]: galaxy/
[my-pipeline-irida]: images/my-pipeline-irida.png
[my-pipeline-launch]: images/my-pipeline-launch.png
[my-pipeline-parameters]: images/my-pipeline-parameters.png