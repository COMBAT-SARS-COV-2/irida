package ca.corefacility.bioinformatics.irida.service.impl;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.lucene.queryparser.classic.QueryParser;

import ca.corefacility.bioinformatics.irida.service.TaxonomyService;
import ca.corefacility.bioinformatics.irida.util.TreeNode;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A {@link TaxonomyService} leveraging Apache Jena's in memory storage service
 * and Apache Lucene's text searching abilities.
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
public class InMemoryTaxonomyService implements TaxonomyService {
	private Model model;
	private Dataset dataset;

	public InMemoryTaxonomyService(Path taxonomyFileLocation) {
		dataset = DatasetFactory.createMem();

		model = dataset.getDefaultModel();
		RDFDataMgr.read(model, taxonomyFileLocation.toString());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<TreeNode<String>> search(String searchTerm) {
		String queryString = StrUtils.strjoinNL("PREFIX text: <http://jena.apache.org/text#>",
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
				"PREFIX go: <http://www.geneontology.org/formats/oboInOwl#>", "SELECT * ",
				"{ ?s go:hasOBONamespace 'ncbi_taxonomy'; ", "rdfs:label ?label .", "FILTER regex(?label, ?term, 'i')",
				"}");

		HashMap<String, TreeNode<String>> visited = new HashMap<>();

		Set<TreeNode<String>> visitedRoots = new HashSet<>();

		ParameterizedSparqlString query = new ParameterizedSparqlString(queryString);
		// add a * for wildcard search
		query.setLiteral("term", QueryParser.escape(searchTerm));
		Query q = query.asQuery();
		QueryExecution qexec = QueryExecutionFactory.create(q, dataset);
		ResultSet result = qexec.execSelect();

		while (result.hasNext()) {
			QuerySolution next = result.next();

			buildTrimmedResultTree(next.getResource("s"), searchTerm, visited);

		}

		// get all the roots
		for (Entry<String, TreeNode<String>> entry : visited.entrySet()) {
			TreeNode<String> current = entry.getValue();
			while (current.getParent() != null) {
				current = current.getParent();
			}

			if (!visitedRoots.contains(current)) {
				visitedRoots.add(current);
			}
		}

		return visitedRoots;
	}

	/**
	 * Build a result tree from a searched resource. This search will look
	 * upwards in the tree until there are no more parent nodes.
	 * 
	 * @param resource
	 *            The resource to start from
	 * @param searchTerm
	 *            The search term that must be included
	 * @param visited
	 *            A map of previously visited nodes.
	 * @return
	 */
	private TreeNode<String> buildTrimmedResultTree(Resource resource, String searchTerm,
			Map<String, TreeNode<String>> visited) {
		TreeNode<String> treeNode;
		String resourceURI = resource.getURI();

		if (visited.containsKey(resourceURI)) {
			treeNode = visited.get(resourceURI);
		} else {
			String elementName = resource.getProperty(RDFS.label).getObject().asLiteral().getString();
			treeNode = new TreeNode<>(elementName);
			visited.put(resourceURI, treeNode);

			Resource matchingParent = getMatchingParent(resource, searchTerm);
			if (matchingParent != null) {
				TreeNode<String> parent = buildTrimmedResultTree(matchingParent, searchTerm, visited);
				parent.addChild(treeNode);
				treeNode.setParent(parent);
			}
		}

		return treeNode;
	}

	/**
	 * Get a parent node with the matching search term
	 * 
	 * @param resource
	 *            The resource to start walking up from
	 * @param searchTerm
	 *            The search term required
	 * @return A parent of the given node with the given search term in the
	 *         label.
	 */
	private Resource getMatchingParent(Resource resource, String searchTerm) {
		NodeIterator subClasses = model.listObjectsOfProperty(resource, RDFS.subClassOf);

		if (subClasses.hasNext()) {
			Resource parentNode = subClasses.next().asResource();
			String elementName = parentNode.getProperty(RDFS.label).getObject().asLiteral().getString();

			if (elementName.toLowerCase().contains(searchTerm.toLowerCase())) {
				return parentNode;
			} else {
				return getMatchingParent(parentNode, searchTerm);
			}
		}

		return null;

	}
}
